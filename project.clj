(defproject data-store "0.1.0"
  :description "An in-memory data store"
  :url "https://github.com/kienstra/data-store"
  :license {:name "GPL-2.0-or-later"
            :url "https://www.gnu.org/licenses/old-licenses/gpl-2.0.txt"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/core.async "1.6.673"]
                 [io.netty/netty-all "4.1.97.Final"]
                 [org.clojure/data.finger-tree "0.0.1"]
                 [com.clojure-goes-fast/clj-async-profiler "1.0.5"]]
  :main ^:skip-aot data-store.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}
             :dev {:aot :all
                   :jvm-opts ["-Dio.netty.leakDetectionLevel=advanced"
                              "-Djdk.attach.allowAttachSelf"
                              "-XX:+UnlockDiagnosticVMOptions"
                              "-XX:+DebugNonSafepoints"]
                   :dependencies [[criterium "0.4.6"]]
                   :plugins [[lein-cljfmt "0.9.2"]]}})
