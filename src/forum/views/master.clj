(ns forum.views.master
  (:use [hiccup core element page util])
  (:import [org.ocpsoft.prettytime PrettyTime]))

(defn pretty-date
  "Turn Date into string of 'about 10 minutes ago'."
  [date]
  (.format (PrettyTime.) date))

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
      [:title "Forum"]
      (render-crumbs (:crumbs opts))
      view)))
