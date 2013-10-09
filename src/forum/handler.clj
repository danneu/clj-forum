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
            [forum.controllers.sessions]
            [forum.middleware.expose-request
             :refer [expose-request req]]
            [forum.middleware.wrap-current-user
             :refer [*current-user* wrap-current-user]]
            [hiccup.middleware :refer [wrap-base-url]]
            [ring.middleware.session.cookie :refer [cookie-store]]
            [ring.util.response :refer [redirect]]))

;; - Controllers do all the DB calls and pass entities into Views.

(defroutes app-routes
  (GET "/" [] (forum.controllers.forums/index))
  (context "/forums/:fuid" [fuid]
    (GET "/" [] (forum.controllers.forums/show fuid))
    (POST "/topics" [topic] (forum.controllers.topics/create fuid topic))
    (context "/topics/:tuid" [tuid]
      (GET "/" [] (forum.controllers.topics/show fuid tuid))
      (context "/posts" []
        (POST "/" [post] (forum.controllers.posts/create fuid tuid post))
        (PUT "/:puid" [puid post] (forum.controllers.posts/update puid post))
        (GET "/:puid/edit" [puid] (forum.controllers.posts/edit fuid tuid puid)))))
  (context "/users" []
    (GET "/" [] (forum.controllers.users/index))
    (GET "/new" [] (forum.controllers.users/new))
    (POST "/create" [user] (forum.controllers.users/create user))
    (PUT "/:uid" [uid user] (forum.controllers.users/update uid user))
    (GET "/:uid" [uid] (forum.controllers.users/show uid))
    (GET "/:uid/edit" [uid] (forum.controllers.users/edit uid)))
  (POST "/sessions/create" [uname pwd] (forum.controllers.sessions/create uname pwd))
  (GET "/logout" [] (forum.controllers.sessions/destroy))
  (route/resources "/")
  (route/not-found "Not Found"))

;; Config ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def config
  ;; {:session-secret String}
  (read-string (slurp (io/resource "config.edn"))))

;; Store session in cookie that's encrypted
;; with AES key defined as :session-secret config.edn.
(def session-opts
  {:store (cookie-store {:key (:session-secret config)})})

;; Middleware ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def app
  (-> app-routes
      ;; TODO: don't hardcode base-url like this.
      (wrap-base-url "http://localhost:3000")
      expose-request
      wrap-current-user
      (handler/site {:session session-opts})))

;; Server

;; (defn start-server [port]
;;   (run-jetty app {:port port}))

;; (defn -main [& args]
;;   (let [port (Integer. (or (first args) "5010"))]
;;     (start-server port)))
