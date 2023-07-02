(ns blockchain-misc-clj.codec.ids
  (:import (ai.z7.blockchain_misc TUID TUID$TUIDComparator)
           (java.util UUID)))

(defn tuid ^UUID [] (TUID/timeUID))

(defn tuid->timestamp ^Long [^UUID tuid]
  (try (TUID/toTimestamp tuid)
       (catch IllegalArgumentException _ nil)))

(def ^:private tuid-comparator (TUID$TUIDComparator.))
(defn tuid-cmp [^UUID t1 ^UUID t2]
  (.compare tuid-comparator t1 t2))

