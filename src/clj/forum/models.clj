(ns forum.models)

;; Generally functions on entities that don't touch
;; the database. One reason being that I think requiring
;; forum.db should indicate that a namespace touched the
;; database.

;; Roles ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; 1. :guest  - {}
;; 2. :member - {:user/uid _}
;; 3. :mod    - {:user/role :mod}
;; 4. :admin  - {:user/role :admin}
(defn role
  "If user has given :user/role, then return that.
   Else, if user has a :user/uid, then return :member.
   Else, return :guest."
  [{role :user/role uid :user/uid}]
  (cond
   role role
   uid :member
   :else :guest))

(defn admin?  [user] (= :admin  (role user)))
(defn mod?    [user] (= :mod    (role user)))
(defn member? [user] (= :member (role user)))
(defn guest?  [user] (= :guest  (role user)))

;; Wrappers ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; Extend database entity maps with additional keys.

(defn wrap-user
  "Adds
   :user/role - :member or :guest"
  [user]
  (-> (into {} user)
      (assoc :user/role (role user))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn posts-asc [topic]
  (sort-by :post/uid < (:topic/posts topic)))

(defn first-post
  "Returns the OP's first post in a topic."
  [topic]
  (first (posts-asc topic)))

(defn first-post?
  "Is this the OP's first post in this topic?"
  [post]
  (let [topic (first (:topic/_posts post))]
    (= post (first-post topic))))

(defn parent-forum
  "Given topic entity, returns parent forum entity."
  [topic]
  (first (:forum/_topics topic)))

(defn parent-topic
  "Given post entity, returns parent topic entity."
  [post]
  (first (:topic/_posts post)))

(defn latest-topic
  "Given a forum entity, returns latest topic
   by createdAt although it should be by latest post bump."
  [forum]
  (first (sort-by :topic/created
                  #(- ( .compareTo %1 %2))
                  (:forum/topics forum))))

(defn latest-post
  [topic]
  (last (sort-by :post/uid (:topic/posts topic))))

(defn entity-type
  [entity]
  (cond
   (:forum/uid entity) :forum
   (:topic/uid entity) :topic
   (:post/uid entity)  :post
   (:user/uid entity)  :user
   ;; For cancan so that :forum can represent any forum.
   (= :forum entity) :forum
   (= :topic entity) :topic
   (= :post entity)  :post
   (= :user entity)  :user))

(defn logged-in? [current-user]
  (:user/uid current-user))

(def logged-out?
  (complement logged-in?))

(defn creator
  [topic-or-post]
  (case (entity-type topic-or-post)
   :topic (first (:user/_topics topic-or-post))
   :post (first (:user/_posts topic-or-post))))

;; Decoration ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; IDEA: Presenter getters like this could pull values of out
;;       an entity map and wrap it with spans/classes for styling.
(defn decorated-uname
  "(decorated-uname {:user/uname 'danneu'})
   -> <span class='decor member'>danneu</span>"
  [user]
  [:span {:class (str "decor" " " (name (role user)))}
   (:user/uname user)])
