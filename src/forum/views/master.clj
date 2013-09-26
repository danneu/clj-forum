(ns forum.views.master
  (:use [hiccup core element page util])
  (:require [forum.middleware.expose-request :refer [req]])
  (:import [java.io StringWriter]))

(defn render-crumbs [crumbs]
  (when crumbs
    [:ol.breadcrumb
     [:li (link-to "/" "Home")]
     (for [c crumbs]
       [:li c])]))

(defn layout
  "The layout wraps individuals views.
   It accepts an opts map as its first argument to let
   views send stuff to the big picture like breadcrumbs."
  ([view]
     (layout {} view))
  ([opts view]
     (html5
      [:head
       [:title "Forum"]
       (include-css "/bootstrap/css/bootstrap.css")
       (include-css "/css/bootstrap-override.css")]
      [:body

       
       [:div.container
        [:div.header
         [:ul.nav.nav-pills.pull-right
          [:li (link-to "/" "Home")]
          [:li (link-to "/" "About")]
          [:li (link-to "/" "Contact")]]
         [:h3 (link-to "/" "Forum")]]

        (render-crumbs (:crumbs opts))

        [:pre#request-map
         (let [w (StringWriter.)]
           (clojure.pprint/pprint req w)
           (.toString w))]

        view 

        [:div.footer
         [:p.pull-right
          "Source code at "
          (link-to "http://github.com/danneu/clj-forum" "github")]]

        (link-to "https://github.com/danneu/clj-forum"
                 (image {:style "position: absolute;
                          top: 0; right: 0; border: 0;"}
                        "https://s3.amazonaws.com/github/ribbons/forkme_right_white_ffffff.png"
                        "Fork me on Github"))
        ]

       ;; Debug bar
       [:style "body { padding-bottom: 70px; }"]  ; 20px default + 50px debug bar height
       [:nav#debug-bar.navbar.navbar-inverse.navbar-fixed-bottom
        [:div.navbar-header [:a.navbar-brand "Debug"]]
        [:button#toggle-request-map.btn.btn-default.navbar-btn {:type "button"} "Toggle request-map"]]

       (include-js "//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js")
       (include-js "/js/debug.js")
       
       ]  ;/body


      )))
