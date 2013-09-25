(ns forum.validation
  (:require [clojure.string :as str]
            [forum.db :as db]))

;; Util ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn condense-string
  "Trim and condense whitespace.
   Turn nil into empty string."
  [s]
  (if (nil? s)
    ""
    (as-> s _
          (str/replace _ #"\s+" " ")
          (str/trim _))))

;; User params ;;;;;;;;;;;;;;;;;;;;;;;;;

;; I think validators will start with a
;; prep fn that massages the data.
(defn prep-user-params
  [{:keys [uname] :as user}]
  (merge user
         {:uname (condense-string uname)}))

(defn validate-user-params [user-params]
  (let [u (prep-user-params user-params)]
    ((apply
      some-fn
      [(fn [{uname :uname}]
         (when (or (empty? uname)
                   (nil? uname))
           "Username is blank"))
       (fn [{pwd :pwd}]
         (when (or (empty? pwd)
                   (nil? pwd))
           "Password is blank"))
       (fn [u]
         (when (not= (:pwd u)
                     (:confirm-pwd u))
           "Passwords don't match"))
       (fn [{uname :uname}]
         (when (> 3 (count uname))
           "Username must be at least 3 chars"))
       (fn [{uname :uname}]
         (when (< 15 (count uname))
           "Username must be shorter than 16 chars"))
       (fn [{pwd :pwd}]
         (when (> 6 (count pwd))
           "Password must be at least 6 chars"))
       (fn [{pwd :pwd}]
         (when (< 30 (count pwd))
           "Password must be no longer than 30 chars"))
       (fn [{uname :uname}]
         (when (db/get-user-by-uname
                uname)
           "Username is taken"))])
     u)))


