(defproject forum "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.5"]
                 [com.datomic/datomic "0.8.3335"]]
  :plugins [[lein-ring "0.8.5"]]
  :ring {:handler forum.handler/app}
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.5"]]}})
