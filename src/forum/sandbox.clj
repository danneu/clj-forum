(ns forum.sandbox
  (:require [datomic.api :as d])
  (:import [java.util Date]))


;; (defn seeds []
;;   (let [forum1 (d/tempid :db.part/user)
;;         topic1 (d/tempid :db.part/user)
;;         post1 (d/tempid :db.part/user)
;;         post2 (d/tempid :db.part/user)
;;         topic2 (d/tempid :db.part/user)
;;         post3 (d/tempid :db.part/user)
;;         post4 (d/tempid :db.part/user)]
;;     (let [seeds [;; Forum A
;;                  {:db/id forum1
;;                   :forum/title "Forum 1"
;;                   :forum/topics [topic1 topic2]}
;;                  {:db/id topic1
;;                   :topic/title "Topic 1"
;;                   :topic/position 0}
;;                  {:db/id topic2
;;                   :topic/title "Topic 2"
;;                   :topic/position 1}]]
;;       seeds)))

;; {:db/id #db/id[:db.part/user]
;;  :db/ident :append-position-in-scope
;;  :db/doc "Atomically adds to the end of a list of sorted cardinality/many lists"
;;  :db/fn #db/fn
;;  {:lang "clojure"
;;   :params [db scope-id scope-attr new-id pos-attr]
;;   :code (let [children (scope-attr (datomic.api/entity db scope-id))
;;               highest-pos (reduce max 0 (map pos-attr children))]
;;           [[:db/add new-id pos-attr (inc highest-pos)]])}}

;; (defn tempid []
;;   (d/tempid :db.part/user))

;; (add-forum conn )

;; (defn add-forum [conn title]
;;   (let [forumid ()])
;;   (d/transact conn ))

;; (defn create-mem-db []
;;   (let [uri "datomic:mem://forum"]
;;     (d/delete-database uri)
;;     (d/create-database uri)
;;     (let [conn (d/connect uri)]
;;       @(d/transact conn schema)
;;       @(d/transact conn (seeds))
;;       conn)))

;; (def conn (create-mem-db))

;; (d/q '[:find ?title ?pos
;;        :where [?t :topic/title ?title]
;;               [?t :topic/position ?pos]]
;;      (d/db conn))


