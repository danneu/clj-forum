(ns forum.views.topics
  (:require [forum.views.base :refer [load-base-view]]))

(load-base-view)

(defn show [forum topic posts]
  (html
   ;; Topic title
   [:h1 (:topic/title topic)]

   (for [post posts
         :let [user (first (:user/_posts post))]]
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
                        ))
        [:p {:style "text-align: center;"} (str/capitalize (name (role user)))]
        ]
       ;; Post text
       [:div.col-sm-10
        (to-html (:post/text post))]]
      [:div.panel-footer.clearfix

       (if (logged-in? *current-user*)
         (list
          [:div.nav.nav-pills.pull-left.post-controls
           [:li (link-to "/"
                         [:span.glyphicon.glyphicon-exclamation-sign])]
           [:li.dropdown
            [:a.dropdown-toggle
             {:data-toggle "dropdown" :href "/"}
             "Moderate " [:span.caret]]
            [:ul.dropdown-menu
             (if (first-post? post)
               (list [:li (link-to "/" "Stick Topic")]
                     [:li (link-to "/" "Lock Topic")]
                     [:li (link-to "/" "Delete Topic")])
               (list [:li (form-to [:delete (url-for post)]
                            (anti-forgery-field)
                            (submit-button {:class "btn btn-link"}
                                           "Delete Post"))]))
             ]]
           ]))

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
      (form-to {:role "form"}
               [:post (url-for topic "/posts")]
        (forum.views.posts/post-text-area "post[text]")
        (anti-forgery-field)
        (submit-button {:class "btn btn-primary"} "Submit"))
      ))
  ))
