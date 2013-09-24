(ns forum.views.topics
  (:use [hiccup core element form util])
  (:require [forum.views.master :refer :all]
            [forum.helpers :refer :all]))

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
       [:post (str (url-for topic) "/posts")]
    [:div.form-group
     (text-area {:class "form-control"
                 :placeholder "Text"}
                "post[text]")]
    (submit-button {:class "btn btn-default"} "Submit"))))
