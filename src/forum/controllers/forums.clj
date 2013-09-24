(ns forum.controllers.forums
  (:require [forum.db]
            [forum.views.forums]
            [forum.views.master :refer :all]))

(defn index []
  (let [fs (forum.db/get-all-forums)]
    (layout (forum.views.forums/index fs))))

(defn show [forumid]
  (when-let [forum (forum.db/get-forum (Long. forumid))]
    (let [topics (:forum/topics forum)]
      (layout
       {:crumbs []}
       (forum.views.forums/show forum
                                (sort-by :topic/position topics))))))
