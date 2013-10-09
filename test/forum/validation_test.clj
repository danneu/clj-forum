(ns forum.validation-test
  (:use [forum.validation]
        [forum.db]
        [expectations])
  (:require [clojure.string :as str]))

;; User params ;;;;;;;;;;;;;;;;;;;;;;;;;;

;(expect "a b c"
        ;(condense-string "  a  b  c   "))

;(expect "Username is blank"
        ;(validate-user-params
         ;{:uname ""}))

;(expect "Username is blank"
        ;(validate-user-params
         ;{:uname nil}))

;(expect "Password is blank"
        ;(validate-user-params
         ;{:uname "Sedendegad"}))

;(expect "Password is blank"
        ;(validate-user-params
         ;{:uname "Sedendegad"
          ;:pwd ""}))

;(expect "Passwords don't match"
        ;(validate-user-params
         ;{:uname "Sedendegad"
          ;:pwd "my secret"}))

;(expect "Passwords don't match"
        ;(validate-user-params
         ;{:uname "Sedendegad"
          ;:pwd "my secret"
          ;:confirm-pwd ""}))

;(expect "Username must be at least 3 chars"
        ;(validate-user-params
         ;{:uname "Ab"
          ;:pwd "my secret"
          ;:confirm-pwd "my secret"}))

;(expect "Username must be shorter than 16 chars"
        ;(validate-user-params
         ;{:uname (str/join (repeat 16 "a"))
          ;:pwd "my secret"
          ;:confirm-pwd "my secret"}))

;(expect "Password must be at least 6 chars"
        ;(validate-user-params
         ;{:uname "Sedendegad"
          ;:pwd "xxx"
          ;:confirm-pwd "xxx"}))

;(expect "Password must be no longer than 30 chars"
        ;(validate-user-params
         ;(let [pwd (repeat 31 "a")]
           ;{:uname "Sedendegad"
            ;:pwd pwd
            ;:confirm-pwd pwd})))

;(expect "Username is taken"
        ;(with-redefs [conn (create-test-db)]
          ;(create-user "Sedendegad"
                       ;"my secret")
          ;(validate-user-params
           ;{:uname "Sedendegad"
            ;:pwd "my secret"
            ;:confirm-pwd "my secret"})))
