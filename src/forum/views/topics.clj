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
   [:ul.list-group
    (for [p posts]
      [:li.list-group-item
        [:div.row
         [:div.col-lg-2
          (link-to "/" "Username") [:br]
          (pretty-date (:post/createdAt p))]
         [:div.col-lg-10 (:post/text p)]]])]

   [:h3 "New Post"]
   (form-to
       {:role "form"}
       [:post (gen-url forum topic "posts")]
    [:div.form-group
     (text-area {:class "form-control"
                 :placeholder "Text"}
                "post[text]")]
    (submit-button {:class "btn btn-default"} "Submit"))))
