(ns forum.handler
  (:use [compojure.core])
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [forum.db :as db]
            [forum.controllers.forums]
            [forum.controllers.topics]))

;; - The Router should do preliminary checking on params so that
;;   Controllers can expect to coerce, ex. (Long. "123"), without exception.
;; - Controllers do all the DB calls and pass entities into Views.
;; - Views shouldn't have to dig into nested resource like
;;   have to pull the Posts out of a Topic entity. Instead, the Controller
;;   should just pass in `topic` and `posts`. Or perhaps call a decorator fn that does it
;;   for the View.

(defroutes app-routes
  (GET "/" []
    (forum.controllers.forums/index))
  ;; /forums/:forumid
  ;; /forums/:forumid/topics/:topicid
  (context ["/forums/:forumid", :forumid #"[0-9]+"] [forumid]
    (GET "/" []
      (forum.controllers.forums/show forumid))
    (GET ["/topics/:topicid", :topicid #"[0-9]+"] [topicid]
      (forum.controllers.topics/show forumid topicid)))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
