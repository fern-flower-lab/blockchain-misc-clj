(defproject ai.z7/blockchain-misc-clj "0.0.6"
  :description "The Clojure-helpers to make blockchain development less painful"
  :url "https://github.com/fern-flower-lab/blockchain-misc-clj"
  :license {:name "MIT License"
            :url  "https://github.com/fern-flower-lab/blockchain-misc-clj/blob/master/LICENSE"}
  :source-paths ["src-clj"]
  :java-source-paths ["src-java"]
  :test-paths ["test-clj"]
  :javac-options ["-source" "9" "-target" "9" "-g:none"]
  :jar-exclusions [#"\.java"]
  :dependencies [[metosin/jsonista "1.0.0"]
                 ;; keep jackson-core in lockstep with jackson-databind
                 [com.fasterxml.jackson.core/jackson-core "2.22.1"]
                 [com.fasterxml.jackson.core/jackson-databind "2.22.1"]
                 ;; NOTE: bson4jackson 3.x targets Jackson 3 (tools.jackson) and is
                 ;; incompatible with jsonista/Jackson 2 — stay on the 2.x line.
                 [de.undercouch/bson4jackson "2.18.0"]])
