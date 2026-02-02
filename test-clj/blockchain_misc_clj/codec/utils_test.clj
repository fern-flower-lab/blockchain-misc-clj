(ns blockchain-misc-clj.codec.utils-test
  (:require [clojure.test :refer [deftest testing is]]
            [blockchain-misc-clj.codec.utils :refer [byte-count bytes->long long->bytes
                                                      hex->bytes bytes->hex]])
  (:import (ai.z7.blockchain_misc.UInts UInt256)
           (java.math BigInteger)))

;; byte-count tests

(deftest byte-count-tests
  (testing "byte-count returns correct counts"
    (is (= 0 (byte-count 0)))
    (is (= 1 (byte-count 1)))
    (is (= 1 (byte-count 255)))
    (is (= 2 (byte-count 256)))
    (is (= 2 (byte-count 65535)))
    (is (= 3 (byte-count 65536)))
    (is (= 4 (byte-count 0xFFFFFFFF)))
    (is (= 5 (byte-count 0x100000000)))
    (is (= 8 (byte-count Long/MAX_VALUE)))))

;; bytes->long tests

(deftest bytes->long-tests
  (testing "bytes->long converts byte arrays to longs"
    (is (= 0 (bytes->long (byte-array []))))
    (is (= 1 (bytes->long (byte-array [1]))))
    (is (= 255 (bytes->long (byte-array [-1]))))
    (is (= 256 (bytes->long (byte-array [1 0]))))
    (is (= 0x01020304 (bytes->long (byte-array [1 2 3 4])))))
  (testing "bytes->long with offset and length"
    (is (= 2 (bytes->long (byte-array [1 2 3]) 1 1)))
    (is (= 0x0203 (bytes->long (byte-array [1 2 3 4]) 1 2)))))

;; long->bytes tests

(deftest long->bytes-tests
  (testing "long->bytes converts longs to byte arrays"
    (is (java.util.Arrays/equals (byte-array []) (long->bytes 0)))
    (is (java.util.Arrays/equals (byte-array [1]) (long->bytes 1)))
    (is (java.util.Arrays/equals (byte-array [-1]) (long->bytes 255)))
    (is (java.util.Arrays/equals (byte-array [1 0]) (long->bytes 256)))
    (is (java.util.Arrays/equals (byte-array [1 2 3 4]) (long->bytes 0x01020304)))))

(deftest bytes-long-roundtrip
  (testing "bytes->long and long->bytes are inverse operations"
    (doseq [v [0 1 127 128 255 256 65535 65536 0xFFFFFFFF 0x100000000]]
      (is (= v (bytes->long (long->bytes v)))))))

;; hex->bytes tests

(deftest hex->bytes-tests
  (testing "hex->bytes parses hex strings"
    (is (java.util.Arrays/equals (byte-array []) (hex->bytes "")))
    (is (java.util.Arrays/equals (byte-array [0]) (hex->bytes "00")))
    (is (java.util.Arrays/equals (byte-array [1]) (hex->bytes "01")))
    (is (java.util.Arrays/equals (byte-array [-1]) (hex->bytes "ff")))
    (is (java.util.Arrays/equals (byte-array [-1]) (hex->bytes "FF")))
    (is (java.util.Arrays/equals (byte-array [1 2 3]) (hex->bytes "010203"))))
  (testing "hex->bytes handles 0x prefix"
    (is (java.util.Arrays/equals (byte-array [1 2 3]) (hex->bytes "0x010203")))
    (is (java.util.Arrays/equals (byte-array [-1]) (hex->bytes "0xFF")))))

(deftest hex->bytes-validation
  (testing "hex->bytes throws on odd-length strings"
    (is (thrown? IllegalArgumentException (hex->bytes "1")))
    (is (thrown? IllegalArgumentException (hex->bytes "123"))))
  (testing "hex->bytes throws on invalid characters"
    (is (thrown? IllegalArgumentException (hex->bytes "GG")))
    (is (thrown? IllegalArgumentException (hex->bytes "zz")))
    (is (thrown? IllegalArgumentException (hex->bytes "!@")))))

(deftest hex->bytes-nil-handling
  (testing "hex->bytes handles nil"
    (is (thrown? Exception (hex->bytes nil)))))

;; bytes->hex tests

(deftest bytes->hex-tests
  (testing "bytes->hex converts byte arrays to hex"
    (is (= "" (bytes->hex (byte-array []))))
    (is (= "1" (bytes->hex (byte-array [1]))))
    (is (= "ff" (bytes->hex (byte-array [-1]))))
    (is (= "10203" (bytes->hex (byte-array [1 2 3])))))
  (testing "bytes->hex strips leading zeros"
    (is (= "1" (bytes->hex (byte-array [0 0 1]))))))

(deftest bytes->hex-with-padding
  (testing "bytes->hex respects pad-left option"
    (is (= "0001" (bytes->hex (byte-array [1]) {:pad-left 2})))
    (is (= "00000001" (bytes->hex (byte-array [1]) {:pad-left 4})))))

(deftest bytes->hex-biginteger
  (testing "bytes->hex handles BigInteger"
    (is (= "7b" (bytes->hex (BigInteger. "123"))))
    (is (= "ffffffff" (bytes->hex (BigInteger. "4294967295"))))))

(deftest bytes->hex-uint256
  (testing "bytes->hex handles UInt256"
    (is (= "7b" (bytes->hex (UInt256. 123))))
    (is (= "ff" (bytes->hex (UInt256. 255))))))

(deftest bytes->hex-number
  (testing "bytes->hex handles numbers"
    (is (= "7b" (bytes->hex 123)))
    (is (= "ffffffff" (bytes->hex 4294967295)))))

(deftest hex-roundtrip
  (testing "hex->bytes and bytes->hex roundtrip"
    (doseq [hex ["01" "ff" "0102030405060708"]]
      (let [bs (hex->bytes hex)]
        (is (= hex (bytes->hex bs {:pad-left (/ (count hex) 2)})))))))
