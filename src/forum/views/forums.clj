(ns forum.views.forums
  (:use [hiccup core form page element util])
  (:require [forum.views.master :refer :all]))

(defn index [forums]
  (html

   ;; Jumbotron only should annoy guests on forumlist.
   [:div.jumbotron
    [:h1 "clj-forum"]
    [:p.lead "Where thugs get it free and you've gotta be a G."]
    [:p [:a.btn.btn-lg.btn-success {:href "/"} "Sign up"]]]


   [:h1 "Forums"]
   [:table.table
    (for [f forums]
      [:tr
       [:td (link-to (url "/forums/" (:db/id f)) (:forum/title f))]])]))

(defn show [forum topics]
  (html
   [:h1 (:forum/title forum)]
   [:table.table
    (for [t topics]
      [:tr
       [:td (link-to (url "/forums/"
                          (:db/id forum)
                          "/topics/"
                          (:db/id t))
                     (:topic/title t))]
       ;; FIXME: Pulling posts out of topic. I think this is too much knowledge for view.
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
