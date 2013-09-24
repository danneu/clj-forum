(ns forum.handler
  (:use [compojure.core]
        [hiccup core element util])
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [forum.db :as db]))

(defn index-view [forums]
  (html
   [:h1 "Forums"]
   [:ul
    (for [f forums]
      [:li
       (link-to (url "/forums/" (:db/id f)) (:forum/title f))
       " - (" (count (:forum/topics f)) " topics)"])]))

(defn index-controller []
  (index-view (db/get-all-forums)))

(defn show-forum-view [forum topics]
  (html
   [:h1 (:forum/title forum)]
   [:ul
    (for [t topics]
      [:li (link-to (url "/forums/"
                         (:db/id forum)
                         "/topics/"
                         (:db/id t))
                    (:topic/title t))])]))

(defn show-forum-controller [forumid]
  (when-let [forum (db/get-forum (Long. forumid))]
    (let [topics (:forum/topics forum)]
      (show-forum-view forum topics))))

(defroutes app-routes
  (GET "/" [] (index-controller))
  (GET "/forums/:forumid" [forumid]
    (show-forum-controller forumid))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
