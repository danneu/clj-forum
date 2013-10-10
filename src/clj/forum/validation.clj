(ns forum.validation
  (:require [clojure.string :as str]
            [forum.db :as db]
            [bouncer.core :as b]
            [bouncer.validators :as v]))

;; I guess stuff like missing user-uid and tuid should
;; be exceptions thrown by transactor?

;; User validation ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(v/defvalidator valid-uname-length
  {:default-message-format "Username must be 3 to 15 characters"}
  [uname]
  (<= 3 (count uname) 15))

(v/defvalidator valid-pwd-length
  {:default-message-format "Password must be 6 to 50 characters"}
  [pwd]
  (<= 6 (count pwd) 50))

(v/defvalidator pwd-confirmed
  {:default-message-format "Password confirmation must match password."}
  [confirm-pwd pwd]
  (= confirm-pwd pwd))

(v/defvalidator uname-unique
  {:default-message-format "Username is taken"}
  [uname]
  (nil? (db/find-user-by-uname uname)))

(defn validate-user
  "Returns bouncer validation vector.
   Errors: [{:uname ('Username required') :pwd ('Password required')} ...]
   No errors: [nil ...]"
  [user]
  (let [pwd (:pwd user)]
    (b/validate
     user
     :uname [[v/required :message "Username required"]
             valid-uname-length]
     :pwd [[v/required :message "Password required"]
           valid-pwd-length]
     :confirm-pwd [[pwd-confirmed pwd]])))

;; TODO figure out better way
(defn validate-user-pwd
  [user]
  (let [pwd (:pwd user)]
    (b/validate
     user
     :pwd [[v/required :message "Password required"]
           valid-pwd-length]
     :confirm-pwd [[pwd-confirmed pwd]])))

;; Post validation ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(v/defvalidator valid-text-length
  {:default-message-format "Text must be 10 to 10,000 characters"}
  [text]
  (<= 10 (count text) 10000))

(defn validate-post
  "Expects: {:text _}"
  [post]
  (b/validate post :text valid-text-length))

;; Topic validation

(v/defvalidator valid-title-length
  {:default-message-format "Title must be 3 to 60 characters"}
  [title]
  (<= 3 (count title) 60))

(defn validate-topic
  "Expects: {:title _
             :text _}"
  [topic]
  (b/validate topic
              :text valid-text-length
              :title valid-title-length))

;; Public functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn post-errors
  "Returns nil or a collection of error strings."
  [post-params]
  (let [[error-map] (validate-post post-params)]
    (not-empty (flatten (vals error-map)))))

(defn user-errors
  "Returns nil or a collection of error strings."
  [user-params]
  (let [[error-map] (validate-user user-params)]
    (not-empty (flatten (vals error-map)))))

;; TODO Figure out better way
(defn user-pwd-errors
  "Returns nil or a collection of error strings."
  [user-params]
  (let [[error-map] (validate-user-pwd user-params)]
    (not-empty (flatten (vals error-map)))))

(defn topic-errors
  "Returns nil or a collection of error strings."
  [topic-params]
  (let [[error-map] (validate-topic topic-params)]
    (not-empty (flatten (vals error-map)))))
