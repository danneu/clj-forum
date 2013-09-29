(ns forum.controllers.sessions
  (:require [forum.db :as db]
            [forum.authentication :refer [match?]]
            [ring.util.response :refer [redirect]]))

(defn create [uname pwd]
  (let [user (db/find-user-by-uname uname)]
    (if (match? pwd (:user/digest user))
      (-> (redirect "/")
          (assoc :session (select-keys user [:user/uid]))
          (assoc :flash [:success "Successfully logged in."]))
      (-> (redirect "/")
          (assoc :flash [:danger "Invalid credentials."])))))

(defn destroy []
  (-> (redirect "/")
      (assoc :session nil)
      (assoc :flash [:success "Successfully logged out."])))
