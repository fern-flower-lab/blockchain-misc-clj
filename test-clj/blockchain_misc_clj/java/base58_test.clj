(ns blockchain-misc-clj.java.base58-test
  (:require [clojure.test :refer [deftest testing is]])
  (:import (ai.z7.blockchain_misc Base58)
           (java.io IOException)))

(deftest base58-roundtrip
  (testing "encode/decode round-trip consistency"
    (let [test-data [(.getBytes "hello")
                     (.getBytes "Hello World")
                     (byte-array [1 2 3 4 5])
                     (byte-array (range 256))]]
      (doseq [data test-data]
        (is (java.util.Arrays/equals data
                                     (Base58/decode (Base58/encode data))))))))

(deftest base58-empty-input
  (testing "empty byte array encodes to empty string"
    (is (= "" (Base58/encode (byte-array 0)))))
  (testing "empty string decodes to empty byte array"
    (is (= 0 (alength (Base58/decode ""))))))

(deftest base58-leading-zeros
  (testing "leading zeros are preserved"
    (let [data (byte-array [0 0 0 1 2 3])]
      (is (java.util.Arrays/equals data
                                   (Base58/decode (Base58/encode data)))))
    (let [data (byte-array [0 0 0 0 0])]
      (is (java.util.Arrays/equals data
                                   (Base58/decode (Base58/encode data)))))))

(deftest base58-invalid-characters
  (testing "invalid characters throw IOException"
    (is (thrown? IOException (Base58/decode "0")))
    (is (thrown? IOException (Base58/decode "O")))
    (is (thrown? IOException (Base58/decode "I")))
    (is (thrown? IOException (Base58/decode "l")))
    (is (thrown? IOException (Base58/decode "hello!")))))

(deftest base58-known-vectors
  (testing "known test vectors (Bitcoin-style)"
    ;; "Hello World" in Base58
    (is (= "JxF12TrwUP45BMd" (Base58/encode (.getBytes "Hello World"))))
    ;; Single byte values
    (is (= "1" (Base58/encode (byte-array [0]))))
    (is (= "2" (Base58/encode (byte-array [1]))))
    ;; Verify decode
    (is (java.util.Arrays/equals (.getBytes "Hello World")
                                 (Base58/decode "JxF12TrwUP45BMd")))))

(deftest base58-alphabet
  (testing "all alphabet characters are valid"
    (let [alphabet "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"]
      (doseq [c alphabet]
        (is (some? (Base58/decode (str c))))))))

(deftest base58-large-input
  (testing "handles large input"
    (let [data (byte-array 1000 (byte 42))]
      (is (java.util.Arrays/equals data
                                   (Base58/decode (Base58/encode data)))))))
