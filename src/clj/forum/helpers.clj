(ns forum.helpers
  (:require [clojure.string :as str] [hiccup.util :refer [*base-url*]])
  (:import (org.ocpsoft.prettytime PrettyTime)))

;; AKA orphaned functions

(defn posts-asc [topic]
  (sort-by :post/uid < (:topic/posts topic)))

(defn first-post
  "Returns the OP's first post in a topic."
  [topic]
  (first (posts-asc topic)))

(defn first-post?
  "Is this the OP's first post in this topic?"
  [post]
  (let [topic (first (:topic/_posts post))]
    (= post (first-post topic))))

(defn parent-forum
  "Given topic entity, returns parent forum entity."
  [topic]
  (first (:forum/_topics topic)))

(defn parent-topic
  "Given post entity, returns parent topic entity."
  [post]
  (first (:topic/_posts post)))

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

(defn pretty-date
  "Turn Date into string of 'about 10 minutes ago'."
  [date]
  (.format (PrettyTime.) date))

(defn entity-type
  [entity]
  (cond
   (:forum/uid entity) :forum
   (:topic/uid entity) :topic
   (:post/uid entity)  :post
   (:user/uid entity)  :user
   ;; For cancan so that :forum can represent any forum.
   (= :forum entity) :forum
   (= :topic entity) :topic
   (= :post entity)  :post
   (= :user entity)  :user))

(defn creator
  [topic-or-post]
  (case (entity-type topic-or-post)
   :topic (first (:user/_topics topic-or-post))
   :post (first (:user/_posts topic-or-post))))

(defn logged-in? [current-user]
  (:user/uid current-user))

(def logged-out?
  (complement logged-in?))

;; URL resolution ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn path-for
  "Returns the path of an entity.
   /forums/1
   /forums/1/topics/3
   /forums/1/topics/3/posts/9"
  [e & args]
  (let [base-path
        (cond
         (:user/uid e) (str "/users/" (:user/uid e))
         (:forum/uid e) (str "/forums/" (:forum/uid e))
         (:topic/uid e) (str (path-for (parent-forum e))
                             "/topics/" (:topic/uid e))
         (:post/uid e) (str (path-for (parent-topic e))
                            "/posts/" (:post/uid e)))]
    (str base-path (str/join args))))

(defn url-for
  "Returns path of an entity concat with base url.
   http://base-url.com/forums/1
   http://base-url.com/forums/1/topics/3
   http://base-url.com/forums/1/topics/3/posts/9 "
  [e & args]
  (str *base-url* (apply path-for e args)))

(defn make-url
  "Concats arbitrary strings with *base-url*."
  [& args]
  (str *base-url* (str/join args)))
