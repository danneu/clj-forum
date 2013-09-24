(ns forum.controllers.topics
  (:use [hiccup element page util])
  (:require [forum.db]
            [forum.views.topics]
            [forum.views.master :refer :all]
            [ring.util.response :refer [redirect]]))

(defn show [forumid topicid]
  (when-let [forum (forum.db/get-forum (Long. forumid))]
    (when-let [topic (forum.db/get-topic (Long. topicid))]
      (let [posts (:topic/posts topic)]
        ;; TODO: Put the sort-by in forum.db layer or something.
        (layout
         {:crumbs [(link-to (url "/forums/" (:db/id forum))
                            (:forum/title forum))]}
         (forum.views.topics/show forum
                                  topic
                                  (sort-by :post/position
                                           posts)))))))

(defn create [forumid topic]
  (when-let [f (forum.db/get-forum (Long. forumid))]
    (if-let [topicid (forum.db/create-topic (Long. forumid)
                                            (:title topic)
                                            (:text (:post topic)))]
      (redirect (str "/forums/" forumid "/topics/" topicid))
      "Error creating thread. ^_^")))
