(ns forum.helpers
  (:use [hiccup core page form element util])
  (:import [org.ocpsoft.prettytime PrettyTime]))

;; AKA orphaned functions

(defn parent-forum
  "Given topic entity, returns parent forum entity."
  [topic]
  (first (:forum/_topics topic)))

(defn url-for
  "Here's my URL abstraction. Just pass in entity."
  [e]
  (cond
   (:user/uid e) (url "/users/" (:user/uid e))
   (:forum/uid e) (str "/forums/" (:forum/uid e))
   (:topic/uid e) (str (url-for (parent-forum e))
                       "/topics/" (:topic/uid e))))

(defn latest-topic
  "Given a forum entity, returns latest topic
   by createdAt although it should be by latest post bump."
  [forum]
  (first (sort-by :topic/created
                  #(- ( .compareTo %1 %2))
                  (:forum/topics forum))))

(defn pretty-date
  "Turn Date into string of 'about 10 minutes ago'."
  [date]
  (.format (PrettyTime.) date))


