(ns forum.controllers.posts
  (:require [forum.db]
            [ring.util.response :refer [redirect]]))

;; post: {:text ""}
(defn create [fuid tuid post-params]
  ;; TODO: Add post validation
  (when-let [forum (forum.db/find-forum-by-uid fuid)]
    (when-let [topic (forum.db/find-topic-by-uid tuid)]
      (if-let [puid (forum.db/create-post tuid (:text post-params))]
        (redirect (str "/forums/" fuid "/topics/" tuid))
        "Error creating post. ^_^"))))
