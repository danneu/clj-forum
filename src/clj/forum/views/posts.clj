(ns forum.views.posts
  (:use [hiccup core element form util])
  (:require [forum.helpers :refer :all]))

(defn post-form
  "`form-action` is same [method url] vector that
   Hiccup's form-to expects."
  ([method url]
     (post-form method url {}))
  ([method url post]
     (form-to {:role "form"} [method url]
       [:div.form-group
        (text-area {:id "post-markdown"
                    :class "form-control"
                    :placeholder "Text"}
                   "post[text]"
                   (:post/text post))]
       (submit-button {:class "btn btn-default"} "Submit"))))

(defn edit [topic post]
  (html
   [:h3 "Edit Post"]
   (post-form :put (url-for post) post)))
