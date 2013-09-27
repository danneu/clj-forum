(ns forum.views.master
  (:use [hiccup core element page util])
  (:require [forum.middleware.expose-request :refer [req]]
            [forum.middleware.wrap-current-user :refer [current-user]])
  (:import [java.io StringWriter]))

(defn render-crumbs [crumbs]
  (when crumbs
    [:ol.breadcrumb
     [:li (link-to "/" "Home")]
     (for [c crumbs]
       [:li c])]))

(defn render-flashes []
  (when-let [flashes (:flash req)]
    (html
     (for [flash flashes  ; Ex: [[:danger "Uh oh!"]]
           :let [[type message] flash]]
       [:div.alert {:class (str "alert-" (name type))} message]))))

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
        [:div.header
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
            [:a.pull-right.btn.btn-default.navbar-btn
             {:href "/"}
             "Register"]
            (form-to
                   {:class "navbar-form navbar-right"}
                   [:post "/sessions/new"]
                   [:div.form-group
                    (text-field {:class "form-control"
                                 :placeholder "Username"}
                                "user[uname]")]
                   [:div.form-group
                    (password-field {:class "form-control"
                                     :placeholder "Password"}
                                    "user[pwd]")]
                   (submit-button {:class "btn btn-default"}
                                  "Log in"))
                 ))
         
         [:h3 (link-to "/" "clj-forum")]]

        (render-flashes)

        (render-crumbs (:crumbs opts))

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
