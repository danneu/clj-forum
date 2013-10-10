(ns forum.controllers.users
  (:require [forum.controllers.base :refer [load-base]]
            [forum.authentication :refer [encrypt]]
            [forum.avatar]))
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
               (:user/uname user)]}
     (forum.views.users/show user))))

(defn new []
  (forum.views.master/layout (forum.views.users/new)))

(defn edit [uid]
  (when-let [user (db/find-user-by-uid uid)]
    (forum.views.master/layout
     {:crumbs [(link-to "/users" "Users")
               (link-to (url-for url) (:user/uname user))
               "Edit Settings"]}
     (forum.views.users/edit user))))

;; TODO Don't update values unless they're changed.
(defn update [uid user-params]
  ;; TODO authorize lockdown

  (when-let [user (db/find-user-by-uid uid)]
    ;; TODO Suck less, maybe validation should return a cleaned
    ;;      map: {:user _, :errors _} that trims things so I
    ;;      can diff params against user to prevent, for example,
    ;;      " a@b.com " replacing "a@b.com".
    (when-let [new-attrs (cond
                          ;; If :email exists,
                          ;; then just update {:user/email _}
                          (:email user-params)
                          {:user/email (:email user-params)}

                          (and (:pwd user-params)
                               (not
                                (forum.validation/user-pwd-errors
                                 user-params)))
                          {:user/digest (encrypt
                                         (:pwd user-params))})]
      (when (db/update-user uid new-attrs)
        (redirect (url-for user))))))

(defn create [user-params]
  (if-let [errors (forum.validation/user-errors user-params)]
    (assoc (redirect "/users/new")
           :flash
           [:danger (for [error errors]
                      [:li error])])
    (let [useruid (db/create-user (:uname user-params)
                                  (:email user-params)
                                  (:pwd user-params))]
      (forum.avatar/create-and-write-avatar useruid)
      (-> (redirect "/users")
          (assoc :session {:user/uid useruid})
          (assoc :flash [:success "Successfully registered."])))))
