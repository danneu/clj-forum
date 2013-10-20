(ns forum.controllers.base)

;; TODO: Better way to do something like this?

;; This is the standard view stack that every controller can
;; expect to have. Preferably references will be added
;; here unless they truly are ad-hoc and unique to one controller.
;;
;; For views, there is forum.views.base

(defmacro load-base-controller []
  `(do
     (use '[hiccup.core]
          '[hiccup.element]
          '[hiccup.form]
          '[hiccup.page]
          '[hiccup.util])
     (require
      '[clojure.string :as ~'str]
      '[forum.db :as ~'db]
      '[forum.views ~'forums ~'master ~'posts ~'topics ~'users]
      '[forum.models :refer :all]
      '[forum.helpers :refer :all]
      '[forum.validation]
      '[forum.cancan :refer :all]
      '[ring.util.response :refer [~'redirect]]
      '[forum.middleware.wrap-current-user
        :refer [~'*current-user*]])))

(load-base-controller)

(defn redirect-unauthorized []
  (-> (redirect "/")
      (assoc :flash [:danger "You aren't authorized to do that."])))
