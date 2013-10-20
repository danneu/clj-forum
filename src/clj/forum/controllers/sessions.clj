(ns forum.controllers.sessions
  (:require [forum.controllers.base :refer :all]
            [forum.authentication :refer [match?]]))

(load-base-controller)

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
