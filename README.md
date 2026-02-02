# blockchain-misc-clj

Clojure helpers to make blockchain development less painful.

## Installation

Add the following dependency to your `project.clj`:

```clojure
[ai.z7/blockchain-misc-clj "0.0.5"]
```

Or to your `deps.edn`:

```clojure
ai.z7/blockchain-misc-clj {:mvn/version "0.0.5"}
```

## Features

### Clojure Namespaces

#### `blockchain-misc-clj.codec.utils`

Hex and byte conversion utilities.

```clojure
(require '[blockchain-misc-clj.codec.utils :refer [hex->bytes bytes->hex
                                                    bytes->long long->bytes
                                                    byte-count]])

;; Hex conversion
(hex->bytes "deadbeef")        ; => byte array
(hex->bytes "0xdeadbeef")      ; => handles 0x prefix
(bytes->hex (byte-array [1 2])) ; => "102"
(bytes->hex (byte-array [1]) {:pad-left 4}) ; => "00000001"

;; Long/bytes conversion
(long->bytes 256)              ; => byte array [1 0]
(bytes->long (byte-array [1 0])) ; => 256

;; Byte counting
(byte-count 255)               ; => 1
(byte-count 256)               ; => 2
```

#### `blockchain-misc-clj.codec.rlp`

RLP (Recursive Length Prefix) encoding/decoding for Ethereum compatibility.

```clojure
(require '[blockchain-misc-clj.codec.rlp :refer [encode decode vector?]])

;; Encode various types
(encode nil)                   ; => empty string encoding
(encode (byte-array [1 2 3]))  ; => RLP encoded bytes
(encode "hello")               ; => RLP encoded string
(encode [(byte-array [1]) (byte-array [2])]) ; => RLP encoded list

;; Decode
(decode encoded-bytes)         ; => decoded value or vector

;; Check if encoded value is a vector
(vector? encoded-bytes)        ; => true/false
```

#### `blockchain-misc-clj.codec.bson`

BSON encoding/decoding for maps.

```clojure
(require '[blockchain-misc-clj.codec.bson :refer [encode decode]])

;; Encode map to BSON
(encode {:name "Alice" :age 30})  ; => BSON bytes

;; Decode BSON to map with keyword keys
(decode bson-bytes)               ; => {:name "Alice" :age 30}
```

#### `blockchain-misc-clj.codec.ids`

Time-based UUID (TUID) generation and utilities.

```clojure
(require '[blockchain-misc-clj.codec.ids :refer [tuid tuid->timestamp tuid-cmp]])

;; Generate time-based UUID (v1)
(tuid)                         ; => #uuid "..."

;; Extract timestamp from TUID
(tuid->timestamp (tuid))       ; => milliseconds since epoch

;; Compare TUIDs by timestamp
(tuid-cmp tuid1 tuid2)         ; => -1, 0, or 1
```

### Java Classes

#### `ai.z7.blockchain_misc.UInts.UInt128` / `UInt256`

Unsigned 128-bit and 256-bit integer arithmetic.

```clojure
(import '[ai.z7.blockchain_misc.UInts UInt128 UInt256])

;; Construction
(UInt128. 123)                 ; from long
(UInt128. "12345")             ; from decimal string
(UInt128. "ff" 16)             ; from hex string
(UInt128. (biginteger 123))    ; from BigInteger
(UInt128. (byte-array [1 2]))  ; from bytes

;; Constants
UInt128/ZERO
UInt128/ONE
UInt128/MAX_VALUE

;; Arithmetic
(.add (UInt128. 5) (UInt128. 3))       ; => 8
(.subtract (UInt128. 5) (UInt128. 3))  ; => 2
(.multiply (UInt128. 5) (UInt128. 3))  ; => 15
(.divide (UInt128. 10) (UInt128. 3))   ; => 3
(.mod (UInt128. 10) (UInt128. 3))      ; => 1
(.pow (UInt128. 2) 10)                 ; => 1024

;; Modular arithmetic
(.addmod (UInt128. 4) (UInt128. 3) (UInt128. 5))  ; (4+3) % 5 = 2
(.mulmod (UInt128. 4) (UInt128. 3) (UInt128. 5))  ; (4*3) % 5 = 2

;; Bitwise operations
(.and (UInt128. 0xF0) (UInt128. 0xFF)) ; => 0xF0
(.or (UInt128. 0xF0) (UInt128. 0x0F))  ; => 0xFF
(.xor (UInt128. 0xFF) (UInt128. 0x0F)) ; => 0xF0
(.not (UInt128. 0))                     ; => MAX_VALUE
(.shiftLeft (UInt128. 1) 8)            ; => 256
(.shiftRight (UInt128. 256) 4)         ; => 16

;; Comparison
(.compareTo (UInt128. 5) (UInt128. 3)) ; => positive
(.equals (UInt128. 5) (UInt128. 5))    ; => true

;; Conversion
(.toBigInteger (UInt128. 123))         ; => BigInteger
(.toByteArray (UInt128. 123))          ; => byte[]
(.longValue (UInt128. 123))            ; => 123
```

#### `ai.z7.blockchain_misc.Base58`

Base58 encoding/decoding (Bitcoin-style).

```clojure
(import '[ai.z7.blockchain_misc Base58])

(Base58/encode (.getBytes "Hello"))    ; => "9Ajdvzr"
(Base58/decode "9Ajdvzr")              ; => byte[]
```

#### `ai.z7.blockchain_misc.TUID`

Time-based UUID generation (version 1).

```clojure
(import '[ai.z7.blockchain_misc TUID])

(TUID/timeUID)                         ; => UUID (v1)
(TUID/toTimestamp uuid)                ; => epoch millis
```

#### `ai.z7.blockchain_misc.Binary.Primitives`

Protocol buffer style primitive encoding.

```clojure
(import '[ai.z7.blockchain_misc.Binary Primitives]
        '[java.io ByteArrayInputStream ByteArrayOutputStream])

;; Varint encoding
(let [out (ByteArrayOutputStream.)]
  (Primitives/writeVarint32 out 300)
  (let [in (ByteArrayInputStream. (.toByteArray out))]
    (Primitives/readVarint32 in)))     ; => 300

;; ZigZag encoding for signed integers
(Primitives/encodeZigZag32 -1)         ; => 1
(Primitives/decodeZigZag32 1)          ; => -1

;; Fixed-width encoding
(Primitives/writeFixed32 out 12345)
(Primitives/writeFixed64 out 123456789L)

;; Float/Double
(Primitives/writeFloat out 3.14)
(Primitives/writeDouble out 3.14159)
```

## Development

### Running Tests

```bash
lein with-profile +provided test
```

### Test Coverage

The library includes comprehensive tests covering:

- **TUID**: UUID generation, timestamp extraction, thread safety, comparisons
- **Base58**: Encode/decode roundtrip, leading zeros, invalid characters
- **UInt128/UInt256**: Construction, arithmetic, bitwise ops, comparisons, conversions
- **Primitives**: Varint, ZigZag, fixed-width, float/double encoding
- **Utils**: Hex conversion, byte operations
- **RLP**: Encode/decode for strings, bytes, vectors
- **BSON**: Map serialization with keyword keys
- **IDs**: TUID wrapper functions

## License

See [LICENSE](LICENSE) file.
