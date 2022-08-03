{:dev      [:dev/all #=(eval (leiningen.core.utils/get-os))]

 :dev/all  {:global-vars  {*warn-on-reflection* true}
            :dependencies [[org.clojure/clojure "1.11.1"]]}

 :linux    {}
 :windows  {}
 :macosx   {}
 :freebsd  {}
 :openbsd  {}

 :provided {:source-paths      ["src-clj"]
            :java-source-paths ["src-java"]
            :javac-options     ["-source" "11" "-target" "11" "-g:none"]
            :jar-exclusions    [#"\.java"]}

 :aliases  {}}
