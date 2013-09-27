(ns forum.views.users
  (:use [hiccup core element form page util]))

(defn index [users]
  (html
   [:h2 (count users) " Users"]
   [:table.table
    (for [user users]
      [:tr
       [:td (:user/uname user)]
       [:td (:user/uid user)]
       [:td (:user/digest user)]])]))

(defn show [user]
  (html
   [:h2 (:user/uname user)]))

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
       (label "user[pwd]" "Password:")
       (text-field {:class "form-control"
                    :type "password"} "user[pwd]")]
      [:div.form-group
       (label "user[confirm-pwd]" "Confirm Password:")
       (text-field {:class "form-control"
                    :type "password"}
                   "user[confirm-pwd]")]
      (submit-button {:class "btn btn-default"} "Join Forum"))
    ]))
