(ns forum.db-test
  (:require [forum.db :refer :all]
            [expectations :refer :all]
            [datomic.api :as d]
            [clojure.java.io :as io])
  (:import [datomic.Util]))

(defn create-test-db []
  (with-redefs [uri "datomic:mem://forum-test"]
    (create-db)))

;; FORUM ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; create-forum should create a forum entity
(expect #{["Forum 1"]}
        (with-redefs [conn (create-test-db)]
          (create-forum "Forum 1")
          (q '[:find ?title
               :where [_ :forum/title ?title]])))

;; get-all-forums should return all the forum entities
(expect #{"Forum A" "Forum B" "Forum C"}
        (with-redefs [conn (create-test-db)]
          (let [forum1 (create-forum "Forum A")
                forum2 (create-forum "Forum B")
                forum3 (create-forum "Forum C")]
            (set (map :forum/title (get-all-forums))))))

;; get-forum should return single forum
(expect "Forum 1"
        (with-redefs [conn (create-test-db)]
          (let [forum1 (create-forum "Forum 1")]
            (:forum/title (get-forum forum1)))))

;; TOPIC CREATION ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; create-topic should create a topic entity
(expect #{["Topic 1"]}
        (with-redefs [conn (create-test-db)]
          (create-topic (create-forum "Forum 1") "Topic 1")
          (q '[:find ?title
               :where [_ :topic/title ?title]])))


;; create-topic should increment :topic/position
(expect #{["Topic 1" 1] ["Topic 2" 2] ["Topic 3" 3]}
        (with-redefs [conn (create-test-db)]
          (let [forumid (create-forum "Forum 1")]
            (create-topic forumid "Topic 1")
            (create-topic forumid "Topic 2")
            (create-topic forumid "Topic 3")
            (q '[:find ?title ?pos
                 :where [?t :topic/title ?title]
                        [?t :topic/position ?pos]]))))

;; create-topic should increment :topic/position separately within different forums
(expect #{["Topic 1" 1] ["Topic 2" 2] ["Topic 3" 1] ["Topic 4" 2]}
        (with-redefs [conn (create-test-db)]
          (let [forum1 (create-forum "Forum 1")
                forum2 (create-forum "Forum 2")]
            (create-topic forum1 "Topic 1")
            (create-topic forum1 "Topic 2")
            (create-topic forum2 "Topic 3")
            (create-topic forum2 "Topic 4")
            (q '[:find ?title ?pos
                 :where [?t :topic/title ?title]
                        [?t :topic/position ?pos]]))))

;; create-topic should add topic to :forum/topics
(expect [[3]]
        (with-redefs [conn (create-test-db)]
          (let [forum1 (create-forum "Forum 1")]
            (create-topic forum1 "Topic 1")
            (create-topic forum1 "Topic 2")
            (create-topic forum1 "Topic 3")
            (d/q '[:find (count ?ts)
                   :in $ ?f
                   :where [?f :forum/topics ?ts]]
                 (d/db conn)
                 forum1))))

;; POST CREATION ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; create-post should create a post
(expect #{["Post 1"]}
        (with-redefs [conn (create-test-db)]
          (let [forumid (create-forum "Forum 1")
                topicid (create-topic forumid "Topic 1")]
            (create-post topicid "Post 1")
            (q '[:find ?text
                 :where [_ :post/text ?text]]))))
