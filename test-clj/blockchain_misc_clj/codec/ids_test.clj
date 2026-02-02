(ns blockchain-misc-clj.codec.ids-test
  (:require [clojure.test :refer [deftest testing is]]
            [blockchain-misc-clj.codec.ids :refer [tuid tuid->timestamp tuid-cmp]])
  (:import (java.util UUID)))

;; tuid generation tests

(deftest tuid-generates-uuid
  (testing "tuid returns a UUID"
    (is (instance? UUID (tuid)))))

(deftest tuid-generates-v1-uuid
  (testing "tuid returns version 1 UUID"
    (is (= 1 (.version (tuid))))))

(deftest tuid-uniqueness
  (testing "sequential tuids are unique"
    (let [ids (repeatedly 100 tuid)]
      (is (= 100 (count (set ids)))))))

;; tuid->timestamp tests

(deftest tuid->timestamp-extracts-timestamp
  (testing "tuid->timestamp extracts correct timestamp"
    (let [before (System/currentTimeMillis)
          id (tuid)
          after (System/currentTimeMillis)
          ts (tuid->timestamp id)]
      (is (<= before ts after)))))

(deftest tuid->timestamp-nil-handling
  (testing "tuid->timestamp returns nil for nil"
    (is (nil? (tuid->timestamp nil)))))

(deftest tuid->timestamp-invalid-version
  (testing "tuid->timestamp returns nil for non-v1 UUID"
    (is (nil? (tuid->timestamp (UUID/randomUUID))))))

;; tuid-cmp tests

(deftest tuid-cmp-ordering
  (testing "tuid-cmp orders by timestamp"
    (let [t1 (tuid)
          _ (Thread/sleep 2)
          t2 (tuid)]
      (is (neg? (tuid-cmp t1 t2)))
      (is (pos? (tuid-cmp t2 t1)))
      (is (zero? (tuid-cmp t1 t1))))))

(deftest tuid-cmp-sorting
  (testing "tuids sort correctly with tuid-cmp"
    (let [ids (vec (repeatedly 10 #(do (Thread/sleep 1) (tuid))))
          shuffled (shuffle ids)
          sorted (sort tuid-cmp shuffled)]
      (is (= ids sorted)))))

(deftest tuid-cmp-nil-handling
  (testing "tuid-cmp handles nil"
    (is (thrown? NullPointerException (tuid-cmp nil (tuid))))
    (is (thrown? NullPointerException (tuid-cmp (tuid) nil)))))

(deftest tuid-cmp-non-v1-uuids
  (testing "tuid-cmp handles non-v1 UUIDs"
    (let [v4-1 (UUID/randomUUID)
          v4-2 (UUID/randomUUID)]
      ;; Should not throw, uses standard compareTo
      (is (integer? (tuid-cmp v4-1 v4-2))))))

(deftest tuid-cmp-mixed-versions
  (testing "tuid-cmp handles mixed UUID versions"
    (let [v1 (tuid)
          v4 (UUID/randomUUID)]
      ;; Should not throw
      (is (integer? (tuid-cmp v1 v4)))
      (is (integer? (tuid-cmp v4 v1))))))
