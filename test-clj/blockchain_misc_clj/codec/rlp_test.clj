(ns blockchain-misc-clj.codec.rlp-test
  (:require [clojure.test :refer [deftest testing is]]
            [blockchain-misc-clj.codec.rlp :refer [encode decode vector?]]
            [blockchain-misc-clj.codec.utils :refer [hex->bytes bytes->hex]])
  (:import (java.util Arrays)))

;; Nil encoding

(deftest rlp-nil-encoding
  (testing "nil encodes as empty string prefix"
    (is (= 0x80 (bit-and 0xff (aget (encode nil) 0))))
    (is (= 1 (alength (encode nil))))))

;; Single byte encoding (compact form)

(deftest rlp-single-byte-compact
  (testing "single byte < 0x80 encodes as itself"
    (is (= 1 (alength (encode (byte-array [0])))))
    (is (= 0 (aget (encode (byte-array [0])) 0)))
    (is (= 1 (alength (encode (byte-array [127])))))
    (is (= 127 (aget (encode (byte-array [127])) 0)))))

(deftest rlp-single-byte-non-compact
  (testing "single byte >= 0x80 gets prefixed"
    (let [encoded (encode (byte-array [-128]))]  ; 0x80
      (is (= 2 (alength encoded)))
      (is (= (unchecked-byte 0x81) (aget encoded 0))))))

;; Short string encoding (1-55 bytes)

(deftest rlp-short-string
  (testing "short strings (1-55 bytes) encode with length prefix"
    (let [data (byte-array [1 2 3])
          encoded (encode data)]
      (is (= (+ 0x80 3) (bit-and 0xff (aget encoded 0))))
      (is (= 4 (alength encoded))))
    (let [data (byte-array 55 (byte 42))
          encoded (encode data)]
      (is (= (+ 0x80 55) (bit-and 0xff (aget encoded 0))))
      (is (= 56 (alength encoded))))))

;; Long string encoding (56+ bytes)

(deftest rlp-long-string
  (testing "long strings (56+ bytes) encode with length-of-length prefix"
    (let [data (byte-array 56 (byte 42))
          encoded (encode data)]
      (is (= (unchecked-byte 0xb8) (aget encoded 0)))  ; 0xb7 + 1 byte for length
      (is (= 56 (bit-and 0xff (aget encoded 1))))
      (is (= 58 (alength encoded))))
    (let [data (byte-array 256 (byte 42))
          encoded (encode data)]
      (is (= (unchecked-byte 0xb9) (aget encoded 0)))  ; 0xb7 + 2 bytes for length
      (is (= 259 (alength encoded))))))

;; String encoding

(deftest rlp-string-encoding
  (testing "strings encode as bytes"
    (let [encoded (encode "hello")]
      (is (= (+ 0x80 5) (bit-and 0xff (aget encoded 0))))
      (is (= 6 (alength encoded))))))

;; Vector encoding

(deftest rlp-empty-vector
  (testing "empty vector"
    (let [encoded (encode [])]
      (is (= 1 (alength encoded)))
      (is (= (unchecked-byte 0xc0) (aget encoded 0))))))

(deftest rlp-short-vector
  (testing "short vector (total < 55 bytes)"
    (let [encoded (encode [(byte-array [1]) (byte-array [2])])]
      (is (= (unchecked-byte 0xc2) (aget encoded 0)))
      (is (= 3 (alength encoded))))))

(deftest rlp-nested-vector
  (testing "nested vectors"
    (let [encoded (encode [[(byte-array [1])] [(byte-array [2])]])]
      (is (vector? encoded)))))

;; Decode tests

(deftest rlp-decode-nil
  (testing "decode nil/empty string"
    (let [decoded (decode (byte-array [0x80]))]
      (is (nil? decoded)))))

(deftest rlp-decode-single-byte
  (testing "decode single byte"
    (let [^bytes decoded (decode (byte-array [42]))]
      (is (= 1 (alength decoded)))
      (is (= 42 (aget decoded 0))))))

(deftest rlp-decode-short-string
  (testing "decode short string"
    (let [original (byte-array [1 2 3])
          encoded (encode original)
          ^bytes decoded (decode encoded)]
      (is (Arrays/equals original decoded)))))

(deftest rlp-decode-long-string
  (testing "decode long string"
    (let [original (byte-array 100 (byte 42))
          encoded (encode original)
          ^bytes decoded (decode encoded)]
      (is (Arrays/equals original decoded)))))

(deftest rlp-decode-vector
  (testing "decode vector"
    (let [decoded (decode (byte-array [0xc2 1 2]))]
      (is (clojure.core/vector? decoded))
      (is (= 2 (count decoded))))))

;; Round-trip tests

(deftest rlp-roundtrip
  (testing "encode/decode roundtrip for bytes"
    ;; Note: empty byte array encodes to 0x80 which decodes to nil
    (doseq [^bytes data [(byte-array [0])
                         (byte-array [127])
                         (byte-array [128])
                         (byte-array [1 2 3])
                         (byte-array 55 (byte 42))
                         (byte-array 56 (byte 42))
                         (byte-array 256 (byte 42))]]
      (let [encoded (encode data)
            ^bytes decoded (decode encoded)]
        (is (Arrays/equals data decoded)))))
  (testing "empty byte array roundtrip"
    (let [encoded (encode (byte-array []))
          decoded (decode encoded)]
      (is (nil? decoded)))))

(deftest rlp-roundtrip-string
  (testing "encode/decode roundtrip for strings"
    ;; Note: empty string encodes to 0x80 which decodes to nil
    (doseq [s ["a" "hello" (apply str (repeat 100 "x"))]]
      (let [encoded (encode s)
            decoded (decode encoded)]
        (is (= s (String. ^bytes decoded))))))
  (testing "empty string roundtrip"
    (let [encoded (encode "")
          decoded (decode encoded)]
      (is (nil? decoded)))))

;; vector? predicate tests

(deftest rlp-vector-predicate
  (testing "vector? returns true for vectors"
    (is (vector? (encode [])))
    (is (vector? (encode [[]])))
    (is (vector? (encode [(byte-array [1])]))))
  (testing "vector? returns false for non-vectors"
    (is (not (vector? (encode nil))))
    (is (not (vector? (encode (byte-array [1 2 3])))))
    (is (not (vector? (encode "hello"))))))

;; Edge cases

(deftest rlp-edge-cases
  (testing "very long data"
    (let [data (byte-array 10000 (byte 42))
          encoded (encode data)
          ^bytes decoded (decode encoded)]
      (is (Arrays/equals data decoded)))))

(deftest rlp-decode-nil-input
  (testing "decode throws on nil input"
    (is (thrown? Exception (decode nil)))))

(deftest rlp-decode-empty-input
  (testing "decode throws on empty input"
    (is (thrown? Exception (decode (byte-array []))))))

(deftest rlp-empty-vector-roundtrip
  (testing "empty vector decodes back to an empty vector, not nil"
    (is (= [] (decode (encode []))))
    (is (= [] (decode (byte-array [0xc0]))))))

(deftest rlp-string-utf8
  (testing "strings encode as UTF-8 regardless of platform charset"
    (let [s "héllo中文"
          payload ^bytes (decode (encode s))]
      (is (Arrays/equals (.getBytes s java.nio.charset.StandardCharsets/UTF_8) payload))
      (is (= s (String. payload java.nio.charset.StandardCharsets/UTF_8))))))

(deftest rlp-decode-truncated
  (testing "decode throws on truncated input instead of fabricating zero bytes"
    ;; claims 3 payload bytes, only 2 present
    (is (thrown? clojure.lang.ExceptionInfo (decode (byte-array [0x83 1 2]))))
    ;; long form claims 256 payload bytes, none present
    (is (thrown? clojure.lang.ExceptionInfo (decode (byte-array [0xb9 0x01 0x00]))))
    ;; length-of-length bytes themselves missing
    (is (thrown? clojure.lang.ExceptionInfo (decode (byte-array [0xb9 0x01]))))
    ;; vector element claims more bytes than the payload holds
    (is (thrown? clojure.lang.ExceptionInfo (decode (byte-array [0xc2 0x82 0x01]))))))

(deftest rlp-decode-non-canonical
  (testing "single byte below 0x80 must be encoded as itself"
    (is (thrown? clojure.lang.ExceptionInfo (decode (byte-array [0x81 0x05])))))
  (testing "a prefixed byte >= 0x80 is canonical and accepted"
    (is (= 1 (alength ^bytes (decode (byte-array [0x81 0x80]))))))
  (testing "long form must not be used for lengths <= 55"
    (is (thrown? clojure.lang.ExceptionInfo
                 (decode (byte-array (into [0xb8 0x03] (repeat 3 1)))))))
  (testing "length bytes must not have leading zeros"
    (is (thrown? clojure.lang.ExceptionInfo
                 (decode (byte-array (into [0xb9 0x00 0x38] (repeat 56 1))))))))

(deftest rlp-decode-trailing-bytes
  (testing "decode throws when input has bytes after the first item"
    (is (thrown? clojure.lang.ExceptionInfo (decode (byte-array [0x01 0x02]))))
    (is (thrown? clojure.lang.ExceptionInfo (decode (byte-array [0x80 0x00]))))
    (is (thrown? clojure.lang.ExceptionInfo (decode (byte-array [0xc2 1 2 3]))))))
