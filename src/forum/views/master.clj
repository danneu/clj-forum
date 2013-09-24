(ns forum.views.master
  (:use [hiccup core element page util])
  (:import [org.ocpsoft.prettytime PrettyTime]))

(defn pretty-date [date]
  (.format (PrettyTime.) date))

(defn render-crumbs [crumbs]
  (when crumbs
    [:ul
     [:li (link-to "/" "Home")]
     (for [c crumbs]
       [:li c])]))

(defn layout
  ([view]
     (layout {} view))
  ([opts view]
     (html5
      [:title "Forum"]
      (render-crumbs (:crumbs opts))
      view)))
