(ns forum.views.forums
  (:use [hiccup core form page element util])
  (:require [forum.views.master :refer :all]))

(defn index [forums]
  (html
   [:h1 "Forums"]
   [:ul
    (for [f forums]
      [:li
       (link-to (url "/forums/" (:db/id f)) (:forum/title f))
       " - (" (count (:forum/topics f)) " topics)"])]))

(defn show [forum topics]
  (html
   [:h1 (:forum/title forum)]
   [:ul
    (for [t topics]
      [:li
       (:topic/position t) ". "
       (link-to (url "/forums/" (:db/id forum) "/topics/" (:db/id t))
                (:topic/title t))
       ;; FIXME: Pulling posts out of topic. I think this is too much knowledge for view.
       " - (" (count (:topic/posts t)) " posts)"
       " - " (pretty-date (:topic/createdAt t))])]

   ;; New topic form
   (form-to [:post (str "/forums/" (:db/id forum) "/topics")]
    (label "topic[title]" "Topic Title:")
    (text-field "topic[title]")
    (label "topic[post][text]" "Topic Text:")
    (text-area "topic[post][text]")
    (submit-button "Post Topic"))))
