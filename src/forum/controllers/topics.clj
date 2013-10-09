(ns forum.controllers.topics
  (:require [forum.controllers.base :refer :all]))
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

(defn create [fuid topic-params]
  (if (cannot *current-user* :create :topic)
    (redirect-unauthorized)
    (when-let [forum (forum.db/find-forum-by-uid fuid)]
      (when-let [user-uid (:user/uid *current-user*)]
        (if-let [errors (forum.validation/topic-errors topic-params)]
          (-> (redirect (url-for forum))
              (assoc :flash [:danger (for [error errors]
                                       [:li error])]))
          (when-let [tuid (db/create-topic user-uid
                                           fuid
                                           (:title topic-params)
                                           (:text topic-params))]
            (-> (redirect (make-url "/forums/" fuid
                                    "/topics/" tuid))
                (assoc :flash [:success "Successfully created topic."]))))))))
