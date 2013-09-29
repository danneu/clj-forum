(ns forum.validation
  (:require [clojure.string :as str]
            [forum.db :as db]
            [bouncer.core :as b]
            [bouncer.validators :as v]))

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

;; Public functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn user-errors
  "Returns nil or a collection of error strings."
  [user]
  (let [[error-map] (validate-user user)]
    (flatten (vals error-map))))
