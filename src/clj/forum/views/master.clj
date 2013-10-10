(ns forum.views.master
  (:require [forum.helpers :refer [logged-in? url-for]]
            [forum.middleware.expose-request :refer [req]]
            [forum.middleware.wrap-current-user :refer [*current-user*]]
            [hiccup.core :refer [html]]
            [hiccup.element :refer [image link-to]]
            [hiccup.form :refer [form-to
                                 password-field
                                 submit-button
                                 text-field]]
            [hiccup.page :refer [html5 include-css include-js]]
            [ring.util.anti-forgery :refer [anti-forgery-field]])
  (:import (java.io StringWriter)))

(defn render-crumbs [crumbs]
  (when crumbs
    [:ol.breadcrumb
     [:li (link-to "/" "Home")]
     (for [c crumbs]
       [:li c])]))

(defn render-flash
  "Expects flash of the form [:danger 'Message']."
  []
  (when-let [[type message] (:flash req)]
    (html
     (let [alert-class (str "alert-" (name type))]
       [:div.alert {:class alert-class}
        message]))))

(defn layout
  "The layout wraps individuals views.
   It accepts an opts map as its first argument to let
   views send stuff to the big picture like breadcrumbs."
  ([view]
     (layout {} view))
  ([opts view]
     (html5
      [:head
       [:title "clj-forum"]
       (include-css "//netdna.bootstrapcdn.com/bootstrap/3.0.0/css/bootstrap.min.css")
       (include-css "/css/bootstrap-override.css"
                    "/css/forum.css")]
      [:body

       [:div.container
        [:nav.navbar.header
         ;; [:ul.nav.nav-pills.pull-right
         ;;  [:li (link-to "/" "Home")]
         ;;  [:li (link-to "/users" "Users")]]

         [:style ".badge-sm { padding: 1px 5px; }
                  .btn .badge-sm { top: 0; }"]

         (if (logged-in? *current-user*)
           (list
            ;; Logout button
            (form-to {:class "navbar-form navbar-right"}
              [:delete "/logout"]
              (anti-forgery-field)
              (submit-button {:class "btn btn-link"} "logout"))
            [:div.btn-group.pull-right.btn-group-sm
             (link-to {:class "btn btn-default navbar-btn"}
                      "/"
                      [:span.glyphicon.glyphicon-heart])
             (link-to {:class "btn btn-default navbar-btn"}
                      "/"
                      [:span.glyphicon.glyphicon-envelope]
                      " "
                      [:span.badge.badge-sm "2"])
             (link-to {:class "btn btn-default navbar-btn"}
                      (url-for *current-user*)
                      [:span.glyphicon.glyphicon-user]
                      " "
                      (:user/uname *current-user*))
             ])
           ;; If logged out
           (list
            ;; Login form
            (form-to {:class "navbar-form navbar-right"}
                     [:post "/sessions/create"]
              [:div.form-group
               (text-field {:class "form-control input-sm"
                            :placeholder "Username"}
                           "uname")]
              [:div.form-group
               (password-field {:class "form-control input-sm"
                                :placeholder "Password"}
                               "pwd")]
              (anti-forgery-field)
              (submit-button {:class "btn btn-primary btn-sm"}
                             "Log in"))

            ;; Register link
            [:p.navbar-text.pull-right
             (link-to {:class "navbar-link"} "/users/new" "Register")
             " or"])
           )  ; /if *current-user*

         [:div.navbar-header
          [:h3 (link-to {:class "navbar-brand"} "/" "clj-forum")]]
         [:ul.nav.navbar-nav
          [:li (link-to "/users" "Users")]]
         ]

        (render-crumbs (:crumbs opts))

        ;; [:ul.nav.nav-tabs.main-nav
        ;;  [:li.active (link-to "/" "Forums")]
        ;;  [:li (link-to "/users" "Users")]]

        (render-flash)

        view

        [:div.footer.clearfix
         [:p.pull-right
          "Source code at "
          (link-to "http://github.com/danneu/clj-forum" "github")]]

        (link-to "https://github.com/danneu/clj-forum"
                 (image {:style "position: absolute;
                          top: 0; right: 0; border: 0;"}
                        "https://s3.amazonaws.com/github/ribbons/forkme_right_white_ffffff.png"
                        "Fork me on Github"))

        ;; Debug
        [:div#request-map
         [:pre
          (let [w (StringWriter.)]
            (clojure.pprint/pprint req w)
            (.toString w))]]

        ;; Debug bar
        [:style "body { padding-bottom: 70px; }"]  ; 20px default + 50px debug bar height
        [:style ".debug-bar { min-height: 30px; }"]
        [:style ".debug-bar .navbar-brand { padding: 5px; }
                 .debug-bar .btn { padding: 0 5px; margin: 5px; }"]
        [:nav.debug-bar.navbar.navbar-inverse.navbar-fixed-bottom
         [:div.navbar-header [:a.navbar-brand "Debug"]]
         [:button#toggle-request-map.btn.btn-default.navbar-btn.btn-sm {:type "button"} "Toggle request-map"]]

        ] ;/container

       (include-js "//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js")
       (include-js "//netdna.bootstrapcdn.com/bootstrap/3.0.0/js/bootstrap.min.js")
       (include-js "/js/debug.js")
       [:script {:type "text/javascript"} "$('.dropdown-toggle').dropdown();"]
       ;(include-js "/js/app.min.js")
       (include-js "/js/app.js")

       ]  ;/body

      )))
