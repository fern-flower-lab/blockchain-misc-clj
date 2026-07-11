(ns blockchain-misc-clj.codec.rlp
  (:require [blockchain-misc-clj.codec.utils :refer [bytes->long long->bytes byte-count]])
  (:import (java.io ByteArrayOutputStream)
           (java.nio.charset StandardCharsets)
           (java.util Arrays)
           (clojure.lang IPersistentVector))
  (:refer-clojure :exclude [vector?]))

(def ^:private cutoff 55)
(def ^:private str-short 0x80)
(def ^:private str-long (+ str-short cutoff))
(def ^:private seq-short 0xc0)
(def ^:private seq-long (+ seq-short cutoff))

(defprotocol RLP
  (stream-encode [this stream opts]))

(defn- compact? [bs]
  (and (= (count bs) 1)
       (< (bit-and (first bs) 0xff) str-short)))

(defn- write-long [^ByteArrayOutputStream stream l]
  (let [^bytes bs (long->bytes l)]
    (.write stream bs 0 (alength bs))))

(defn- write-prefix [^ByteArrayOutputStream stream cnt magic]
  (if (<= cnt cutoff)
    (.write stream (int (+ cnt magic)))
    (do
      (write-long stream (+ (byte-count cnt) magic cutoff))
      (write-long stream cnt))))

(extend-type nil
  RLP
  (stream-encode [_ stream opts]
    (write-prefix stream 0 str-short)))

(extend-type (type (byte-array 0))
  RLP
  (stream-encode [bs stream opts]
    (let [cnt (alength ^bytes bs)]
      (cond (:raw? opts) (.write ^ByteArrayOutputStream stream ^bytes bs 0 cnt)
            (compact? bs) (.write ^ByteArrayOutputStream stream ^bytes bs 0 1)
            :else
            (do (write-prefix stream cnt str-short)
                (.write ^ByteArrayOutputStream stream ^bytes bs 0 cnt))))))

(extend-protocol RLP
  String
  (stream-encode [x stream opts]
    (stream-encode (.getBytes x StandardCharsets/UTF_8) stream opts))
  IPersistentVector
  (stream-encode [xs stream opts]
    (let [stream' (ByteArrayOutputStream.)]
      (doseq [x xs]
        (stream-encode x stream' opts))
      (write-prefix stream (.size stream') seq-short)
      (.writeTo stream' ^ByteArrayOutputStream stream))))

(defn encode ^bytes [x & [opts]]
  (let [stream (ByteArrayOutputStream.)]
    (stream-encode x stream opts)
    (.toByteArray stream)))

(defn- check-payload [^bytes bs start width]
  (when (or (neg? width) (< (- (alength bs) start) width))
    (throw (ex-info "Truncated RLP input."
                    {:available (- (alength bs) start) :needed width}))))

(defn- read-wide-bounds [^bytes bs i width-width]
  (check-payload bs (inc i) width-width)
  (when (zero? (aget bs (inc i)))
    (throw (ex-info "Non-canonical RLP: length has a leading zero byte."
                    {:offset (inc i)})))
  (let [start (+ i width-width 1)
        width (bytes->long bs (inc i) width-width)]
    (when (<= width cutoff)
      (throw (ex-info "Non-canonical RLP: long form used for a short length."
                      {:length width})))
    (check-payload bs start width)
    [start width]))

(defn- read-bounds [^bytes bs i]
  (let [c (bit-and 0xff (aget bs i))]
    (cond (< c str-short) [i 1]
          (<= c str-long)
          (let [start (inc i)
                width (- c str-short)]
            (check-payload bs start width)
            (when (and (= width 1) (< (bit-and 0xff (aget bs start)) str-short))
              (throw (ex-info "Non-canonical RLP: single byte below 0x80 must be encoded as itself."
                              {:offset i})))
            [start width])
          (< c seq-short) (read-wide-bounds bs i (- c str-long))
          (<= c seq-long)
          (let [start (inc i)
                width (- c seq-short)]
            (check-payload bs start width)
            [start width])
          :else (read-wide-bounds bs i (- c seq-long)))))

(defn- split-vector [^bytes bs]
  (let [n (alength bs)]
    (loop [i 0, acc []]
      (if (<= n i)
        acc
        (let [[start width] (read-bounds bs i)
              end (+ start width)]
          (recur (long end) (conj acc (Arrays/copyOfRange bs (int i) (int end)))))))))

(defn vector? [bs]
  (<= seq-short (bit-and 0xff (aget ^bytes bs 0))))

(defn decode [bs]
  (when (nil? bs)
    (throw (IllegalArgumentException. "Input byte array cannot be nil.")))
  (when (zero? (alength ^bytes bs))
    (throw (ex-info "Empty RLP input." {})))
  (let [[start width] (read-bounds bs 0)
        end (+ start width)]
    (when-not (= end (alength ^bytes bs))
      (throw (ex-info "Trailing bytes after RLP item."
                      {:consumed end :length (alength ^bytes bs)})))
    (if (vector? bs)
      (split-vector (Arrays/copyOfRange ^bytes bs (int start) (int end)))
      (when-not (zero? width)
        (Arrays/copyOfRange ^bytes bs (int start) (int end))))))
