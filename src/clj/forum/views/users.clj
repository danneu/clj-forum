(ns forum.views.users
  (:require [forum.helpers :refer [pretty-date url-for]]
            [forum.cancan :refer [role]]
            [hiccup.core :refer [html]]
            [hiccup.element :refer [image link-to]]
            [hiccup.form :refer [form-to
                                 label
                                 submit-button
                                 text-field]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]))

(defn index [users]
  (html
   [:h2.page-header "Users "
    [:small (count users)]]
   [:table.table
    [:thead
     [:tr
      [:td "User"]
      [:td "Joined"]
      [:td "Topics"]
      [:td "Posts"]]]
    [:tbody
     (for [user users]
       [:tr
        [:td (link-to (url-for user) (:user/uname user))]
        [:td (pretty-date (:user/created user))]
        [:td (count (:user/topics user))]
        [:td (count (:user/posts user))]])]]))

(defn show [user]
  (html
   [:h2.page-header (:user/uname user) " "
    [:small (link-to (url-for user "/edit") "Edit")]]
   [:div.row
    [:div.col-sm-2
     (image {:class "avatar img-rounded"}
            (url-for user "/avatar"))]
    [:div.col-sm-10
     [:ul
      [:li "Role: " (role user)]
      [:li "Topics: " (count (:user/topics user))]
      [:li "Posts: " (count (:user/posts user))]]]]))

(defn new []
  (html
   [:div.row

    [:h2 "New User"]

    ;; User sign-up creates a user -> creates a session
    (form-to {:role "form"} [:post "/users/create"]
      [:div.form-group
       (label "user[uname]" "Username:")
       (text-field {:class "form-control"}
                   "user[uname]")]
      [:div.form-group
       (label "user[email]" "Email:")
       (text-field {:class "form-control"}
                   "user[email]")
       [:p.help-block "Your email is only used to recover your password."]]
      [:div.form-group
       (label "user[pwd]" "Password:")
       (text-field {:class "form-control"
                    :type "password"} "user[pwd]")]
      [:div.form-group
       (label "user[confirm-pwd]" "Confirm Password:")
       (text-field {:class "form-control"
                    :type "password"}
                   "user[confirm-pwd]")]
      (anti-forgery-field)
      (submit-button {:class "btn btn-primary"} "Join Forum"))
    ]))

;; can edit:
;; - password
;; - email
;; - avatar
(defn edit [user]
  (html
   [:h3 "Change Email"]
   (form-to {:class "well"
             :role "form"}
     [:put (url-for user)]
     [:div.form-group
      (label "user[email]" "Email:")
      (text-field {:class "form-control"}
                  "user[email]"
                  (:user/email user))]
     (anti-forgery-field)
     (submit-button {:class "btn btn-default"}
                    "Change Email")
     )
   [:h3 "Change Password"]
   (form-to {:class "well"
             :role "form"}
     [:put (url-for user)]
     [:div.form-group
      (label "user[new-pwd]" "New password:")
      (text-field {:class "form-control"}
                  "user[pwd]")]
     [:div.form-group
      (label "user[new-confirm-pwd]" "Confirm new password:")
      (text-field {:class "form-control"}
                  "user[confirm-pwd]")]
     (anti-forgery-field)
     (submit-button {:class "btn btn-default"} "Change Password")
     )))
