(ns forum.controllers.topics
  (:require [forum.controllers.base :refer [load-base]]))
(load-base)

(defn show [fuid tuid]
  (when-let [forum (forum.db/find-forum-by-uid fuid)]
    (when-let [topic (forum.db/find-topic-by-uid tuid)]
      (let [posts (:topic/posts topic)]
        ;; TODO: Put the sort-by in forum.db layer or something.
        ;; TODO: Create a url generator from resources.
        (forum.views.master/layout
         {:crumbs [(link-to (url "/forums/" fuid)
                            (:forum/title forum))
                   (:topic/title topic)]}
         (forum.views.topics/show forum
                                  topic
                                  ;; Old posts come first
                                  (sort-by :post/uid < posts)))))))

(defn create [fuid topic]
  (when-let [forum (forum.db/find-forum-by-uid fuid)]
    (if-let [tuid (forum.db/create-topic
                   (:user/uid current-user)
                   fuid
                   (:title topic)
                   (:text (:post topic)))]
      (redirect (str "/forums/" fuid "/topics/" tuid))
      "Error creating thread. ^_^")))
