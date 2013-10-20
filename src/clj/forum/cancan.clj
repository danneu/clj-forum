(ns forum.cancan
  (:require [forum.models :refer :all]))

;; Only things that should require cancan
;; (i.e. only things that need perission-checking):
;; - controllers
;; - views

;; Keep in sync with cancan_test.clj

;; FORUMS   CREATE READ UPDATE DESTROY
;; :admin   all    all  all    all
;; :mod     --     all  --     --
;; :member  --     all  --     --
;; :guest   --     all  --     --

;; TOPICS   CREATE READ UPDATE DESTROY
;; :admin   all    all  all    all
;; :mod     all    all  all    all
;; :member  all    all  own    own
;; :guest   --     all  --     --
;;
;; Mods can change topic titles, not user posts.

;; POSTS    CREATE READ UPDATE DESTROY
;; :admin   all    all  all    all
;; :mod     all    all  own    all
;; :member  all    all  own    all
;; :guest   --     all  --     --

;; USERS   BAN
;; :admin  mods, members
;; :mod    members
;; :member --
;; :guest  --

(defn can?
  "Returns true or false.
   (can? *current-user* :read entity)"
  [user ability entity]
  (let [entity-type (entity-type entity)]
    (case (role user)
      :admin true
      :mod (case ability
             :create (case entity-type
                       :forum false
                       :topic true
                       :post true
                       false)
             :read (case entity-type
                     :forum true
                     :topic true
                     :post true
                     false)
             :destroy (case entity-type
                        :forum false
                        :topic true
                        :post true
                        false)
             :update (case entity-type
                       :forum false
                       :topic true
                       :post (= user (creator entity))
                       :user false
                       false)
             :ban (not (or (admin? entity) (mod? entity))))
      :member (case ability
                :create (case entity-type
                          :forum false
                          :topic true
                          :post true
                          false)
                :read (case entity-type
                        :forum true
                        :topic true
                        :post true
                        false)
                :update (case entity-type
                          :forum false
                          :topic (= user (creator entity))
                          :post false
                          false)
                :destroy (case entity-type
                           :forum false
                           :topic false
                           :post false
                           false)
                :ban false)
      :guest (case ability
               :read true
               :destroy false
               :create false
               false))))

(def cannot?
  (complement can?))
