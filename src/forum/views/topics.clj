(ns forum.views.topics
  (:use [hiccup core element util]))

(defn show [forum topic posts]
  (html
   [:h1 (:topic/title topic)]
   [:ul
    (for [p posts]
      [:li (:post/text p)])]))
