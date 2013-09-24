(ns forum.handler
  (:use [compojure.core])
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [forum.db :as db]
            [forum.controllers.forums]
            [forum.controllers.posts]
            [forum.controllers.topics]))

;; - The Router should do preliminary checking on params so that
;;   Controllers can expect to coerce, ex. (Long. "123"),
;;   without exception. Hmm, I don't like having the remember
;;   to coerce. Perhaps db-fns should handle strings.
;; - Controllers do all the DB calls and pass entities into Views.

(defroutes app-routes
  (GET "/" []
    (forum.controllers.forums/index))
  (context ["/forums/:forumid", :forumid #"[0-9]+"] [forumid]
    ;; /forums/:forumid
    (GET "/" []
      (forum.controllers.forums/show forumid))
    ;; /forums/:forumid/topics
    (POST "/topics" [topic]
      (forum.controllers.topics/create forumid topic))
    (context ["/topics/:topicid", :topicid #"[0-9]+"] [topicid]
      ;; /forums/:forumid/topics/:topicid
      (GET "/" []
        (forum.controllers.topics/show forumid topicid))
      ;; /forums/:forumid/topics/:topicid/posts
      (POST "/posts" [post]
        (forum.controllers.posts/create forumid topicid post))))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
