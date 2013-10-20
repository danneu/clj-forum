(ns forum.controllers.users
  (:require [forum.controllers.base :refer :all]
            [forum.authentication :refer [encrypt]]
            [forum.recaptcha :as recaptcha]
            [forum.avatar]))

(load-base-controller)

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

(defn create [params]
  ;; TODO: Should I check if recaptcha is :enabled? manually here
  ;;       or should I let the recaptcha module do that? Perhaps
  ;;       it's misleading to have this recaptcha in the code path
  ;;       even though it's not actually getting checked.
  ;; First check recaptcha
  (println params)
  (let [recaptcha-verification
        (recaptcha/send-verify-request
         {:challenge (:recaptcha_challenge_field params)
          :user-input (:recaptcha_response_field params)})]
    (if-not (:status recaptcha-verification)
      ;; Recaptcha invalid
      (-> (redirect "/users/new")
          (assoc :flash [:danger (str "Invalid captcha: "
                                      (:error-code recaptcha-verification))]))
      ;; Recaptcha valid
      (let [user-params (:user params)]
        (if-let [errors (forum.validation/user-errors user-params)]
          (assoc (redirect "/users/new")
            :flash
            [:danger (for [error errors]
                       [:li error])])
          (let [useruid (db/create-user
                         {:uname (:uname user-params)
                          :email (:email user-params)
                          :pwd (:pwd user-params)})]
            (forum.avatar/create-and-write-avatar useruid)
            (-> (redirect "/users")
                (assoc :session {:user/uid useruid})
                (assoc :flash [:success "Successfully registered."]))))))))
