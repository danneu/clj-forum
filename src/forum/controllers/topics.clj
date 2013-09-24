(ns forum.controllers.topics
  (:require [forum.db]
            [forum.views.topics]))

(defn show [forumid topicid]
  (when-let [f (forum.db/get-forum (Long. forumid))]
    (when-let [t (forum.db/get-topic (Long. topicid))]
      (let [ps (:topic/posts t)]
        (forum.views.topics/show f t ps)))))
