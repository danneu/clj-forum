(ns forum.views.master
  (:use [hiccup core element form page util])
  (:require [forum.middleware.expose-request :refer [req]]
            [forum.middleware.wrap-current-user :refer [current-user]])
  (:import [java.io StringWriter]))

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
       (include-css "/css/bootstrap-override.css")]
      [:body
       
       [:div.container
        [:nav.navbar.header
         ;; [:ul.nav.nav-pills.pull-right
         ;;  [:li (link-to "/" "Home")]
         ;;  [:li (link-to "/users" "Users")]]
         (if current-user
           ;; If logged in
           (list [:ul.nav.navbar-nav.pull-right
                  [:li (link-to "/logout" "logout")]]
                 [:div.pull-right
                  [:p.navbar-text
                   "Logged in as "
                   (link-to (url "/users/" (:user/uid current-user))
                            (:user/uname current-user))]])
           ;; If logged out
           (list
            (form-to
                   {:class "navbar-form navbar-right"}
                   [:post "/sessions/create"]
                   [:div.form-group
                    (text-field {:class "form-control input-sm"
                                 :placeholder "Username"}
                                "uname")]
                   [:div.form-group
                    (password-field {:class "form-control input-sm"
                                     :placeholder "Password"}
                                    "pwd")]
                   (submit-button {:class "btn btn-primary btn-sm"}
                                  "Log in"))

            [:p.navbar-text.pull-right
             (link-to {:class "navbar-link"} "/" "Register")
             " or"]

                 ))  ; /if current-user
         
         [:h3 (link-to {:class "navbar-brand"} "/" "clj-forum")]]

        (render-crumbs (:crumbs opts))

        [:ul.nav.nav-tabs.main-nav
         [:li.active (link-to "/" "Forums")]
         [:li (link-to "/users" "Users")]]

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
        [:nav#debug-bar.navbar.navbar-inverse.navbar-fixed-bottom
         [:div.navbar-header [:a.navbar-brand "Debug"]]
         [:button#toggle-request-map.btn.btn-default.navbar-btn {:type "button"} "Toggle request-map"]]

        ] ;/container

       (include-js "//netdna.bootstrapcdn.com/bootstrap/3.0.0/js/bootstrap.min.js")
       (include-js "//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js")
       (include-js "/js/debug.js")
       
       ]  ;/body

      )))
