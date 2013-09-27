(ns forum.controllers.sessions
  (:require [forum.db :as db]
            [forum.authentication :refer [match?]]
            [ring.util.response :refer [redirect]]))

(defn- fail-authentication []
  (-> (redirect "/")
      (assoc :flash [:danger "Invalid credentials."])))

;; Actions

(defn create [uname pwd]
  (if-let [user (db/get-user-by-uname uname)]
    (if (match? pwd (:user/digest user))
      (-> (redirect "/")
          (assoc :session (select-keys user [:user/uid]))
          (assoc :flash [:success "Successfully logged in."]))
      (fail-authentication))
    (fail-authentication)))
