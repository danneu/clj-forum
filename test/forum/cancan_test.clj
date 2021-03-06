(ns forum.cancan-test
  (:require [forum.cancan :refer :all]
            [forum.helpers :refer :all]
            [forum.models :refer :all]
            [expectations :refer :all]))

;; Keep in sync with cancan.clj

;; Test stubs
(def guest         {})
(def forum1        {:forum/uid 1})
(def admin         {:user/uid 1, :user/role :admin})
(def mod1          {:user/role :mod, :user/uid 1})
(def mod1-topic    {:topic/uid 1, :user/_posts [mod1]})
(def mod1-post     {:post/uid 1, :user/_posts [mod1]})
(def member1       {:user/uid 2})
(def member1-topic {:topic/uid 1, :user/_posts [member1]})
(def member1-post  {:post/uid 1, :user/_posts [member1]})
;; Represent entities that don't belong to test subjects
(def some-topic    {:topic/uid 42})
(def some-post     {:post/uid 42})

;; Roles
(expect :guest  (role {}))
(expect :guest  (role guest))
(expect :mod    (role {:user/role :mod}))
(expect :mod    (role mod1))
(expect :member (role {:user/uid 1}))
(expect :member (role member1))
(expect :admin  (role {:user/role :admin}))
(expect :admin  (role admin))

;; GUEST    CREATE READ UPDATE DESTROY
;; Forums   --     all  --     --
;; Topics   --     all  --     --
;; Posts    --     all  --     --

;; Forums
(expect (cannot? guest :create  :forum))
(expect (can?    guest :read    forum1))
(expect (cannot? guest :update  forum1))
(expect (cannot? guest :destroy forum1))
;; Topics
(expect (cannot? guest :create  :topic))
(expect (can?    guest :read    some-topic))
(expect (cannot? guest :update  some-topic))
(expect (cannot? guest :destroy some-topic))
;; Posts
(expect (cannot? guest :create  :post))
(expect (can?    guest :read    some-post))
(expect (cannot? guest :update  some-post))
(expect (cannot? guest :destroy some-post))

;; MOD      CREATE READ UPDATE DESTROY
;; Forums    --     all  --     --
;; Topics    all    all  all    all
;; Posts     all    all  own    all

;; Forums
(expect (cannot? mod1 :create  :forum))
(expect (can?    mod1 :read    forum1))
(expect (cannot? mod1 :update  forum1))
(expect (cannot? mod1 :destroy forum1))
;; Topics
(expect (can?    mod1 :create  :topic))
(expect (can?    mod1 :read    some-topic))
(expect (can?    mod1 :update  some-topic))
(expect (can?    mod1 :destroy some-topic))
;; Posts
(expect (can?    mod1 :create  :post))
(expect (can?    mod1 :read    some-post))
(expect (cannot? mod1 :update  some-post))
(expect (can?    mod1 :update  mod1-post))
(expect (can?    mod1 :destroy some-post))

;; MEMBER    CREATE READ UPDATE DESTROY
;; Forums    --     all  --     --
;; Topics    all    all  own    own
;; Posts     all    all  own    all

;; Forums
(expect (cannot? member1 :create  :forum))
(expect (can?    member1 :read    forum1))
(expect (cannot? member1 :update  forum1))
(expect (cannot? member1 :destroy forum1))
;; Topics
(expect (can?    member1 :create  :topic))
(expect (can?    member1 :read    some-topic))
(expect (cannot? member1 :update  some-topic))
(expect (cannot? member1 :update  member1-topic))
(expect (cannot? member1 :destroy some-topic))
(expect (cannot? member1 :destroy member1-topic))
;; Posts
(expect (can?    member1 :create  :post))
(expect (can?    member1 :read    some-post))
(expect (cannot? member1 :update  some-post))
(expect (cannot? member1 :update  member1-post))
(expect (cannot? member1 :destroy some-post))
(expect (cannot? member1 :destroy member1-post))

;; USERS   BAN
;; :admin  mods, members
;; :mod    members
;; :member --
;; :guest  --

(expect (can?    admin   :ban member1))
(expect (can?    admin   :ban mod1))
(expect (cannot? mod1    :ban admin))
(expect (can?    mod1    :ban member1))
(expect (cannot? member1 :ban mod1))
(expect (cannot? guest   :ban member1))
