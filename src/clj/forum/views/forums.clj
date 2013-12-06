(ns forum.views.forums
  (:require [forum.views.base :refer [load-base-view]]))

(load-base-view)

(defn index [forums]
  (html
   ;; Jumbotron only should annoy guests on forumlist.
   (when (guest? *current-user*)
     [:div.jumbotron
      [:h1 "clj-forum"]
      [:p.lead "Where thugs get it free and you've gotta be a G."]
      [:p [:a.btn.btn-lg.btn-success {:href "/users/new"} "Sign up"]]])

   ;; List all forums. TODO: In position order.
   [:div.list-group.forum-list
    (for [forum forums]
      [:a.list-group-item {:href (url-for forum)}
       [:div.row
        [:div.col-xs-8
         [:h4.list-group-item-heading.forum-title
          (:forum/title forum)]
         [:p.list-group-item-text (:forum/desc forum)]]
        [:div.col-xs-4.forum-meta
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
          :let [user (first (:user/_topics topic))
                latest-post (latest-post topic)
                latest-creator (creator latest-post)]]
      [:div.list-group-item.clearfix.topic
       ;; Topic info
       [:div.col-sm-8
        [:div.topic-title
         (link-to (url-for topic) (:topic/title topic))]

        ;; Topic creator
        [:style
         ".topic-creator { color: #888; }
          .topic-creator a { color: #555; }"]
        [:div.topic-creator
         "Started " (pretty-date (:topic/created topic))
         " by " (link-to (url-for user) (:user/uname user))
         ]]

       [:style
        ".latest-post-meta { color: #888; }
         .latest-post-meta .latest-post-creator,
         .latest-post-meta .latest-post {
           color: #555;
         }"]

       ;; Latest post
       [:div.col-sm-4.latest-post-meta
        [:div
         (link-to {:class "latest-post"}
                  (url-for topic
                           "#post-"
                           (:post/uid latest-post))
                  "Latest post")
         " by "
         (link-to {:class "latest-post-creator"}
                  (url-for latest-creator)
                  (decorated-uname latest-creator))]
        (pretty-date (:post/created latest-post))]])]

   (when (can? *current-user* :create :topic)
     (list
      ;; New topic form
      [:h3 "New Topic"]
      (form-to {:role "form"} [:post (str (url-for forum) "/topics")]
        [:div.form-group
         (text-field {:class "form-control"
                      :placeholder "Title"}
                     "topic[title]")]
        [:div.form-group
         (forum.views.posts/post-text-area "topic[text]")]
        (anti-forgery-field)
        (submit-button {:class "btn btn-primary"} "Submit"))))))
