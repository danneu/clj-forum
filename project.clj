(defproject forum "0.1.0-SNAPSHOT"
  :description "a forum build with Datomic and Clojure"
  :url "http://github.com/danneu/clj-forum"
  :dependencies
  [[org.clojure/clojure "1.5.1"]
   [compojure "1.1.5"]  ; routing
   [com.datomic/datomic-free "0.8.4020.24"]  ; database
   [org.ocpsoft.prettytime/prettytime "3.1.0.Final"]  ; formats Date -> "2 days ago"
   [bouncer "0.3.0-alpha1"]  ; validation
   [hiccup "1.0.4"]  ; templating
   [ring "1.2.0"]
   [org.jasypt/jasypt "1.9.1"]  ; password encryption
   [expectations "1.4.55"]  ; test framework
   ]
  :plugins [[lein-ring "0.8.5"]]
  :ring {:handler forum.handler/app}
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.5"]]}})
