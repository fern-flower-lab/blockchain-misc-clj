{:dev      [:dev/all #=(eval (leiningen.core.utils/get-os))]

 :dev/all  {:global-vars  {*warn-on-reflection* true}
            :dependencies [[org.clojure/clojure "1.12.4"]]}

 :linux    {}
 :windows  {}
 :macosx   {}
 :freebsd  {}
 :openbsd  {}

 :provided {:source-paths      ["src-clj"]
            :java-source-paths ["src-java"]
            :test-paths        ["test-clj"]
            :javac-options     ["-source" "9" "-target" "9" "-g:none"]
            :jar-exclusions    [#"\.java"]}

 :aliases  {}}
