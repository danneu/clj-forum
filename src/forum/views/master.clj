(ns forum.views.master
  (:use [hiccup core element page util]))

(defn render-crumbs [crumbs]
  (when crumbs
    [:ul
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

        view 

       [:div.footer
        [:p.pull-right
         "Source code at "
         (link-to "http://github.com/danneu/clj-forum" "github")]]]]


       ;(render-crumbs (:crumbs opts))
      )))
