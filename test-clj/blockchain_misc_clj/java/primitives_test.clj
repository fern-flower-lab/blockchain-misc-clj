(ns blockchain-misc-clj.java.primitives-test
  (:require [clojure.test :refer [deftest testing is]])
  (:import (ai.z7.blockchain_misc.Binary Primitives)
           (java.io ByteArrayInputStream ByteArrayOutputStream)))

(defn- write-read-varint32 [value]
  (let [out (ByteArrayOutputStream.)]
    (Primitives/writeVarint32 out value)
    (let [in (ByteArrayInputStream. (.toByteArray out))]
      (Primitives/readVarint32 in))))

(defn- write-read-varint64 [value]
  (let [out (ByteArrayOutputStream.)]
    (Primitives/writeVarint64 out value)
    (let [in (ByteArrayInputStream. (.toByteArray out))]
      (Primitives/readVarint64 in))))

;; Boolean tests

(deftest primitives-boolean
  (testing "boolean write/read"
    (let [out (ByteArrayOutputStream.)]
      (Primitives/writeBoolean out true)
      (let [in (ByteArrayInputStream. (.toByteArray out))]
        (is (true? (Primitives/readBoolean in)))))
    (let [out (ByteArrayOutputStream.)]
      (Primitives/writeBoolean out false)
      (let [in (ByteArrayInputStream. (.toByteArray out))]
        (is (false? (Primitives/readBoolean in)))))))

;; Varint32 tests

(deftest primitives-varint32-small-values
  (testing "varint32 small values (single byte)"
    (is (= 0 (write-read-varint32 0)))
    (is (= 1 (write-read-varint32 1)))
    (is (= 127 (write-read-varint32 127)))))

(deftest primitives-varint32-boundaries
  (testing "varint32 boundary values"
    (is (= 128 (write-read-varint32 128)))
    (is (= 16383 (write-read-varint32 16383)))
    (is (= 16384 (write-read-varint32 16384)))
    (is (= 2097151 (write-read-varint32 2097151)))
    (is (= 2097152 (write-read-varint32 2097152)))
    (is (= 268435455 (write-read-varint32 268435455)))
    (is (= 268435456 (write-read-varint32 268435456)))))

(deftest primitives-varint32-max
  (testing "varint32 max value"
    (is (= Integer/MAX_VALUE (write-read-varint32 Integer/MAX_VALUE)))))

(deftest primitives-varint32-negative
  (testing "varint32 negative values"
    ;; Negative values are sign-extended to 64 bits
    (is (= -1 (write-read-varint32 -1)))))

;; Varint64 tests

(deftest primitives-varint64-small-values
  (testing "varint64 small values"
    (is (= 0 (write-read-varint64 0)))
    (is (= 1 (write-read-varint64 1)))
    (is (= 127 (write-read-varint64 127)))))

(deftest primitives-varint64-boundaries
  (testing "varint64 boundary values"
    (is (= 128 (write-read-varint64 128)))
    (is (= 16384 (write-read-varint64 16384)))
    (is (= (long (Math/pow 2 21)) (write-read-varint64 (long (Math/pow 2 21)))))
    (is (= (long (Math/pow 2 28)) (write-read-varint64 (long (Math/pow 2 28)))))
    (is (= (long (Math/pow 2 35)) (write-read-varint64 (long (Math/pow 2 35)))))
    (is (= (long (Math/pow 2 42)) (write-read-varint64 (long (Math/pow 2 42)))))))

(deftest primitives-varint64-max
  (testing "varint64 max value"
    (is (= Long/MAX_VALUE (write-read-varint64 Long/MAX_VALUE)))))

(deftest primitives-varint64-negative
  (testing "varint64 negative values"
    (is (= -1 (write-read-varint64 -1)))))

;; ZigZag encoding tests

(deftest primitives-zigzag32
  (testing "zigzag32 encoding"
    (is (= 0 (Primitives/encodeZigZag32 0)))
    (is (= 1 (Primitives/encodeZigZag32 -1)))
    (is (= 2 (Primitives/encodeZigZag32 1)))
    (is (= 3 (Primitives/encodeZigZag32 -2)))
    (is (= 4 (Primitives/encodeZigZag32 2))))
  (testing "zigzag32 decoding"
    (is (= 0 (Primitives/decodeZigZag32 0)))
    (is (= -1 (Primitives/decodeZigZag32 1)))
    (is (= 1 (Primitives/decodeZigZag32 2)))
    (is (= -2 (Primitives/decodeZigZag32 3)))
    (is (= 2 (Primitives/decodeZigZag32 4))))
  (testing "zigzag32 roundtrip"
    (doseq [v [0 1 -1 100 -100 Integer/MAX_VALUE Integer/MIN_VALUE]]
      (is (= v (Primitives/decodeZigZag32 (Primitives/encodeZigZag32 v)))))))

(deftest primitives-zigzag64
  (testing "zigzag64 encoding"
    (is (= 0 (Primitives/encodeZigZag64 0)))
    (is (= 1 (Primitives/encodeZigZag64 -1)))
    (is (= 2 (Primitives/encodeZigZag64 1))))
  (testing "zigzag64 decoding"
    (is (= 0 (Primitives/decodeZigZag64 0)))
    (is (= -1 (Primitives/decodeZigZag64 1)))
    (is (= 1 (Primitives/decodeZigZag64 2))))
  (testing "zigzag64 roundtrip"
    (doseq [v [0 1 -1 100 -100 Long/MAX_VALUE Long/MIN_VALUE]]
      (is (= v (Primitives/decodeZigZag64 (Primitives/encodeZigZag64 v)))))))

;; Signed varint tests

(deftest primitives-signed32
  (testing "signed32 roundtrip"
    (let [write-read (fn [v]
                       (let [out (ByteArrayOutputStream.)]
                         (Primitives/writeSigned32 out v)
                         (let [in (ByteArrayInputStream. (.toByteArray out))]
                           (Primitives/readSigned32 in))))]
      (is (= 0 (write-read 0)))
      (is (= 1 (write-read 1)))
      (is (= -1 (write-read -1)))
      (is (= 100 (write-read 100)))
      (is (= -100 (write-read -100)))
      (is (= Integer/MAX_VALUE (write-read Integer/MAX_VALUE)))
      (is (= Integer/MIN_VALUE (write-read Integer/MIN_VALUE))))))

(deftest primitives-signed64
  (testing "signed64 roundtrip"
    (let [write-read (fn [v]
                       (let [out (ByteArrayOutputStream.)]
                         (Primitives/writeSigned64 out v)
                         (let [in (ByteArrayInputStream. (.toByteArray out))]
                           (Primitives/readSigned64 in))))]
      (is (= 0 (write-read 0)))
      (is (= 1 (write-read 1)))
      (is (= -1 (write-read -1)))
      (is (= Long/MAX_VALUE (write-read Long/MAX_VALUE)))
      (is (= Long/MIN_VALUE (write-read Long/MIN_VALUE))))))

;; Fixed32/64 tests

(deftest primitives-fixed32
  (testing "fixed32 roundtrip"
    (let [write-read (fn [v]
                       (let [out (ByteArrayOutputStream.)]
                         (Primitives/writeFixed32 out v)
                         (let [in (ByteArrayInputStream. (.toByteArray out))]
                           (Primitives/readFixed32 in))))]
      (is (= 0 (write-read 0)))
      (is (= 1 (write-read 1)))
      (is (= -1 (write-read -1)))
      (is (= Integer/MAX_VALUE (write-read Integer/MAX_VALUE)))
      (is (= Integer/MIN_VALUE (write-read Integer/MIN_VALUE)))
      (is (= 0x12345678 (write-read 0x12345678))))))

(deftest primitives-fixed64
  (testing "fixed64 roundtrip"
    (let [write-read (fn [v]
                       (let [out (ByteArrayOutputStream.)]
                         (Primitives/writeFixed64 out v)
                         (let [in (ByteArrayInputStream. (.toByteArray out))]
                           (Primitives/readFixed64 in))))]
      (is (= 0 (write-read 0)))
      (is (= 1 (write-read 1)))
      (is (= -1 (write-read -1)))
      (is (= Long/MAX_VALUE (write-read Long/MAX_VALUE)))
      (is (= Long/MIN_VALUE (write-read Long/MIN_VALUE)))
      (is (= 0x123456789ABCDEF0 (write-read 0x123456789ABCDEF0))))))

;; Float/Double tests

(deftest primitives-float
  (testing "float roundtrip"
    (let [write-read (fn [v]
                       (let [out (ByteArrayOutputStream.)]
                         (Primitives/writeFloat out v)
                         (let [in (ByteArrayInputStream. (.toByteArray out))]
                           (Primitives/readFloat in))))]
      (is (= (float 0.0) (write-read (float 0.0))))
      (is (= (float 1.5) (write-read (float 1.5))))
      (is (= (float -1.5) (write-read (float -1.5))))
      (is (= Float/MAX_VALUE (write-read Float/MAX_VALUE)))
      (is (= Float/MIN_VALUE (write-read Float/MIN_VALUE))))))

(deftest primitives-float-special-values
  (testing "float special values"
    (let [write-read (fn [v]
                       (let [out (ByteArrayOutputStream.)]
                         (Primitives/writeFloat out v)
                         (let [in (ByteArrayInputStream. (.toByteArray out))]
                           (Primitives/readFloat in))))]
      (is (Float/isNaN (write-read Float/NaN)))
      (is (= Float/POSITIVE_INFINITY (write-read Float/POSITIVE_INFINITY)))
      (is (= Float/NEGATIVE_INFINITY (write-read Float/NEGATIVE_INFINITY))))))

(deftest primitives-double
  (testing "double roundtrip"
    (let [write-read (fn [v]
                       (let [out (ByteArrayOutputStream.)]
                         (Primitives/writeDouble out v)
                         (let [in (ByteArrayInputStream. (.toByteArray out))]
                           (Primitives/readDouble in))))]
      (is (= 0.0 (write-read 0.0)))
      (is (= 1.5 (write-read 1.5)))
      (is (= -1.5 (write-read -1.5)))
      (is (= Double/MAX_VALUE (write-read Double/MAX_VALUE)))
      (is (= Double/MIN_VALUE (write-read Double/MIN_VALUE))))))

(deftest primitives-double-special-values
  (testing "double special values"
    (let [write-read (fn [v]
                       (let [out (ByteArrayOutputStream.)]
                         (Primitives/writeDouble out v)
                         (let [in (ByteArrayInputStream. (.toByteArray out))]
                           (Primitives/readDouble in))))]
      (is (Double/isNaN (write-read Double/NaN)))
      (is (= Double/POSITIVE_INFINITY (write-read Double/POSITIVE_INFINITY)))
      (is (= Double/NEGATIVE_INFINITY (write-read Double/NEGATIVE_INFINITY))))))

;; Int32/Int64/Unsigned32/Unsigned64 aliases

(deftest primitives-int32
  (testing "int32 uses varint encoding"
    (let [write-read (fn [v]
                       (let [out (ByteArrayOutputStream.)]
                         (Primitives/writeInt32 out v)
                         (let [in (ByteArrayInputStream. (.toByteArray out))]
                           (Primitives/readInt32 in))))]
      (is (= 0 (write-read 0)))
      (is (= 127 (write-read 127)))
      (is (= Integer/MAX_VALUE (write-read Integer/MAX_VALUE))))))

(deftest primitives-int64
  (testing "int64 uses varint encoding"
    (let [write-read (fn [v]
                       (let [out (ByteArrayOutputStream.)]
                         (Primitives/writeInt64 out v)
                         (let [in (ByteArrayInputStream. (.toByteArray out))]
                           (Primitives/readInt64 in))))]
      (is (= 0 (write-read 0)))
      (is (= Long/MAX_VALUE (write-read Long/MAX_VALUE))))))

(deftest primitives-unsigned32
  (testing "unsigned32 uses varint encoding"
    (let [write-read (fn [v]
                       (let [out (ByteArrayOutputStream.)]
                         (Primitives/writeUnsigned32 out v)
                         (let [in (ByteArrayInputStream. (.toByteArray out))]
                           (Primitives/readUnsigned32 in))))]
      (is (= 0 (write-read 0)))
      (is (= 255 (write-read 255))))))

(deftest primitives-unsigned64
  (testing "unsigned64 uses varint encoding"
    (let [write-read (fn [v]
                       (let [out (ByteArrayOutputStream.)]
                         (Primitives/writeUnsigned64 out v)
                         (let [in (ByteArrayInputStream. (.toByteArray out))]
                           (Primitives/readUnsigned64 in))))]
      (is (= 0 (write-read 0)))
      (is (= Long/MAX_VALUE (write-read Long/MAX_VALUE))))))
