(ns forum.controllers.users
  (:require [forum.views.master :refer :all]
            [forum.views.users]
            [forum.db :as db]
            [forum.validation :refer [validate-user-params]]
            [ring.util.response :refer [redirect]]))

(defn new [flash]
  (layout (forum.views.users/new flash)))

(defn create [user-params]
  (if-let [error (validate-user-params user-params)]
    (assoc (redirect "/users/new") :flash {:danger error})
    (do (db/create-user (:uname user-params)
                        (:pwd user-params))
        (pr-str (db/get-user-by-uname (:uname user-params))))))

