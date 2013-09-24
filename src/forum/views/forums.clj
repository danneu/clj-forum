(ns forum.views.forums
  (:use [hiccup core element util]))

(defn index [forums]
  (html
   [:h1 "Forums"]
   [:ul
    (for [f forums]
      [:li
       (link-to (url "/forums/" (:db/id f)) (:forum/title f))
       " - (" (count (:forum/topics f)) " topics)"])]))

(defn show [forum topics]
  (html
   [:h1 (:forum/title forum)]
   [:ul
    (for [t topics]
      [:li
       (link-to (url "/forums/" (:db/id forum) "/topics/" (:db/id t))
                (:topic/title t))
       ;; FIXME: Pulling posts out of topic. I think this is too much knowledge for view.
       " - (" (count (:topic/posts t)) " posts)"])]))
