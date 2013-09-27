(ns forum.controllers.topics
  (:use [hiccup element page util])
  (:require [forum.db]
            [forum.views.topics]
            [forum.views.master :refer :all]
            [ring.util.response :refer [redirect]]))

(defn show [fuid tuid]
  (when-let [forum (forum.db/find-forum-by-uid fuid)]
    (when-let [topic (forum.db/find-topic-by-uid tuid)]
      (let [posts (:topic/posts topic)]
        ;; TODO: Put the sort-by in forum.db layer or something.
        ;; TODO: Create a url generator from resources.
        (layout
         {:crumbs [(link-to (url "/forums/" fuid)
                            (:forum/title forum))]}
         (forum.views.topics/show forum
                                  topic
                                  ;; Old posts come first
                                  (sort-by :post/uid < posts)))))))

(defn create [fuid topic]
  (when-let [forum (forum.db/find-forum-by-uid fuid)]
    (if-let [tuid (forum.db/create-topic fuid
                                         (:title topic)
                                         (:text (:post topic)))]
      (redirect (str "/forums/" fuid "/topics/" tuid))
      "Error creating thread. ^_^")))
