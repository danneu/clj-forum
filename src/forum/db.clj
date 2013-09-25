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
;; (def conn (create-db))

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
  [fuid]
  (let [res (d/q '[:find ?f
                   :in $ ?fuid
                   :where [?f :forum/uid ?fuid]]
                 (d/db conn)
                 fuid)]
    (entity (ffirst res))))

;; User ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn create-user [uname pwd]
  (let [digest (forum.authentication/encrypt pwd)
        ueid (tempid)
        res @(d/transact conn
                         [{:db/id ueid
                           :user/uname uname
                           :user/digest digest
                           :user/created (Date.)}
                          [:addUID ueid :user/uid]])]
    res))

(defn get-user-by-uname [uname]
  (let [res (d/q '[:find ?u
                   :in $ ?uname
                   :where [?u :user/uname ?uname]]
                 (d/db conn)
                 uname)]
    (entity (ffirst res))))

(defn get-user-by-uid [uid]
  (let [res (d/q '[:find ?u
                   :in $ ?uid
                   :where [?u :user/uid ?uid]]
                 (d/db conn)
                 uid)]
    (entity (ffirst res))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Creation functions should return uids

(defn create-forum
  "Returns uid of created forum."
  [title desc]
  (let [feid (tempid)
        tx-result @(d/transact conn
                               [{:db/id feid
                                 :forum/title title
                                 :forum/desc desc}
                                [:addUID feid :forum/uid]])]
    (:forum/uid (entity (:e (last (:tx-data tx-result)))))))

(defn create-topic
  "Returns uid of created topic."
  [fuid title text]
  (let [teid (tempid)
        peid (tempid)
        now (Date.)]
    (let [tx-result
          @(d/transact conn
                       [;; Make topic
                        {:db/id teid
                         :topic/title title
                         :topic/created now}
                        [:addUID teid :topic/uid]

                        ;; Add topic to forum
                        {:forum/uid fuid :forum/topics teid :db/id (tempid)}

                        ;; Make post
                        {:db/id peid
                         :post/text text
                         :post/created now}
                        [:addUID peid :post/uid]
                        {:db/id teid :topic/posts peid}])]
      ;; Get uid of the topic created
      (:topic/uid (entity (:e (first (filter #(= title (:v %)) (:tx-data tx-result)))))))))

(defn get-topic
  "Returns entity or nil"
  [tuid]
  (let [res (d/q '[:find ?t
                   :in $ ?tuid
                   :where [?t :topic/uid ?tuid]]
                 (d/db conn)
                 tuid)]
    (entity (ffirst res))))

(defn create-post
  "Returns uid of created post."
  [tuid text]
  (let [peid (tempid)]
    (let [tx-result @(d/transact conn
                                 ;; Create post
                                 [{:db/id peid
                                   :post/text text
                                   :post/created (Date.)}
                                  [:addUID peid :post/uid]
                                  ;; Add post to topic with this tuid
                                  {:topic/uid tuid :topic/posts peid :db/id (tempid)}])]
      ;; Get uid of the post created
      (:post/uid (entity (:e (first (filter #(= text (:v %)) (:tx-data tx-result)))))))))

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
  (let [forums (for [[title desc]
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
                    (create-topic forum
                                  (generate-sentence)
                                  (generate-sentence)))))]
    (doseq [topic topics]
      (dotimes [_ posts-per-topic]
        (create-post
         topic
         (clojure.string/join
          " "
          (take 3 (repeatedly generate-sentence))))))
    conn))

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

(let [wipe? false]
  (when (or (> 1 (count (get-all-posts)))
            wipe?)
    ;; Can I think of a better solution than redef?
    (def conn (create-db))
    (seed-db conn 5 5)))

;; Show total post count on file eval (sanity check)

(let [posts (get-all-posts)]
  {:post-count (count posts)
   :latest-post ((comp :post/created first) (sort-by :post/uid #(> %1 %2) (get-all-posts)))
})
