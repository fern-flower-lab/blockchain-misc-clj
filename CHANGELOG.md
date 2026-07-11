# Changelog

## 0.0.6 (2026-07-11)

### Fixed
- `codec.utils/hex->bytes` now strips `0x`/`0X` only as a prefix; a `0x`
  appearing elsewhere in the string is rejected instead of silently removed.
- `codec.utils/bytes->hex` is now faithful for byte arrays: every byte renders
  as two hex chars, leading zero bytes are preserved, and the output always
  round-trips through `hex->bytes`. Numbers (including `BigInteger` and
  `UInt128`/`UInt256`) still render minimally; zero renders as `"0"` and
  negative numbers throw instead of producing garbage.
- `codec.rlp/decode` validates its input: truncated payloads, non-canonical
  encodings (single byte below `0x80` behind a prefix, long form used for
  short lengths, length bytes with leading zeros), trailing bytes, and empty
  input all throw `ex-info` instead of fabricating zero bytes.
- `codec.rlp/decode` of an encoded empty vector returns `[]` instead of `nil`.
- `codec.rlp` encodes strings as UTF-8 explicitly instead of relying on the
  platform default charset.
- `codec.bson/decode` no longer round-trips through JSON: it decodes BSON
  directly with keywordized keys, so binary values come back as byte arrays
  instead of base64 strings, and corrupt input throws instead of silently
  returning `nil`.
- `Binary.Bson.decodeObject` rethrows decode failures as `UncheckedIOException`
  instead of swallowing them and returning `null`; the shared `ObjectMapper`
  is now a reusable static instance.
- `Binary.Primitives` read methods throw `IllegalStateException` on truncated
  or exhausted input instead of returning garbage values.
- `Base58.decode` throws `IllegalArgumentException` naming the offending
  character and position instead of a bare checked `IOException`.
- `UInt128`/`UInt256` constants (`ZERO`, `ONE`, `TWO`, `MAX_VALUE`) are now
  `final` and can no longer be reassigned.
- License metadata now matches the MIT LICENSE file.

### Changed
- **Breaking:** `bytes->hex` output for byte arrays (see above) — callers that
  relied on stripped leading zero nibbles must strip them explicitly.
- **Breaking:** `Base58.decode` no longer declares `throws IOException`.
- Source/test paths moved from `profiles.clj` into `project.clj`; plain
  `lein test` now works without profile incantations.
- `TUID` obtains the process id via `ProcessHandle` instead of parsing the
  JMX runtime name.
- Removed the unused `potemkin` dependency.

### Added
- GitHub Actions CI running the test suite.
- Regression tests for all of the above.

## 0.0.5

- Initial public development version.
