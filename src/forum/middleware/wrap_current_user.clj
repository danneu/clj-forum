(ns forum.middleware.wrap-current-user
  (:require [forum.db :as db]))

;; Either nil or the user DB entity.
(def ^:dynamic current-user nil)

(defn wrap-current-user [handler]
  (fn [request]
    (let [session (:session request)]
      (binding [current-user (when-let [uid (:user/uid session)]
                               (db/find-user-by-uid uid))]
        (handler request)))))
