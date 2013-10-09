(ns forum.middleware.wrap-current-user
  (:require [forum.db :as db]))

;; *current-user* is either:
;; {}                -- guest
;; {:user/uid _ ...} -- logged in
(def ^:dynamic *current-user* {})

(defn wrap-current-user [handler]
  (fn [request]
    (let [session (:session request)]
      (binding [*current-user* (when-let [uid (:user/uid session)]
                               (db/find-user-by-uid uid))]
        (handler request)))))
