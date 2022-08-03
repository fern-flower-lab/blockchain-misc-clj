(ns blockchain-misc-clj.codec.bson
  (:require [jsonista.core :as j]
            [clojure.string :as str])
  (:import (ai.z7.blockchain_misc.Binary Bson)
           (potemkin PersistentMapProxy$IMap)
           (clojure.lang PersistentHashMap)))

(defn encode ^bytes [^PersistentMapProxy$IMap m]
  (Bson/encodeObject m))

(defn decode* ^Object [^bytes b]
  (Bson/decodeObject b Object))

(defn- keywordize [v]
  (keyword
    (if (and (string? v) (str/starts-with? v ":"))
      (str/replace-first v ":" "") v)))

(defn- convert [m]
  (j/read-value (j/write-value-as-bytes m)
                (j/object-mapper {:decode-key-fn keywordize})))

(defn decode ^PersistentHashMap [^bytes b]
  (-> b decode* convert))
