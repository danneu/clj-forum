(ns forum.db
  (:require [datomic.api :as d]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]]
            [forum.helpers :refer :all]
            [forum.authentication])
  (:import [datomic Util]
           [java.util Date]))

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

(defn create-test-db []
  (with-redefs [uri "datomic:mem://forum-test"]
    (create-db)))

(def conn (d/connect uri))

(defn tempid []
  (d/tempid :db.part/user))

(defn only
  "Returns the only item from a query result"
  [query-result]
  (ffirst query-result))

(defn qe
  "Returns the single entity returned from a query"
  [query db & args]
  (let [result (apply d/q query db args)]
    (d/entity db (only result))))

(defn find-by
  "Returns the unique entity identified by attr and val.
   Ex: (find-by :user/uid 42)"
  [db attr val]
  (qe '[:find ?e
        :in $ ?attr ?val
        :where [?e ?attr ?val]]
      db attr val))

(defn qes
  "Returns the entities returned by a query, assuming that
   all :find results are entity ids."
  [query db & args]
  (->> (apply d/q query db args)
       (mapv (fn [items]
               (mapv (partial d/entity db) items)))))

(defn find-all-by
  "Returns all entities possessing attr."
  [db attr]
  (qes '[:find ?e
         :in $ ?attr
         :where [?e ?attr]]
       db attr))

(defn qfs
  "Returns the first of each query result"
  [query db & args]
  (->> (apply d/q query db args)
       (mapv first)))

;; Finders - Return one or many entities ;;;;;;;;;;;;;;;;;;;;
;;
;; Forms:
;; 
;; - find-all-X []
;; - find-X-by-Y [y]

(defn find-all-forums []
  (flatten (find-all-by (d/db conn) :forum/uid)))

(defn find-all-users []
  (flatten (find-all-by (d/db conn) :user/uid)))

(defn find-all-topics []
  (flatten (find-all-by (d/db conn) :topic/uid)))

(defn find-all-posts []
  (flatten (find-all-by (d/db conn) :post/uid)))

(defn find-forum-by-uid [uid]
  (find-by (d/db conn) :forum/uid uid))

(defn find-user-by-uid [uid]
  (find-by (d/db conn) :user/uid uid))

(defn find-user-by-uname [uname]
  (find-by (d/db conn) :user/uname uname))

(defn find-topic-by-uid [uid]
  (find-by (d/db conn) :topic/uid uid))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TODO: This is roundabout. Can I think of something better?
(defn- created-entity
  "Returns the entity created from a transaction (or nil)
   where `tx-result` is the result of a d/transact call.
   `attr` and `val` are used to look through the created Datoms.
   Ex: (created-entity res :user/uname \"danneu\")"
  [tx-result attr val]
  (d/entity (:db-after tx-result)
            (:e (first (filter #(= val (:v %))
                               (:tx-data tx-result))))))

(defn create-user
  "Returns uid or nil."
  [uname pwd]
  (let [digest (forum.authentication/encrypt pwd)
        eid (tempid)
        result @(d/transact
                 conn [[:user/construct eid uname digest]])]
    (:user/uid (created-entity result :user/uname uname))))

(defn create-user
  "Returns uid or nil."
  [uname pwd]
  (let [digest (forum.authentication/encrypt pwd)
        eid (tempid)
        result @(d/transact
                 conn [[:user/construct eid uname digest]])]
    (:user/uid (created-entity result :user/uname uname))))

(defn create-forum
  "Returns uid or nil."
  [title desc]
  (let [eid (tempid)
        result @(d/transact
                 conn [[:forum/construct eid title desc]])]
    (:forum/uid (created-entity result :forum/title title))))

(defn create-topic
  "Returns uid or nil"
  [user-uid fuid title text]
  (let [result @(d/transact conn
                            [[:topic/construct
                              user-uid fuid title text]])]
    (:topic/uid (created-entity result :topic/title title))))

(defn create-post
  "Return uid or nil"
  [user-uid tuid text]
  (let [result @(d/transact
                 conn
                 [[:post/construct user-uid tuid text]])]
    (:post/uid (created-entity result :post/text text))))

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
  (let [danneu-uid (create-user "danneu" "catdog")
        forums (for [[title desc]
                     [["Newbie Introductions"
                       "Are you new here? Then introduce yourself!"]
                      ["General Discussion"
                       "Talk about whatever and just kick it."]
                      ["Suggestions/Support"
                       "Need help or have an idea to improve the place? Come share."]]]
                 (create-forum title desc))
        topics (flatten
                (repeatedly
                 topics-per-forum
                 #(for [forum forums]
                    (create-topic danneu-uid
                                  forum
                                  (generate-sentence)
                                  (generate-sentence)))))]
    (doseq [topic topics]
      (dotimes [_ posts-per-topic]
        (create-post
         danneu-uid
         topic
         (clojure.string/join
          " "
          (take 3 (repeatedly generate-sentence))))))
    conn))

;; Seed check ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn seed
  "To be run from cli: lein run -m forum.db/seed"
  []
  (def conn (create-db))
  (seed-db conn 5 5)
  (let [posts (find-all-posts)]
    (prn {:post-count (count posts)
          :latest-post ((comp :post/created first) (sort-by :post/uid #(> %1 %2) (find-all-posts)))})
    :done))
