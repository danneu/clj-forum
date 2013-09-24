(ns forum.db-test
  (:require [forum.db :refer :all]
            [expectations :refer :all]
            [datomic.api :as d]
            [clojure.java.io :as io])
  (:import [datomic.Util]))

(defn create-test-db []
  (with-redefs [uri "datomic:mem://forum-test"]
    (create-db)))

;; Can create a forum
(expect #{["Forum A" "Desc" 1]}
        (with-redefs [conn (create-test-db)]
          (create-forum "Forum A" "Desc")
          (d/q '[:find ?title ?desc ?uid
                 :where [?f :forum/title ?title]
                        [?f :forum/desc ?desc]
                        [?f :forum/uid ?uid]]
               (d/db conn))))

;; Forum uid increments
(expect #{["Forum A" "Desc" 1]
          ["Forum B" "Desc" 2]}
        (with-redefs [conn (create-test-db)]
          (create-forum "Forum A" "Desc")
          (create-forum "Forum B" "Desc")
          (d/q '[:find ?title ?desc ?uid
                 :where [?f :forum/title ?title]
                        [?f :forum/desc ?desc]
                        [?f :forum/uid ?uid]]
               (d/db conn))))

;; Topics inc uid
(expect #{["Topic1" 1] ["Topic2" 2] ["Topic3" 3]}
        (with-redefs [conn (create-test-db)]
          (let [f1 (create-forum "Forum1" "Desc")
                t1 (create-topic f1 "Topic1" "Text")
                t2 (create-topic f1 "Topic2" "Text")
                t3 (create-topic f1 "Topic3" "Text")
                ])
          (d/q '[:find ?title ?uid
                 :where
                 [?e :topic/title ?title]
                 [?e :topic/uid ?uid]]
               (d/db conn))))

;; Topics inc uid
(expect #{["A" 1] ["B" 2] ["C" 3] ["D" 4]}
        (with-redefs [conn (create-test-db)]
          (let [t1 (create-topic (tempid) "Topic1" "A")
                p1 (create-post t1 "B")
                p2 (create-post t1 "C")
                p3 (create-post t1 "D")
                ])
          (d/q '[:find ?text ?uid
                 :where
                 [?e :post/text ?text]
                 [?e :post/uid ?uid]]
               (d/db conn))))

;; Query functions

(expect #{"Forum A" "Forum B" "Forum C"}
        (with-redefs [conn (create-test-db)]
          (let [forum1 (create-forum "Forum A" "")
                forum2 (create-forum "Forum B" "")
                forum3 (create-forum "Forum C" "")]
            (set (map :forum/title (get-all-forums))))))

;; get-forum should return single forum
(expect "Forum 1"
        (with-redefs [conn (create-test-db)]
          (let [forum1 (create-forum "Forum 1" "")]
            (:forum/title (get-forum forum1)))))

;; ;; TOPIC ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; create-topic should create a topic entity
(expect #{["Topic 1"]}
        (with-redefs [conn (create-test-db)]
          (create-topic (create-forum "Forum 1" "") "Topic 1" "Post text")
          (q '[:find ?title
               :where [_ :topic/title ?title]])))

;; get-topic should return single topic
(expect "Topic 1"
        (with-redefs [conn (create-test-db)]
          (let [forum1 (create-forum "Forum 1" "")
                topic1 (create-topic forum1 "Topic 1" "")]
            (:topic/title (get-topic topic1)))))

;; create-topic should add topic to :forum/topics
(expect [[3]]
        (with-redefs [conn (create-test-db)]
          (let [forum1 (create-forum "Forum 1" "")]
            (create-topic forum1 "Topic 1" "")
            (create-topic forum1 "Topic 2" "")
            (create-topic forum1 "Topic 3" "")
            (d/q '[:find (count ?ts)
                   :in $ ?f
                   :where [?f :forum/topics ?ts]]
                 (d/db conn)
                 forum1))))

;; ;; POST CREATION ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; create-post should create a post
(expect #{["Post 1"] ["Post 2"]}
        (with-redefs [conn (create-test-db)]
          (let [forumid (create-forum "Forum 1" "")
                topicid (create-topic forumid "Topic 1" "Post 1")]
            (create-post topicid "Post 2")
            (q '[:find ?text
                 :where [_ :post/text ?text]]))))
