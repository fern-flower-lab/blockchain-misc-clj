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
  :dependencies [[metosin/jsonista "0.3.13"]
                 [com.fasterxml.jackson.core/jackson-databind "2.21.0"]
                 [de.undercouch/bson4jackson "2.18.0"]])
