{:dev      [:dev/all #=(eval (leiningen.core.utils/get-os))]

 :dev/all  {:global-vars  {*warn-on-reflection* true}
            :dependencies [[org.clojure/clojure "1.12.4"]]}

 :linux    {}
 :windows  {}
 :macosx   {}
 :freebsd  {}
 :openbsd  {}

 ;; Source/test paths now live in project.clj; this stays so
 ;; `lein with-profile +provided ...` invocations keep working.
 :provided {}

 :aliases  {}}
