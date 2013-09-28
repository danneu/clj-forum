(ns forum.views.forums
  (:use [hiccup core form page element util])
  (:require [forum.views.master :refer :all]
            [forum.db]
            [forum.middleware.wrap-current-user
             :refer [current-user]]
            [forum.helpers :refer :all]))

(defn index [forums]
  (html
   ;; Jumbotron only should annoy guests on forumlist.
   (when-not current-user
     [:div.jumbotron
      [:h1 "clj-forum"]
      [:p.lead "Where thugs get it free and you've gotta be a G."]
      [:p [:a.btn.btn-lg.btn-success {:href "/users/new"} "Sign up"]]])

   ;; List all forums. TODO: In position order.
   [:div.list-group.forum-list
    (for [forum forums]
      [:a.list-group-item {:href (url-for forum)}
       [:div.row
        [:div.col-lg-8
         [:h4.list-group-item-heading.forum-title
          (:forum/title forum)]
         [:p.list-group-item-text (:forum/desc forum)]]
        [:div.col-lg-4.forum-meta
         (str (count (:forum/topics forum)) " topics") [:br]
         (:posts-count forum) " posts"
         ]
        ]])
    ]))

(defn show [forum topics]
  (html


   [:style ".topics .topic { padding: 5px 0; }"]
   [:style ".topics .topic.sticky { background-color: #f5f5f5; }"]
   
   [:h2 (:forum/title forum)]

   [:div.list-group.topics
    (for [topic topics
          :let [[user] (:user/_topics topic)
                latest-post (latest-post topic)
                latest-creator (creator latest-post)]]
      [:div.list-group-item.clearfix.topic
       ;; Topic info
       [:div.col-sm-9
        [:div.topic-title
         (link-to (url-for topic) (:topic/title topic))]
        [:div.topic-creator
         "by " (link-to (url-for user) (:user/uname user))
         ", "
         (pretty-date (:topic/created topic))]]
       ;; Latest post
       [:div.col-sm-3
        [:div
         (link-to (url-for topic
                           "#post-"
                           (:post/uid latest-post))
                  "Latest post")
         " by "
         (link-to (url-for latest-creator)
                  (:user/uname latest-creator))]
        (pretty-date (:topic/created topic))]])]

   ;; New topic form
   [:h3 "New Topic"]
   (form-to
       {:role "form"}
       [:post (str (url-for forum) "/topics")]
     [:div.form-group
      (text-field {:class "form-control"
                   :placeholder "Title"}
                  "topic[title]")]
     [:div.form-group
      (text-area {:class "form-control"
                  :placeholder "Post"}
                 "topic[post][text]")]
     (submit-button {:class "btn btn-default"} "Submit"))))
