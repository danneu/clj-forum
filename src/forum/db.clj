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

(defn get-all-forums
  "Returns entities"
  []
  (let [res (q '[:find ?f
                 :where [?f :forum/title]])]
    (map (comp entity first) res)))

(defn get-forum
  "Returns entity or nil"
  [forumid]
  (let [res (d/q '[:find ?f
                   :in $ ?f
                   :where [?f :forum/title]]
                 (d/db conn)
                 forumid)]
    (entity (ffirst res))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Creation functions should return eids

(defn create-forum
  "Returns entity ID of created forum."
  [title]
  (let [tx-result @(d/transact conn
                               [{:db/id (tempid)
                                 :forum/title title}])]
    (:e (last (:tx-data tx-result)))))

(defn create-topic
  "Returns entity ID of created topic."
  [forumid title]
  (let [topicid (tempid)]
    (let [tx-result @(d/transact conn
                                 [{:db/id topicid :topic/title title}
                                  [:addTopicPosition forumid topicid]
                                  {:db/id forumid :forum/topics topicid}])]
      ;; Get eid of the topic created
      (:e (first (filter #(= title (:v %)) (:tx-data tx-result)))))))

(defn get-topic
  "Returns entity or nil"
  [topicid]
  (let [res (d/q '[:find ?t
                   :in $ ?t
                   :where [?t :topic/title]]
                 (d/db conn)
                 topicid)]
    (entity (ffirst res))))

(defn create-post
  "Returns entity ID of created post."
  [topicid text]
  (let [postid (tempid)]
    (let [tx-result @(d/transact conn
                                 [{:db/id postid :post/text text}
                                  [:addPostPosition topicid postid]
                                  {:db/id topicid :topic/posts postid}])]
      ;; Get eid of the post created
      (:e (first (filter #(= text (:v %)) (:tx-data tx-result)))))))

;; Seed the DB ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Noise generators

(defn generate-word
  "Generates a word 3 to 7 letters long."
  []
  (clojure.string/join
   (take (+ 3 (rand-int 5))
         (repeatedly #(rand-nth "abcdefghijklmnopqrstuvwxyz")))))

(defn generate-post-text
  "Generates a string of words between 7 and 27 words long."
  []
  (as-> (+ 7 (rand-int 21)) _
        (take _ (repeatedly generate-word))
        (clojure.string/join " " _)
        (clojure.string/capitalize _)
        (str _ ".")))

;; FIXME: Generalize seed-db so I can scale it.

(defn seed-db [conn]
  (let [forum1 (create-forum "Forum A")
        forum2 (create-forum "Forum B")
        forum3 (create-forum "Forum C")]
    (let [forum1-topics (map (partial create-topic forum1)
                             ["Topic 1" "Topic 2" "Topic 3"])
          forum2-topics (map (partial create-topic forum2)
                             ["Topic 4" "Topic 5" "Topic 6"])
          forum3-topics (map (partial create-topic forum3)
                             ["Topic 7" "Topic 8" "Topic 9"])]
      (doseq [t (flatten [forum1-topics forum2-topics forum3-topics])]
        (dotimes [_ 3] (create-post t (generate-post-text)))))))

(seed-db conn)
