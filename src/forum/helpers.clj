(ns forum.helpers
  (:use [hiccup core page form element util])
  (:import [org.ocpsoft.prettytime PrettyTime]))

;; AKA orphaned functions

(defn parent-forum
  "Given topic entity, returns parent forum entity."
  [topic]
  (first (:forum/_topics topic)))

(defn parent-topic
  "Given post entity, returns parent topic entity."
  [post]
  (first (:topic/_posts post)))

(defn url-for
  "Here's my URL abstraction. Just pass in entity."
  [e]
  (cond
   (:user/uid e) (url "/users/" (:user/uid e))
   (:forum/uid e) (str "/forums/" (:forum/uid e))
   (:topic/uid e) (str (url-for (parent-forum e))
                       "/topics/" (:topic/uid e))
   (:post/uid e) (str (url-for (parent-topic e))
                      "/posts/" (:post/uid e))))

(defn latest-topic
  "Given a forum entity, returns latest topic
   by createdAt although it should be by latest post bump."
  [forum]
  (first (sort-by :topic/created
                  #(- ( .compareTo %1 %2))
                  (:forum/topics forum))))

(defn latest-post
  [topic]
  (last (sort-by :post/uid (:topic/posts topic))))

(defn entity-type
  [entity]
  (cond
   (:topic/uid entity) :topic
   (:post/uid entity) :post
   (:forum/uid entity) :forum
   (:user/uid entity) :user))

(defn creator
  [topic-or-post]
  (case (entity-type topic-or-post)
   :topic (first (:user/_topics topic-or-post))
   :post (first (:user/_posts topic-or-post))))

(defn pretty-date
  "Turn Date into string of 'about 10 minutes ago'."
  [date]
  (.format (PrettyTime.) date))


