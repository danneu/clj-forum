(ns forum.controllers.users
  (:require [forum.views.master :refer :all]
            [forum.views.users]
            [forum.db :as db]
            [forum.validation :refer [validate-user-params]]
            [ring.util.response :refer [redirect]]))

(defn index []
  (let [users (db/find-all-users)]
    (layout (forum.views.users/index
             (sort-by :user/uid > users)))))

(defn new []
  (layout (forum.views.users/new)))

(defn create [user-params]
  (if-let [error (validate-user-params user-params)]
    (assoc (redirect "/users/new") :flash [:danger error])
    (let [useruid (db/create-user (:uname user-params)
                                  (:pwd user-params))]
      (-> (redirect "/users")
          (assoc :session {:user/uid useruid})
          (assoc :flash [:success "Successfully registered."])))))
