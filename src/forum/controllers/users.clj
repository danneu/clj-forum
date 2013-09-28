(ns forum.controllers.users
  (:require [forum.controllers.base :refer [load-base]]))
(load-base)

(defn index []
  (let [users (db/find-all-users)]
    (forum.views.master/layout
     {:crumbs ["Users"]}
     (forum.views.users/index
      (sort-by :user/uid > users)))))

(defn show [uid]
  (when-let [user (db/find-user-by-uid uid)]
    (forum.views.master/layout
     {:crumbs [(link-to "/users" "Users")
               (:user/uname user)]} (forum.views.users/show user))))

(defn new []
  (forum.views.master/layout (forum.views.users/new)))

(defn create [user-params]
  (if-let [error (forum.validation/validate-user-params user-params)]
    (assoc (redirect "/users/new") :flash [:danger error])
    (let [useruid (db/create-user (:uname user-params)
                                  (:pwd user-params))]
      (-> (redirect "/users")
          (assoc :session {:user/uid useruid})
          (assoc :flash [:success "Successfully registered."])))))
