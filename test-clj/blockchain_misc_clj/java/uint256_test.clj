(ns blockchain-misc-clj.java.uint256-test
  (:require [clojure.test :refer [deftest testing is]])
  (:import (ai.z7.blockchain_misc.UInts UInt256 UInt128)
           (java.math BigInteger)))

;; Construction tests

(deftest uint256-construction-from-long
  (testing "construction from long"
    (is (= 0 (.longValue (UInt256. 0))))
    (is (= 1 (.longValue (UInt256. 1))))
    (is (= 100 (.longValue (UInt256. 100))))
    (is (= Long/MAX_VALUE (.longValue (UInt256. Long/MAX_VALUE))))))

(deftest uint256-construction-from-string
  (testing "construction from decimal string"
    (is (= 0 (.longValue (UInt256. "0"))))
    (is (= 12345 (.longValue (UInt256. "12345"))))))

(deftest uint256-construction-from-biginteger
  (testing "construction from BigInteger"
    (let [bi (BigInteger. "123456789012345678901234567890123456789012345678901234567890")]
      (is (= bi (.toBigInteger (UInt256. bi)))))))

(deftest uint256-construction-from-uint128
  (testing "construction from UInt128"
    (let [u128 (UInt128. "12345678901234567890")]
      (is (= (.toBigInteger u128) (.toBigInteger (UInt256. u128)))))))

(deftest uint256-construction-from-bytes
  (testing "construction from byte array"
    (let [bs (byte-array [0x01 0x02 0x03 0x04])]
      (is (= 0x01020304 (.longValue (UInt256. bs)))))))

;; Constant tests

(deftest uint256-constants
  (testing "ZERO constant"
    (is (.isZero UInt256/ZERO))
    (is (= 0 (.longValue UInt256/ZERO))))
  (testing "ONE constant"
    (is (= 1 (.longValue UInt256/ONE))))
  (testing "TWO constant"
    (is (= 2 (.longValue UInt256/TWO))))
  (testing "MAX_VALUE constant"
    (is (= 256 (.bitLength UInt256/MAX_VALUE)))))

;; Arithmetic tests

(deftest uint256-addition
  (testing "basic addition"
    (is (= 5 (.longValue (.add (UInt256. 2) (UInt256. 3)))))
    (is (= 100 (.longValue (.add (UInt256. 0) (UInt256. 100)))))))

(deftest uint256-subtraction
  (testing "basic subtraction"
    (is (= 2 (.longValue (.subtract (UInt256. 5) (UInt256. 3)))))
    (is (.isZero (.subtract (UInt256. 5) (UInt256. 5))))))

(deftest uint256-underflow-wrapping
  (testing "subtraction underflow wraps around"
    (let [result (.subtract (UInt256. 0) (UInt256. 1))]
      (is (= UInt256/MAX_VALUE result)))))

(deftest uint256-multiplication
  (testing "basic multiplication"
    (is (= 6 (.longValue (.multiply (UInt256. 2) (UInt256. 3)))))
    (is (.isZero (.multiply (UInt256. 100) UInt256/ZERO)))))

(deftest uint256-division
  (testing "basic division"
    (is (= 3 (.longValue (.divide (UInt256. 9) (UInt256. 3)))))
    (is (= 2 (.longValue (.divide (UInt256. 7) (UInt256. 3))))))
  (testing "division by zero throws"
    (is (thrown? ArithmeticException (.divide (UInt256. 5) UInt256/ZERO)))))

(deftest uint256-mod
  (testing "modulo operation"
    (is (= 1 (.longValue (.mod (UInt256. 7) (UInt256. 3)))))
    (is (.isZero (.mod (UInt256. 9) (UInt256. 3)))))
  (testing "mod by zero throws"
    (is (thrown? ArithmeticException (.mod (UInt256. 5) UInt256/ZERO)))))

(deftest uint256-divmod
  (testing "divmod returns quotient and remainder"
    (let [result (.divmod (UInt256. 7) (UInt256. 3))]
      (is (= 2 (.longValue (aget result 0))))
      (is (= 1 (.longValue (aget result 1)))))))

(deftest uint256-inc-dec
  (testing "increment"
    (is (= 1 (.longValue (.inc UInt256/ZERO))))
    (is (= 2 (.longValue (.inc UInt256/ONE)))))
  (testing "decrement"
    (is (.isZero (.dec UInt256/ONE)))
    (is (= UInt256/MAX_VALUE (.dec UInt256/ZERO)))))

(deftest uint256-pow
  (testing "exponentiation"
    (is (= 8 (.longValue (.pow (UInt256. 2) 3))))
    (is (= 1 (.longValue (.pow (UInt256. 5) 0))))
    (is (.isZero (.pow UInt256/ZERO 5))))
  (testing "negative exponent throws"
    (is (thrown? ArithmeticException (.pow (UInt256. 2) -1)))))

;; Bitwise operations

(deftest uint256-bitwise-and
  (testing "bitwise AND"
    (is (= 2r1000 (.longValue (.and (UInt256. 2r1010) (UInt256. 2r1100)))))))

(deftest uint256-bitwise-or
  (testing "bitwise OR"
    (is (= 2r1110 (.longValue (.or (UInt256. 2r1010) (UInt256. 2r1100)))))))

(deftest uint256-bitwise-xor
  (testing "bitwise XOR"
    (is (= 2r0110 (.longValue (.xor (UInt256. 2r1010) (UInt256. 2r1100)))))))

(deftest uint256-bitwise-not
  (testing "bitwise NOT"
    (is (= UInt256/MAX_VALUE (.not UInt256/ZERO)))))

(deftest uint256-shift-left
  (testing "left shift"
    (is (= 8 (.longValue (.shiftLeft (UInt256. 1) 3))))
    (is (= 4 (.longValue (.shiftLeft (UInt256. 16) -2))))))

(deftest uint256-shift-right
  (testing "right shift"
    (is (= 2 (.longValue (.shiftRight (UInt256. 8) 2))))
    (is (= 16 (.longValue (.shiftRight (UInt256. 4) -2))))))

(deftest uint256-set-clear-flip-test-bit
  (testing "setBit"
    (is (= 5 (.longValue (.setBit (UInt256. 1) 2)))))
  (testing "clearBit"
    (is (= 1 (.longValue (.clearBit (UInt256. 5) 2)))))
  (testing "flipBit"
    (is (= 5 (.longValue (.flipBit (UInt256. 1) 2))))
    (is (= 1 (.longValue (.flipBit (UInt256. 5) 2)))))
  (testing "testBit"
    (is (.testBit (UInt256. 5) 0))
    (is (not (.testBit (UInt256. 5) 1)))
    (is (.testBit (UInt256. 5) 2))))

;; Comparison and equality

(deftest uint256-comparisons
  (testing "compareTo"
    (is (neg? (.compareTo (UInt256. 1) (UInt256. 2))))
    (is (pos? (.compareTo (UInt256. 2) (UInt256. 1))))
    (is (zero? (.compareTo (UInt256. 5) (UInt256. 5))))))

(deftest uint256-equality
  (testing "equals"
    (is (.equals (UInt256. 123) (UInt256. 123)))
    (is (not (.equals (UInt256. 123) (UInt256. 456))))))

(deftest uint256-min-max
  (testing "min and max"
    (is (= 1 (.longValue (.min (UInt256. 1) (UInt256. 5)))))
    (is (= 5 (.longValue (.max (UInt256. 1) (UInt256. 5)))))))

;; Conversion tests

(deftest uint256-biginteger-roundtrip
  (testing "BigInteger round-trip"
    (let [bi (BigInteger. "12345678901234567890123456789012345678901234567890")]
      (is (= bi (.toBigInteger (UInt256. bi)))))))

(deftest uint256-byte-array-roundtrip
  (testing "byte array round-trip"
    (let [original (UInt256. "123456789012345678901234567890123456789012345678901234567890")
          bytes (.toByteArray original)
          restored (UInt256. bytes)]
      (is (.equals original restored)))))

(deftest uint256-int-array-roundtrip
  (testing "int array round-trip"
    (let [original (UInt256. "123456789012345678901234567890123456789012345678901234567890")
          ints (.toIntArray original)
          restored (UInt256. ints)]
      (is (.equals original restored)))))

;; String conversion

(deftest uint256-string-conversion
  (testing "toString and string constructor"
    (is (= "12345" (.toString (UInt256. "12345"))))
    (is (= "ff" (.toString (UInt256. 255) 16)))))

;; Modular arithmetic

(deftest uint256-addmod
  (testing "addmod operation"
    (is (= 1 (.longValue (.addmod (UInt256. 4) (UInt256. 3) (UInt256. 6))))))
  (testing "addmod with zero values"
    (is (.isZero (.addmod UInt256/ZERO UInt256/ZERO (UInt256. 5))))))

(deftest uint256-mulmod
  (testing "mulmod operation"
    (is (= 2 (.longValue (.mulmod (UInt256. 4) (UInt256. 5) (UInt256. 6)))))))

;; 256-bit specific tests

(deftest uint256-large-values
  (testing "operations with values > 128 bits"
    (let [large (UInt256. (.pow (BigInteger. "2") 200))]
      (is (= 201 (.bitLength large)))
      (is (= 202 (.bitLength (.multiply large (UInt256. 2))))))))

(deftest uint256-max-value-operations
  (testing "operations near MAX_VALUE"
    (is (= UInt256/MAX_VALUE (.add UInt256/MAX_VALUE UInt256/ZERO)))
    (is (.isZero (.add UInt256/MAX_VALUE UInt256/ONE)))  ; overflow wraps
    (is (.isZero (.subtract UInt256/MAX_VALUE UInt256/MAX_VALUE)))))

(deftest uint256-uint128-conversion
  (testing "UInt256 from UInt128 preserves value"
    (let [u128 UInt128/MAX_VALUE
          u256 (UInt256. u128)]
      (is (= (.toBigInteger u128) (.toBigInteger u256)))))
  (testing "UInt128 from large UInt256 truncates"
    (let [u256 UInt256/MAX_VALUE
          u128 (UInt128. u256)]
      (is (= UInt128/MAX_VALUE u128)))))
