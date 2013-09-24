(ns forum.views.topics
  (:use [hiccup core element form util])
  (:require [forum.views.master :refer :all]))

;; TODO: Abstract urls somehow. Not this way tho. ^_^
(defn gen-url
  ([forum topic & rest]
     (apply str
            "/forums/" (:db/id forum)
            "/topics/" (:db/id topic)
            "/" (interpose "/" rest))))

(defn show [forum topic posts]
  (html
   [:h1 (:topic/title topic)]
   [:ul
    (for [p posts]
      [:li (:post/position p) ". " (:post/text p)
       " - " (pretty-date (:post/createdAt p))])
    (form-to [:post (gen-url forum topic "posts")]
      (text-area "post[text]")
      (submit-button "Post Reply"))]))
