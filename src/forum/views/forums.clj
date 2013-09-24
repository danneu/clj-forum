(ns forum.views.forums
  (:use [hiccup core form page element util])
  (:require [forum.views.master :refer :all]
            [forum.db]
            [forum.helpers :refer :all]))

(defn index [forums]
  (html

   ;; Jumbotron only should annoy guests on forumlist.
   [:div.jumbotron
    [:h1 "clj-forum"]
    [:p.lead "Where thugs get it free and you've gotta be a G."]
    [:p [:a.btn.btn-lg.btn-success {:href "/"} "Sign up"]]]

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
         (str (pretty-date (:topic/createdAt (latest-topic forum))))
         ]
        ]])
    ]))

(defn show [forum topics]
  (html
   [:h2 (:forum/title forum)]
   
   [:table.table
    (for [t topics]
      [:tr
       [:td
        (link-to (url-for t)
                 (:topic/title t)) [:br]
        "by " (link-to "/" "Username")]
       [:td (count (:topic/posts t)) " posts"]
       [:td (pretty-date (:topic/createdAt t))]])]

   [:h3 "New Topic"]
   (form-to
       {:role "form"}
       [:post (str "/forums/" (:db/id forum) "/topics")]
    [:div.form-group
     (text-field {:class "form-control"
                  :placeholder "Title"}
                 "topic[title]")]
    [:div.form-group
     (text-area {:class "form-control"
                 :placeholder "Post"}
                "topic[post][text]")]
    (submit-button {:class "btn btn-default"} "Submit"))))
