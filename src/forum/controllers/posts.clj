(ns forum.controllers.posts
  (:require [forum.controllers.base :refer [load-base]]))
(load-base)

(defn edit [fuid tuid puid]
  (when-let [forum (db/find-forum-by-uid fuid)]
    (when-let [topic (db/find-topic-by-uid tuid)]
      (when-let [post (db/find-post-by-uid puid)]
        (forum.views.master/layout
         {:crumbs [(link-to (url-for forum) (:forum/title forum))
                   (link-to (url-for topic) (:topic/title topic))
                   "Edit Post"]}
         (forum.views.posts/edit topic post))))))

(defn update [puid {text :text}]
  ;; Do validation
  (when-let [post (db/find-post-by-uid puid)]
    ;; Due to my created-entity fn, update-post returns nil if
    ;; the update didn't actually change the text of the post.
    ;; TODO: Figure out what I wanna to do.
    (if (db/update-post puid text)
      (-> (redirect (url-for (parent-topic post) "#post-" (:post/uid post)))
          (assoc :flash [:success "Successfully edited post"]))
      ":)")))

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
