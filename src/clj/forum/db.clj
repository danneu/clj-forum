(ns forum.db
  (:require [clojure.string :as str]
            [clojure.pprint :refer [pprint]]
            [clojure.java.io :as io]
            [datomic.api :as d]
            [forum.models :refer :all]
            [forum.authentication :refer [encrypt]])
  (:import (datomic Util)))

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
    @(d/transact conn txd))
  :done)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def uri "datomic:free://localhost:4334/clj-forum")

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

(defn get-conn []
  (d/connect uri))

(defn get-db []
  (d/db (get-conn)))

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
  (flatten (find-all-by (get-db) :forum/uid)))

(defn find-all-users []
  (map wrap-user (flatten (find-all-by (get-db) :user/uid))))

(defn find-all-topics []
  (flatten (find-all-by (get-db) :topic/uid)))

(defn find-all-posts []
  (flatten (find-all-by (get-db) :post/uid)))

(defn find-forum-by-uid [uid]
  (find-by (get-db) :forum/uid (Long. uid)))

(defn find-user-by-uid
  ([uid] (find-user-by-uid (get-db) uid))
  ([db uid]
     (when-let [entity (->> (d/datoms db :avet :user/uid uid)
                            (d/q '[:find ?e :where [?e]])
                            ffirst
                            (d/entity db)
                            )]
       (wrap-user entity))))

(defn find-user-by-uname [uname]
  (when-let [entity (find-by (get-db) :user/uname uname)]
    (wrap-user entity)))

(defn find-topic-by-uid [uid]
  (find-by (get-db) :topic/uid (Long. uid)))

(defn find-post-by-uid [uid]
  (find-by (get-db) :post/uid (Long. uid)))

;; Retraction ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn retract-post
  "Returns nil if nothing happened.
   Can't retract the first post in a topic (Instead use
   retract-topic."
  [uid]
  (when-let [post (find-post-by-uid uid)]
    (assert (not (first-post? post)))
    (let [result @(d/transact
                   (get-conn)
                   [[:db.fn/retractEntity (:db/id post)]])]
      result)))

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
  "Returns uid or nil.
   {:uname _              Required
    :email _              Required
    :pwd _                Required
    :role :admin or :mod  Optional"
  [{:keys [uname email pwd role] :as user}]
  ;; (let [digest (encrypt pwd)
  ;;       eid (tempid)
  ;;       result @(d/transact
  ;;                conn [[:user/construct eid uname email digest]])]
  ;;   (:user/uid (created-entity result :user/uname uname))))
  (if (find-user-by-uname uname)
    (ex-info "TODO: Handle this case.")
    (let [digest (encrypt pwd)
          eid (tempid)
          user-map (merge {:db/id eid
                           :user/uname uname
                           :user/email email
                           :user/digest digest
                           :user/created (java.util.Date.)}
                          (when role
                            {:user/role role}))]
      (let [result @(d/transact
                     (get-conn)
                     [user-map
                      [:addUID eid :user/uid]])]
        (:user/uid (created-entity result :user/uname uname))))))

(defn create-forum
  "Returns uid or nil."
  [title desc]
  (let [eid (tempid)
        result @(d/transact
                 (get-conn) [[:forum/construct eid title desc]])]
    (:forum/uid (created-entity result :forum/title title))))

(defn create-topic
  "Returns uid or nil"
  [user-uid fuid title text]
  (let [result @(d/transact (get-conn)
                            [[:topic/construct
                              (Long. user-uid)
                              (Long. fuid)
                              title
                              text]])]
    (:topic/uid (created-entity result :topic/title title))))

(defn create-post
  "Return uid or nil"
  [user-uid tuid text]
  (let [result @(d/transact
                 (get-conn)
                 [[:post/construct
                   (Long. user-uid)
                   (Long. tuid)
                   text]])]
    (:post/uid (created-entity result :post/text text))))

(defn update-post
  "Return uid or nil."
  [puid text]
  (let [result @(d/transact (get-conn) [[:post/update
                                   (Long. puid)
                                   text]])]
    ;; TODO: Change name of created-entity fn...
    (:post/uid (created-entity result :post/text text))))

(defn update-user
  "Return uid or nil.
   Only give it the things that actually change.
   {:user/email 'newemail@example.com'}
   {:user/digest 'neWdIgEsT2ashf80hq3r'}"
  [uid attrs]
  (let [base-entity {:db/id (tempid)
                     :user/uid (Long. uid)}
        ;; Merge it with the only things that can change.
        modified-entity (merge base-entity
                               (select-keys attrs
                                            [:user/uname
                                             :user/email
                                             :user/digest]))]
    (let [result @(d/transact (get-conn) [modified-entity])]
      (when result
        (Long. uid)))))

;; Seed the DB ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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

(defn generate-post-text []
  (str/join  " " (repeatedly 3 generate-sentence)))

(defn seed2 []
  (let [user-uids (let [r [;; Admin
                           (create-user {:uname "danneu" :email "me@example.com" :pwd "secret" :role :admin})
                           ;; Moderator
                           (create-user {:uname "Internet Moderator Tough Guy" :email "mod@example.com" :pwd "secret" :role :mod})
                           ;; Regular member
                           (create-user {:uname "Georgy Porgy" :email "porgy@example.com" :pwd "secret"})]]
                    (println "user-uids: " r)
                    r)
        forum-uids (let [r [(create-forum
                             "Newbie Introductions"
                             "Are you new here? Then introduce yourself!")
                            (create-forum
                             "General Discussion"
                             "Talk about whatever and just kick it.")
                            (create-forum
                             "Suggestions/Support"
                             "Need help or have an idea to improve the place? Come share.")]]
                     (println "forum-uids: " r)
                     r)
        topic-uids (let [r (flatten
                            (for [forum-uid forum-uids]
                              (repeatedly 10 #(create-topic (rand-nth user-uids)
                                                            forum-uid
                                                            (generate-sentence)
                                                            (generate-sentence)))))]
                     (println "topic-uids: " r)
                     r)
        post-uids (let [r (flatten
                           (for [topic-uid topic-uids]
                             (do
                               (println "Topic uids: " topic-uids)
                               (println "User uids: " user-uids)
                               (repeatedly 10 #(create-post
                                                (rand-nth user-uids)
                                                topic-uid
                                                (generate-post-text))))))]
                    (println "post-uids: " r)
                    r)]
    (println "Topics created:" (count topic-uids) "\n"
             "Posts created: " (count post-uids))))

(defn forum-posts-count [feid]
  (let [result (d/q '[:find (count ?p)
                      :in $ ?f
                      :where [?f :forum/topics ?t]
                      [?t :topic/posts ?p]]
                    (get-db) feid)]
    (only result)))
