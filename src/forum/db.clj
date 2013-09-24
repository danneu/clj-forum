(ns forum.db
  (:require [datomic.api :as d]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]])
  (:import [datomic Util]))

;; Utils ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn read-all
  "Read all forms in f, where f is any resource that can
be opened by io/reader"
  [f]
  (Util/readAll (io/reader f)))

(defn transact-all
  "Load and run all transactions from f, where f is any
resource that can be opened by io/reader."
  [conn f]
  (doseq [txd (read-all f)]
    (d/transact conn txd))
  :done)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def uri "datomic:mem://forum")

(defn create-db []
  (d/delete-database uri)
  (d/create-database uri)
  (let [conn (d/connect uri)]
    (transact-all conn "resources/schema.edn")
    (transact-all conn "resources/data-functions.edn")
    conn))

(def conn (create-db))

(defn tempid []
  (d/tempid :db.part/user))

(defn q [query]
  (d/q query (d/db conn)))

(defn entity [e]
  (d/entity (d/db conn) e))

;; GET functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Should return entities

(defn get-all-forums []
  (let [res (q '[:find ?f
                 :where [?f :forum/title]])]
    (map (comp entity first) res)))

(defn get-forum
  "Returns entity or nil"
  [forumid]
  (let [res (d/q '[:find ?f
                   :in $ ?f
                   :where [?f]]
                 (d/db conn)
                 forumid)]
    (entity (ffirst res))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Creation functions should return eids

(defn create-forum [title]
  (let [tx-result @(d/transact conn
                               [{:db/id (tempid)
                                 :forum/title title}])]
    (:e (last (:tx-data tx-result)))))

(defn create-topic [forumid title]
  (let [topicid (tempid)]
    (let [tx-result @(d/transact conn
                                 [{:db/id topicid :topic/title title}
                                  [:bumpPosition
                                   forumid
                                   :forum/topics
                                   topicid
                                   :topic/position]
                                  {:db/id forumid :forum/topics topicid}])]
      ;; Get eid of the topic created
      (:e (first (filter #(= title (:v %)) (:tx-data tx-result)))))))

(defn create-post [topicid text]
  (let [postid (tempid)]
    (let [tx-result @(d/transact conn
                                 [{:db/id postid :post/text text}
                                  [:bumpPosition
                                   topicid
                                   :topic/posts
                                   postid
                                   :post/position]
                                  {:db/id topicid :topic/posts postid}])]
      ;; Get eid of the post created
      (:e (first (filter #(= text (:v %)) (:tx-data tx-result)))))))

(defn get-all-forums []
  (let [res (q '[:find ?f
                 :where [?f :forum/title]])]
    (map (comp d/touch entity first) res)))

(defn get-all-topics []
  (let [res (q '[:find ?title ?position
                 :where [?t :topic/title ?title]
                        [?t :topic/position ?position]])]
    res))

;; Seed the DB

(defn seed-db [conn]
  (let [forum1 (create-forum "Forum A")
        forum2 (create-forum "Forum B")
        forum3 (create-forum "Forum C")]
    (create-topic forum1 "Topic 1")
    (create-topic forum1 "Topic 2")
    (create-topic forum1 "Topic 3")
    (create-topic forum2 "Topic 4")
    (create-topic forum2 "Topic 5")
    (create-topic forum2 "Topic 6")
    (create-topic forum3 "Topic 7")
    (create-topic forum3 "Topic 8")
    (create-topic forum3 "Topic 9")))

(seed-db conn)
