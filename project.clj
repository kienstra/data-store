(defproject data-store "0.1.0-SNAPSHOT"
  :description "An in-memory data store"
  :url "http://example.com/FIXME"
  :license {:name "GPL-2.0-or-later"
            :url "https://www.gnu.org/licenses/old-licenses/gpl-2.0.txt"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/core.async "1.6.673"]
                  [org.jboss.netty/netty "3.2.7.Final"]
                 [org.clojure/data.finger-tree "0.0.1"]]
  :plugins [[lein-cljfmt "0.9.2"]]
  :main ^:skip-aot data-store.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
