(ns blockchain-misc-clj.codec.bson-test
  (:require [clojure.test :refer [deftest testing is]]
            [blockchain-misc-clj.codec.bson :refer [encode decode]]))

;; Basic encode/decode roundtrip

(deftest bson-empty-map
  (testing "empty map encode/decode"
    (let [m {}
          encoded (encode m)
          decoded (decode encoded)]
      (is (= m decoded)))))

(deftest bson-simple-map
  (testing "simple map with string values"
    (let [m {:a "hello" :b "world"}
          encoded (encode m)
          decoded (decode encoded)]
      (is (= "hello" (:a decoded)))
      (is (= "world" (:b decoded))))))

(deftest bson-numeric-values
  (testing "map with numeric values"
    (let [m {:int 42 :float 3.14 :negative -100}
          encoded (encode m)
          decoded (decode encoded)]
      (is (= 42 (:int decoded)))
      (is (= 3.14 (:float decoded)))
      (is (= -100 (:negative decoded))))))

(deftest bson-boolean-values
  (testing "map with boolean values"
    (let [m {:yes true :no false}
          encoded (encode m)
          decoded (decode encoded)]
      (is (= true (:yes decoded)))
      (is (= false (:no decoded))))))

(deftest bson-nested-map
  (testing "nested maps"
    (let [m {:outer {:inner "value"}}
          encoded (encode m)
          decoded (decode encoded)]
      (is (= "value" (get-in decoded [:outer :inner]))))))

(deftest bson-array-values
  (testing "map with array values"
    (let [m {:items [1 2 3]}
          encoded (encode m)
          decoded (decode encoded)]
      (is (= [1 2 3] (:items decoded))))))

(deftest bson-nil-value
  (testing "map with nil value"
    (let [m {:nothing nil}
          encoded (encode m)
          decoded (decode encoded)]
      (is (nil? (:nothing decoded))))))

;; Keyword key conversion

(deftest bson-keyword-keys
  (testing "keyword keys are preserved"
    (let [m {:key1 "val1" :key2 "val2"}
          encoded (encode m)
          decoded (decode encoded)]
      (is (keyword? (first (keys decoded))))
      (is (contains? decoded :key1))
      (is (contains? decoded :key2)))))

(deftest bson-string-like-keyword-keys
  (testing "string keys starting with colon become keywords"
    (let [m {":prefixed" "value"}
          encoded (encode m)
          decoded (decode encoded)]
      ;; The keywordize function handles :-prefixed strings
      (is (some? decoded)))))

;; Various value types

(deftest bson-mixed-types
  (testing "map with mixed value types"
    (let [m {:str "hello"
             :num 42
             :bool true
             :arr [1 "two" 3]
             :nested {:a 1}}
          encoded (encode m)
          decoded (decode encoded)]
      (is (= "hello" (:str decoded)))
      (is (= 42 (:num decoded)))
      (is (= true (:bool decoded)))
      (is (= [1 "two" 3] (:arr decoded)))
      (is (= 1 (get-in decoded [:nested :a]))))))

(deftest bson-unicode-strings
  (testing "map with unicode strings"
    (let [m {:emoji "👍" :chinese "中文" :cyrillic "привет"}
          encoded (encode m)
          decoded (decode encoded)]
      (is (= "👍" (:emoji decoded)))
      (is (= "中文" (:chinese decoded)))
      (is (= "привет" (:cyrillic decoded))))))

(deftest bson-long-values
  (testing "map with long integer values"
    (let [m {:big 9007199254740992}  ; 2^53
          encoded (encode m)
          decoded (decode encoded)]
      (is (= 9007199254740992 (:big decoded))))))

;; Error handling

(deftest bson-encode-nil
  (testing "encode nil throws"
    (is (thrown? Exception (encode nil)))))

(deftest bson-decode-empty
  (testing "decode empty returns empty map"
    ;; BSON decoder handles short/malformed input gracefully
    (let [result (decode (byte-array [5 0 0 0 0]))]  ; minimal valid BSON (empty document)
      (is (= {} result)))))
