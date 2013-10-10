(defproject forum "0.1.0-SNAPSHOT"
  :description "a forum build with Datomic and Clojure"
  :url "http://github.com/danneu/clj-forum"
  :dependencies
  [[org.clojure/clojure "1.5.1"]
   [compojure "1.1.5"]  ; Routing
   [com.datomic/datomic-free "0.8.4020.24"]  ; Database
   [org.ocpsoft.prettytime/prettytime "3.1.0.Final"]  ; Date -> "2 days ago"
   [bouncer "0.3.0-alpha1"]  ; Validation
   [hiccup "1.0.4"]  ; Templating
   [markdown-clj "0.9.33"]  ; Server & client-side Markdown parser
   [ring "1.2.0"]
   [net.mikera/imagez "0.1.0"]  ; Create/manipulate images
   [org.jasypt/jasypt "1.9.1"]  ; Password encryption
   [ring-anti-forgery "0.3.0"]  ; CSRF protection

   ;; Testing
   [expectations "1.4.55"]  ; Test framework

   ;; ClojureScript
   [org.clojure/clojurescript "0.0-1913"]
   [org.clojure/google-closure-library-third-party "0.0-2029"]  ; Domina dependency
   [domina "1.0.0"]
   ]
  :plugins [[lein-ring "0.8.5"]
            [lein-cljsbuild "0.3.3"]]
  :source-paths ["src/clj"]
  :hooks [leiningen.cljsbuild]
  :ring {:handler forum.handler/app}
  :profiles {:dev {:dependencies [[ring-mock "0.1.5"]]}}
  :cljsbuild
  {
   :crossovers [forum.markdown.transformers]
   ;; clj files to be available in cljs are to be moved to
   ;; src/crossover
   ;; i.e. src/crossover/forum/markdown/transformers.cljs
   :crossover-path "src/crossover"
   :builds
   [
    {:source-paths ["src/cljs"]
     :compiler {:output-to "resources/public/js/app.js"
                :optimizations :simple
                :pretty-print true}}
    ;; {:source-paths ["src/cljs"]
    ;;  :compiler {:output-to "resources/public/js/app.min.js"
    ;;             :optimizations :advanced
    ;;             :pretty-print false}}
    ]
   }
  )
