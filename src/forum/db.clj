(ns forum.db
  (:require [datomic.api :as d]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]])
  (:import [datomic Util]
           [java.util Date]
           [org.ocpsoft.prettytime PrettyTime]))

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

(def uri "datomic:free://localhost:4334/forum")

(defn create-db []
  (d/delete-database uri)
  (d/create-database uri)
  (let [conn (d/connect uri)]
    (transact-all conn "resources/schema.edn")
    (transact-all conn "resources/data-functions.edn")
    conn))

;; If DB is migrated just return conn, else seed it.
(def conn (d/connect uri))

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
  [forumid title text]
  (let [topicid (tempid)
        postid (tempid)
        now (Date.)]
    (let [tx-result
          @(d/transact conn
                       [;; Make topic
                        {:db/id topicid
                         :topic/title title
                         :topic/createdAt now}
                        [:addTopicPosition forumid topicid]
                        {:db/id forumid :forum/topics topicid}

                        ;; Make post
                        {:db/id postid
                         :post/text text
                         :post/createdAt now}
                        [:addPostPosition topicid postid]
                        {:db/id topicid :topic/posts postid}])]
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
                                 [{:db/id postid
                                   :post/text text
                                   :post/createdAt (Date.)}
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

(defn generate-sentence
  []
  (as-> (+ 2 (rand-int 5)) _
        (take _ (repeatedly generate-word))
        (clojure.string/join " " _)
        (clojure.string/capitalize _)
        (str _ ".")))

(defn seed-db [conn topics-per-forum posts-per-topic]
  (let [forums (map create-forum ["Forum A" "Forum B" "Forum C"])
        topics (flatten
                (repeatedly
                 topics-per-forum
                 #(for [forum forums]
                    (create-topic forum
                                  (generate-sentence)))))]
    (doseq [topic topics]
      (dotimes [_ posts-per-topic]
        (create-post
         topic
         (clojure.string/join
          " "
          (take 3 (repeatedly generate-sentence))))))))

(defn get-all-topics []
  (let [res (d/q '[:find ?t
                   :where [?t :topic/title]]
                 (d/db conn))]
    (map (comp entity first) res)))

(defn get-all-posts []
  (let [res (d/q '[:find ?t
                   :where [?t :post/text]]
                 (d/db conn))]
    (map (comp entity first) res)))

;; Seed check ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(when (empty? (d/q '[:find ?e
                   :where [?e :db/ident :topic/createdAt]]
                 (d/db conn)))
  (let [conn (create-db)]
    (seed-db conn)))

(count (get-all-posts))
