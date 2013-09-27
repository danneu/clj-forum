(ns forum.controllers.forums
  (:require [forum.db]
            [forum.views.forums]
            [forum.views.master :refer :all]))

(defn index []
  (let [forums (forum.db/find-all-forums)]
    (layout (forum.views.forums/index forums))))

(defn show [fuid]
  (when-let [forum (forum.db/find-forum-by-uid fuid)]
    (let [topics (:forum/topics forum)]
      (layout
       {:crumbs []}
       (forum.views.forums/show forum
                                (sort-by :topic/uid > topics))))))
