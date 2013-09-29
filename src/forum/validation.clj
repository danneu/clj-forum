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

;; Post validation ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(v/defvalidator valid-text-length
  {:default-message-format "Post must be 10 to 10,000 characters"}
  [text]
  (<= 3 (count text) 15))

(defn validate-post
  "Expects: {:text _}"
  [post]
  (b/validate post :text valid-text-length))

;; Public functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn post-errors
  "Returns nil or a collection of error strings."
  [post]
  (let [[error-map] (validate-post post)]
    (flatten (vals error-map))))

(defn user-errors
  "Returns nil or a collection of error strings."
  [user]
  (let [[error-map] (validate-user user)]
    (flatten (vals error-map))))
