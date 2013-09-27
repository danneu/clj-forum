(ns forum.views.topics
  (:use [hiccup core element form util])
  (:require [forum.views.master :refer :all]
            [forum.helpers :refer :all]))

(defn show [forum topic posts]
  (html
   ;; Topic title
   [:h1 (:topic/title topic)]

   [:style ".post .post-btn { padding: 0px 5px; }
            .post.panel { margin-bottom: 5px; }
            .post .panel-body { padding: 0 0 10px 0; }
            .post .panel-heading { padding-top: 5px; padding-bottom: 0px; font-size: 12px; }
            .post .panel-footer { padding-top: 5px; padding-bottom: 5px; }
            .post .panel-heading { background-color: white; }
            .post .panel-heading { border-bottom: 0; }"]
   [:style ".post .panel-footer { border: 0; padding: 0; }"]
   [:style ".post .post-meta a { color: #ccc; }"]
   [:style ".post .poster-meta .uname { margin-bottom: 5px; }"]
   [:style "
            @media (min-width: 768px) {
              .post .poster-meta .uname {
                text-align: center;
              }
            } "]

   (for [post posts
         :let [[user] (:user/_posts post)]]
     [:div.panel.panel-default.post
      ;; Post modbar
      [:div.panel-heading.modbar
       ]
      ;; Post heading
      [:div.panel-heading.clearfix.post-meta
       [:div.pull-right
        (link-to (url-for post) (pretty-date (:post/created post)))]]
      [:div.panel-body
       ;; Post info
       [:div.col-sm-2.poster-meta
        [:div.uname
         (link-to  (url-for user) (:user/uname user))]
        (image {:class "avatar hidden-xs"} "http://placehold.it/80x80/f2f2f2/666666")]
       ;; Post text
       [:div.col-sm-10
        (:post/text post)]]
      [:div.panel-footer.clearfix


       [:style ".nav.post-controls>li>a {
                  padding: 0 5px;
                  color: #999;
                  font-size: 12px;
                }"]
       [:style ".nav.post-controls>li>a:hover {color: #333;}"]
       [:style ".post-controls a .caret,
                .post-controls a:hover .caret {
                  border-bottom-color: black;
                  border-top-color: #999;
                }"]
       [:div.nav.nav-pills.pull-left.post-controls
        [:li (link-to "/" [:span.glyphicon.glyphicon-exclamation-sign])]
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
        [:li (link-to {:style "color: red;"} "/"  "7" [:span.glyphicon.glyphicon-heart])]
        [:li (link-to "/" "Edit")]
        [:li (link-to "/" "Reply")]
        ]
       
       ;; [:div.btn-group.btn-group-sm.pull-right
       ;;  [:button.btn.btn-default.post-btn.post-btn {:type ""} "Edit"]
       ;;  [:button.btn.btn-default.post-btn.post-btn {:type "button"} "Quote"]
       ;;  [:button.btn.btn-default.post-btn.post-btn {:type "button"} "Reply"]]
       ]])

   ;; New post form
   [:h3 "New Post"]
   (form-to
       {:role "form"}
       [:post (str (url-for topic) "/posts")]
    [:div.form-group
     (text-area {:class "form-control"
                 :placeholder "Text"}
                "post[text]")]
    (submit-button {:class "btn btn-default"} "Submit"))))
