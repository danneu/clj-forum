(ns forum.views.posts
  (:require [forum.views.base :refer [load-base-view]]))

(load-base-view)

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
