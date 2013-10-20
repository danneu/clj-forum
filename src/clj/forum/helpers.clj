(ns forum.helpers
  (:require [clojure.string :as str]
            [hiccup.util :refer [*base-url*]]
            [forum.models :refer :all])
  (:import (org.ocpsoft.prettytime PrettyTime)))

;; AKA orphaned functions

(defn pretty-date
  "Turn Date into string of 'about 10 minutes ago'."
  [date]
  (.format (PrettyTime.) date))

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
