(ns forum.handler
  (:use [compojure.core]
        [clojure.pprint])
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [clojure.java.io :as io]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [forum.controllers.forums]
            [forum.controllers.posts]
            [forum.controllers.topics]
            [forum.controllers.users]
            [forum.middleware.expose-request :refer [expose-request]]
            [ring.middleware.session.cookie :refer [cookie-store]]))

;; - The Router should do preliminary checking on params.
;;   I've decided to cast params to Longs in the Router instead
;;   of downchain in Controllers where it's easy to forget.
;; - Controllers do all the DB calls and pass entities into Views.

(defroutes app-routes
  (GET "/" []
    (forum.controllers.forums/index))
  (context ["/forums/:fuid", :forumid #"[0-9]+"] [fuid]
    ;; /forums/:fuid
    (GET "/" []
      (forum.controllers.forums/show (Long. fuid)))
    ;; /forums/:fuid/topics
    (POST "/topics" [topic]
      (forum.controllers.topics/create (Long. fuid) topic))
    (context ["/topics/:tuid", :topicid #"[0-9]+"] [tuid]
      ;; /forums/:fuid/topics/:tuid
      (GET "/" []
        (forum.controllers.topics/show (Long. fuid)
                                       (Long. tuid)))
      ;; /forums/:fuid/topics/:tuid/posts
      (POST "/posts" [post]
        (forum.controllers.posts/create (Long. fuid)
                                        (Long. tuid)
                                        post))))
  (GET "/users/new" [_ :as req]
    (pprint req)
    (let [flash (:flash req)]
      (forum.controllers.users/new flash)))
  (POST "/users/create" [user]
    (forum.controllers.users/create user))
  (route/resources "/")
  (route/not-found "Not Found"))

(def config
  ;; {:session-secret String}
  (read-string (slurp (io/resource "config.edn"))))

(def app
  (handler/site (expose-request app-routes)
                ;; Store session in cookie that's encrypted
                ;; with AES key defined as :session-secret config.edn.
                {:session
                 {:store (cookie-store
                          {:key (:session-secret config)})}}))

;; Server

;; (defn start-server [port]
;;   (run-jetty app {:port port}))

;; (defn -main [& args]
;;   (let [port (Integer. (or (first args) "5010"))]
;;     (start-server port)))
