(ns forum.views.posts
  (:require [forum.helpers :refer [url-for]]
            [hiccup.core :refer [html]]
            [hiccup.form :refer [form-to submit-button text-area]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]))

(defn post-text-area [text-area-name & [text]]
  [:div.row
   [:div.col-sm-6
    "Text: "
    (text-area {:id "post-markdown"
                :class "form-control"}
               text-area-name
               text)]
   [:div.col-sm-6
    "Preview: "
    [:div#post-preview.well
     "&larr; Start writing!"
     ]]])

(defn edit [topic post]
  (html
   [:h3 "Edit Post"]
   (form-to {:role "form"} [:put (url-for post)]
     (post-text-area "post[text]" (:post/text post))
     (anti-forgery-field)
     (submit-button {:class "btn btn-primary"} "Update Post"))
   ;(post-form :put (url-for post) post)
   ))
