(ns blockchain-misc-clj.codec.bson
  (:require [jsonista.core :as j]
            [clojure.string :as str])
  (:import (ai.z7.blockchain_misc.Binary Bson)
           (de.undercouch.bson4jackson BsonFactory)))

(defn encode ^bytes [m]
  (when (nil? m)
    (throw (IllegalArgumentException. "Input map cannot be nil.")))
  (when-not (map? m)
    (throw (IllegalArgumentException. "Input must be a map.")))
  (Bson/encodeObject m))

(defn decode* ^Object [^bytes b]
  (Bson/decodeObject b Object))

(defn- keywordize [v]
  (keyword
    (if (and (string? v) (str/starts-with? v ":"))
      (subs v 1) v)))

(def ^:private bson-mapper
  (j/object-mapper {:factory       (BsonFactory.)
                    :decode-key-fn keywordize}))

(defn decode [^bytes b]
  (when (nil? b)
    (throw (IllegalArgumentException. "Input bytes cannot be nil.")))
  (j/read-value b bson-mapper))
