(ns blockchain-misc-clj.java.tuid-test
  (:require [clojure.test :refer [deftest testing is]])
  (:import (ai.z7.blockchain_misc TUID TUID$TUIDComparator)
           (java.util UUID)
           (java.util.concurrent CountDownLatch Executors TimeUnit)))

(deftest tuid-generates-v1-uuid
  (testing "timeUID generates version 1 UUID"
    (let [uuid (TUID/timeUID)]
      (is (instance? UUID uuid))
      (is (= 1 (.version uuid))))))

(deftest tuid-uniqueness
  (testing "sequential TUIDs are unique"
    (let [tuids (repeatedly 100 #(TUID/timeUID))]
      (is (= 100 (count (set tuids)))))))

(deftest tuid-timestamp-extraction
  (testing "toTimestamp extracts correct timestamps"
    (let [before (System/currentTimeMillis)
          uuid (TUID/timeUID)
          after (System/currentTimeMillis)
          ts (TUID/toTimestamp uuid)]
      (is (<= before ts after)))))

(deftest tuid-monotonicity
  (testing "sequential TUIDs have non-decreasing timestamps"
    (let [tuids (repeatedly 100 #(TUID/timeUID))
          timestamps (map #(TUID/toTimestamp %) tuids)]
      (is (= timestamps (sort timestamps))))))

(deftest tuid-invalid-version-handling
  (testing "toTimestamp throws for non-v1 UUIDs"
    (let [v4-uuid (UUID/randomUUID)]
      (is (thrown? IllegalArgumentException (TUID/toTimestamp v4-uuid)))))
  (testing "toTimestamp throws for nil"
    (is (thrown? IllegalArgumentException (TUID/toTimestamp nil)))))

(deftest tuid-comparator
  (testing "TUIDComparator compares v1 UUIDs by timestamp"
    (let [cmp (TUID$TUIDComparator.)
          u1 (TUID/timeUID)
          _ (Thread/sleep 2)
          u2 (TUID/timeUID)]
      (is (neg? (.compare cmp u1 u2)))
      (is (pos? (.compare cmp u2 u1)))
      (is (zero? (.compare cmp u1 u1)))))
  (testing "TUIDComparator handles non-v1 UUIDs"
    (let [cmp (TUID$TUIDComparator.)
          v4-1 (UUID/randomUUID)
          v4-2 (UUID/randomUUID)]
      (is (integer? (.compare cmp v4-1 v4-2))))))

(deftest tuid-thread-safety
  (testing "concurrent TUID generation produces unique values"
    (let [n-threads 8
          n-per-thread 100
          results (java.util.concurrent.ConcurrentHashMap.)
          latch (CountDownLatch. n-threads)
          executor (Executors/newFixedThreadPool n-threads)]
      (dotimes [_ n-threads]
        (.submit executor
                 ^Runnable (fn []
                             (dotimes [_ n-per-thread]
                               (.put results (TUID/timeUID) true))
                             (.countDown latch))))
      (.await latch 10 TimeUnit/SECONDS)
      (.shutdown executor)
      (is (= (* n-threads n-per-thread) (.size results))))))
