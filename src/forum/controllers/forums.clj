(ns forum.controllers.forums
  (:require [forum.db]
            [forum.views.forums]))

(defn index []
  (let [fs (forum.db/get-all-forums)]
    (forum.views.forums/index fs)))

(defn show [forumid]
  (when-let [forum (forum.db/get-forum (Long. forumid))]
    (let [topics (:forum/topics forum)]
      (forum.views.forums/show forum topics))))
