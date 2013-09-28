(ns forum.controllers.users
  (:use [hiccup core element form page util])
  (:require [forum.views.master :refer :all]
            [forum.views.users]
            [forum.db :as db]
            [forum.validation :refer [validate-user-params]]
            [ring.util.response :refer [redirect]]))

(defn index []
  (let [users (db/find-all-users)]
    (layout {:crumbs ["Users"]}
            (forum.views.users/index
             (sort-by :user/uid > users)))))

(defn show [uid]
  (when-let [user (db/find-user-by-uid uid)]
    (layout {:crumbs [(link-to "/users" "Users")
                      (:user/uname user)]} (forum.views.users/show user))))

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
