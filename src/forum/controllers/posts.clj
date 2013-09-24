(ns forum.controllers.posts
  (:require [forum.db]
            [ring.util.response :refer [redirect]]))

;; post: {:text ""}
(defn create [forumid topicid post]
  ;; TODO: Add post validation
  ;; FIXME: I sorta don't like having to remember to do the Long. casts in
  ;;        Controllers. Perhaps I should move to forum.db.
  (println (prn forumid) topicid post)
  (when-let [f (forum.db/get-forum (Long. forumid))]
    (when-let [t (forum.db/get-topic (Long. topicid))]
      (if-let [postid (forum.db/create-post (Long. topicid) (:text post))]
        (redirect (str "/forums/" forumid "/topics/" topicid))
        "Error creating post. ^_^"))))
