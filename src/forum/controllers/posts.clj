(ns forum.controllers.posts
  (:require [forum.db]
            [ring.util.response :refer [redirect]]
            [forum.middleware.wrap-current-user
             :refer [current-user]]))

;; post: {:text ""}
(defn create [fuid tuid post-params]
  ;; TODO: Add post validation
  (when-let [forum (forum.db/find-forum-by-uid fuid)]
    (when-let [topic (forum.db/find-topic-by-uid tuid)]
      (let [user-uid (:user/uid current-user)]
        (if-let [puid (forum.db/create-post
                       user-uid tuid (:text post-params))]
          (redirect (str "/forums/" fuid "/topics/" tuid))
          "Error creating post. ^_^")))))
