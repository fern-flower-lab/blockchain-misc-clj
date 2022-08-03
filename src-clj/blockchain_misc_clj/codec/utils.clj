(ns blockchain-misc-clj.codec.utils)

(defn byte-count [l]
  (int (Math/ceil (/ (- 64 (Long/numberOfLeadingZeros l)) 8))))

(defn bytes->long
  ([bs]
   (bytes->long bs 0 (alength ^bytes bs)))
  ([bs i n]
   (loop [out 0, i i, n n]
     (if (zero? n)
       out
       (recur (bit-or (bit-shift-left out 8)
                      (bit-and 0xff (aget ^bytes bs i)))
              (inc i)
              (dec n))))))

(defn long->bytes [l]
  (let [x (byte-count l)
        out (byte-array x)]
    (loop [x (dec x), l l]
      (if (neg? x)
        out
        (do
          (aset out x (unchecked-byte (bit-and l 0xff)))
          (recur (dec x) (bit-shift-right l 8)))))))
