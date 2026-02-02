(ns blockchain-misc-clj.java.uint128-test
  (:require [clojure.test :refer [deftest testing is]])
  (:import (ai.z7.blockchain_misc.UInts UInt128)
           (java.math BigInteger)))

;; Construction tests

(deftest uint128-construction-from-long
  (testing "construction from long"
    (is (= 0 (.longValue (UInt128. 0))))
    (is (= 1 (.longValue (UInt128. 1))))
    (is (= 100 (.longValue (UInt128. 100))))
    (is (= Long/MAX_VALUE (.longValue (UInt128. Long/MAX_VALUE))))))

(deftest uint128-construction-from-string
  (testing "construction from decimal string"
    (is (= 0 (.longValue (UInt128. "0"))))
    (is (= 12345 (.longValue (UInt128. "12345"))))
    (is (= "340282366920938463463374607431768211455"
           (.toString UInt128/MAX_VALUE)))))

(deftest uint128-construction-from-biginteger
  (testing "construction from BigInteger"
    (let [bi (BigInteger. "123456789012345678901234567890")]
      (is (= bi (.toBigInteger (UInt128. bi)))))))

(deftest uint128-construction-from-bytes
  (testing "construction from byte array"
    (let [bs (byte-array [0x01 0x02 0x03 0x04])]
      (is (= 0x01020304 (.longValue (UInt128. bs)))))))

(deftest uint128-construction-from-int-array
  (testing "construction from int array"
    (let [ints (int-array [1 2])]
      (is (= (+ (bit-shift-left 1 32) 2) (.longValue (UInt128. ints)))))))

;; Constant tests

(deftest uint128-constants
  (testing "ZERO constant"
    (is (.isZero UInt128/ZERO))
    (is (= 0 (.longValue UInt128/ZERO))))
  (testing "ONE constant"
    (is (= 1 (.longValue UInt128/ONE))))
  (testing "TWO constant"
    (is (= 2 (.longValue UInt128/TWO))))
  (testing "MAX_VALUE constant"
    (is (= 128 (.bitLength UInt128/MAX_VALUE)))))

;; Arithmetic tests

(deftest uint128-addition
  (testing "basic addition"
    (is (= 5 (.longValue (.add (UInt128. 2) (UInt128. 3)))))
    (is (= 100 (.longValue (.add (UInt128. 0) (UInt128. 100)))))))

(deftest uint128-subtraction
  (testing "basic subtraction"
    (is (= 2 (.longValue (.subtract (UInt128. 5) (UInt128. 3)))))
    (is (.isZero (.subtract (UInt128. 5) (UInt128. 5))))))

(deftest uint128-underflow-wrapping
  (testing "subtraction underflow wraps around"
    (let [result (.subtract (UInt128. 0) (UInt128. 1))]
      (is (= UInt128/MAX_VALUE result)))))

(deftest uint128-multiplication
  (testing "basic multiplication"
    (is (= 6 (.longValue (.multiply (UInt128. 2) (UInt128. 3)))))
    (is (.isZero (.multiply (UInt128. 100) UInt128/ZERO)))))

(deftest uint128-division
  (testing "basic division"
    (is (= 3 (.longValue (.divide (UInt128. 9) (UInt128. 3)))))
    (is (= 2 (.longValue (.divide (UInt128. 7) (UInt128. 3))))))
  (testing "division by zero throws"
    (is (thrown? ArithmeticException (.divide (UInt128. 5) UInt128/ZERO)))))

(deftest uint128-mod
  (testing "modulo operation"
    (is (= 1 (.longValue (.mod (UInt128. 7) (UInt128. 3)))))
    (is (.isZero (.mod (UInt128. 9) (UInt128. 3)))))
  (testing "mod by zero throws"
    (is (thrown? ArithmeticException (.mod (UInt128. 5) UInt128/ZERO)))))

(deftest uint128-divmod
  (testing "divmod returns quotient and remainder"
    (let [result (.divmod (UInt128. 7) (UInt128. 3))]
      (is (= 2 (.longValue (aget result 0))))
      (is (= 1 (.longValue (aget result 1)))))))

(deftest uint128-inc-dec
  (testing "increment"
    (is (= 1 (.longValue (.inc UInt128/ZERO))))
    (is (= 2 (.longValue (.inc UInt128/ONE)))))
  (testing "decrement"
    (is (.isZero (.dec UInt128/ONE)))
    (is (= UInt128/MAX_VALUE (.dec UInt128/ZERO)))))

(deftest uint128-pow
  (testing "exponentiation"
    (is (= 8 (.longValue (.pow (UInt128. 2) 3))))
    (is (= 1 (.longValue (.pow (UInt128. 5) 0))))
    (is (.isZero (.pow UInt128/ZERO 5))))
  (testing "negative exponent throws"
    (is (thrown? ArithmeticException (.pow (UInt128. 2) -1)))))

;; Bitwise operations

(deftest uint128-bitwise-and
  (testing "bitwise AND"
    (is (= 2r1000 (.longValue (.and (UInt128. 2r1010) (UInt128. 2r1100)))))))

(deftest uint128-bitwise-or
  (testing "bitwise OR"
    (is (= 2r1110 (.longValue (.or (UInt128. 2r1010) (UInt128. 2r1100)))))))

(deftest uint128-bitwise-xor
  (testing "bitwise XOR"
    (is (= 2r0110 (.longValue (.xor (UInt128. 2r1010) (UInt128. 2r1100)))))))

(deftest uint128-bitwise-not
  (testing "bitwise NOT"
    (is (= UInt128/MAX_VALUE (.not UInt128/ZERO)))))

(deftest uint128-shift-left
  (testing "left shift"
    (is (= 8 (.longValue (.shiftLeft (UInt128. 1) 3))))
    (is (= 4 (.longValue (.shiftLeft (UInt128. 16) -2))))))

(deftest uint128-shift-right
  (testing "right shift"
    (is (= 2 (.longValue (.shiftRight (UInt128. 8) 2))))
    (is (= 16 (.longValue (.shiftRight (UInt128. 4) -2))))))

(deftest uint128-set-clear-flip-test-bit
  (testing "setBit"
    (is (= 5 (.longValue (.setBit (UInt128. 1) 2)))))
  (testing "clearBit"
    (is (= 1 (.longValue (.clearBit (UInt128. 5) 2)))))
  (testing "flipBit"
    (is (= 5 (.longValue (.flipBit (UInt128. 1) 2))))
    (is (= 1 (.longValue (.flipBit (UInt128. 5) 2)))))
  (testing "testBit"
    (is (.testBit (UInt128. 5) 0))
    (is (not (.testBit (UInt128. 5) 1)))
    (is (.testBit (UInt128. 5) 2))))

;; Comparison and equality

(deftest uint128-comparisons
  (testing "compareTo"
    (is (neg? (.compareTo (UInt128. 1) (UInt128. 2))))
    (is (pos? (.compareTo (UInt128. 2) (UInt128. 1))))
    (is (zero? (.compareTo (UInt128. 5) (UInt128. 5))))))

(deftest uint128-equality
  (testing "equals"
    (is (.equals (UInt128. 123) (UInt128. 123)))
    (is (not (.equals (UInt128. 123) (UInt128. 456))))))

(deftest uint128-min-max
  (testing "min and max"
    (is (= 1 (.longValue (.min (UInt128. 1) (UInt128. 5)))))
    (is (= 5 (.longValue (.max (UInt128. 1) (UInt128. 5)))))))

;; Conversion tests

(deftest uint128-biginteger-roundtrip
  (testing "BigInteger round-trip"
    (let [bi (BigInteger. "12345678901234567890")]
      (is (= bi (.toBigInteger (UInt128. bi)))))))

(deftest uint128-byte-array-roundtrip
  (testing "byte array round-trip"
    (let [original (UInt128. "123456789012345678901234567890")
          bytes (.toByteArray original)
          restored (UInt128. bytes)]
      (is (.equals original restored)))))

(deftest uint128-int-array-roundtrip
  (testing "int array round-trip"
    (let [original (UInt128. "123456789012345678901234567890")
          ints (.toIntArray original)
          restored (UInt128. ints)]
      (is (.equals original restored)))))

;; String conversion

(deftest uint128-string-conversion
  (testing "toString and string constructor"
    (is (= "12345" (.toString (UInt128. "12345"))))
    (is (= "ff" (.toString (UInt128. 255) 16)))))

;; Modular arithmetic

(deftest uint128-addmod
  (testing "addmod operation"
    (is (= 1 (.longValue (.addmod (UInt128. 4) (UInt128. 3) (UInt128. 6)))))))

(deftest uint128-mulmod
  (testing "mulmod operation"
    (is (= 2 (.longValue (.mulmod (UInt128. 4) (UInt128. 5) (UInt128. 6)))))))
