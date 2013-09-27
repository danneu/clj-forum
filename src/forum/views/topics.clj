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
   [:style ".btn-group>.btn:last-child:not(:first-child),
            .btn-group>.dropdown-toggle:not(:first-child) {
              border-top-right-radius: 0;
            }
            .btn-group>.btn:first-child:not(:last-child):not(.dropdown-toggle) {
              border-top-left-radius: 0;
              border-bottom-left-radius: 0;
            } "]

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
      ;; Post heading
      [:div.panel-heading.clearfix.post-meta
       [:div.pull-right
        (link-to (url-for post) "3 hours ago")]]
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
       [:div.btn-group.btn-group-sm.pull-right
        [:button.btn.btn-default.post-btn.post-btn {:type "button"} "Edit"]
        [:button.btn.btn-default.post-btn.post-btn {:type "button"} "Quote"]
        [:button.btn.btn-default.post-btn.post-btn {:type "button"} "Reply"]
        ]]])

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
