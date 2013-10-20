(ns forum.config
  (:require [clojure.java.io :as io]))

;; :session-secret String
;; :recaptcha
;;   :enabled? Boolean
;;   :public-key String
;;   :private-key String

(def config
  "The config map read from resources/config.edn."
  (read-string (slurp (io/resource "config.edn"))))

;; Accessors ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Store session in cookie that's encrypted
;; with AES key defined as :session-secret config.edn.
(def session-secret
  (:session-secret config))

(def recaptcha
  "{:enabled? Boolean
    :private-key String
    :public-key String}"
  (:recaptcha config))
