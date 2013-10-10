(ns forum.views.topics
  (:use [hiccup core element form util])
  (:require [forum.views.master :refer :all]
            [forum.markdown :refer [to-html]]
            [forum.views.posts]
            [forum.cancan :refer :all]
            [forum.middleware.wrap-current-user
             :refer [*current-user*]]
            [forum.helpers :refer :all]))

(defn show [forum topic posts]
  (html
   ;; Topic title
   [:h1 (:topic/title topic)]

   (for [post posts
         :let [[user] (:user/_posts post)]]
     [:div.panel.panel-default.post
      [:a {:name (str "post-" (:post/uid post))}]
      ;; Post heading
      [:div.panel-heading.clearfix.post-meta
       [:div.pull-right
        (link-to (url-for topic "#post-" (:post/uid post))
                 (pretty-date (:post/created post)))]]
      [:div.panel-body
       ;; Post info
       [:div.col-sm-2.poster-meta
        [:div.uname
         (link-to  (url-for user) (:user/uname user))]
        (link-to (url-for user)
                 (image {:class "avatar hidden-xs img-rounded"}
                        (url-for user "/avatar")
                        ;"http://placehold.it/80x80/f2f2f2/666666"
                        ))]
       ;; Post text
       [:div.col-sm-10
        (to-html (:post/text post))]]
      [:div.panel-footer.clearfix

       [:div.nav.nav-pills.pull-left.post-controls
        [:li (link-to "/"
                      [:span.glyphicon.glyphicon-exclamation-sign])]
        [:li.dropdown
         [:a.dropdown-toggle
          {:data-toggle "dropdown" :href "/"}
          "Moderate " [:span.caret]]
         [:ul.dropdown-menu
          (if (first-post? post)
            (list [:li (link-to "/" "Stick")]
                  [:li (link-to "/" "Lock")]
                  [:li (link-to "/" "Delete")])
            (list [:li (link-to "/" "Delete")]))
          ]]
        ]

       [:div.nav.nav-pills.pull-right.post-controls
        [:li (link-to ;{:style "color: #FF82AB;"}
              "/"  [:span.glyphicon.glyphicon-heart]
              )]
        (when (can? *current-user* :update post)
          [:li (link-to (url-for post "/edit") "Edit")])
        [:li (link-to "/" "Reply")]
        ]]])

   ;; New post form
   (when (can? *current-user* :create :post)
     (list
      [:h3 "New Post"]
      (forum.views.posts/post-form :post
                                   (url-for topic "/posts"))
      [:div#post-preview]))

  ))
