(ns forum.controllers.posts
  (:require [forum.controllers.base :refer :all]))

(load-base)

(defn edit [fuid tuid puid]
  (when-let [forum (db/find-forum-by-uid fuid)]
    (when-let [topic (db/find-topic-by-uid tuid)]
      (when-let [post (db/find-post-by-uid puid)]
        (if (cannot *current-user* :edit post)
          (redirect-unauthorized)
          (forum.views.master/layout
           {:crumbs [(link-to (url-for forum) (:forum/title forum))
                     (link-to (url-for topic) (:topic/title topic))
                     "Edit Post"]}
           (forum.views.posts/edit topic post)))))))

(defn update [puid {text :text}]
  ;; Do validation
  (when-let [post (db/find-post-by-uid puid)]
    (if (cannot *current-user* :update post)
      (redirect-unauthorized)
      ;; Due to my created-entity fn, update-post returns nil if
      ;; the update didn't actually change the text of the post.
      ;; TODO: Figure out what I wanna to do.
      (if (db/update-post puid text)
        (-> (redirect (url-for (parent-topic post)
                               "#post-"
                               (:post/uid post)))
            (assoc :flash [:success "Successfully edited post"]))
        ":("))))

;; post: {:text ""}
(defn create [fuid tuid post-params]
  (if (cannot *current-user* :create :post)
    (redirect-unauthorized)
    ;; TODO: Add post validation
    (when-let [forum (forum.db/find-forum-by-uid fuid)]
      (when-let [topic (forum.db/find-topic-by-uid tuid)]
        (when-let [user-uid (:user/uid *current-user*)]
          (if-let [errors (forum.validation/post-errors post-params)]
            (-> (redirect (make-url "/forums/" fuid "/topics/" tuid))
                (assoc :flash [:danger (for [error errors]
                                         [:li error])]))
            ;; For now I expect transactor to throw exception
            ;; if create-post fails.
            ;; TODO: Figure out how I should handle such an event.
            (when-let [puid (db/create-post
                             user-uid
                             tuid
                             (:text post-params))]
              (-> (redirect (str "/forums/" fuid
                                 "/topics/" tuid
                                 "#post-" puid))
                  (assoc :flash [:success "Successfully posted."])))))))))
