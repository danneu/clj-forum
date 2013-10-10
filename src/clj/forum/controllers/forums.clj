(ns forum.controllers.forums
  (:require [forum.controllers.base :refer [load-base]]))
(load-base)

;; TODO: Should in presenter or something
(defn assoc-posts-count [forum]
  (let [pcount (db/forum-posts-count (:db/id forum))]
    (assoc (into {} forum) :posts-count pcount)))

(defn index []
  (let [forums (for [forum (forum.db/find-all-forums)]
                 (assoc-posts-count forum))]
    (forum.views.master/layout
     {:crumbs []}
     (forum.views.forums/index forums))))

(defn show [fuid]
  (when-let [forum (forum.db/find-forum-by-uid fuid)]
    (let [topics (:forum/topics forum)]
      (forum.views.master/layout
       {:crumbs [(:forum/title forum)]}
       (forum.views.forums/show
        forum (sort-by :topic/uid > topics))))))
