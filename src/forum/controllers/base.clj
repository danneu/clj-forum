(ns forum.controllers.base)

(defmacro load-base []
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
      '[forum.helpers :refer :all]
      '[forum.validation]
      '[forum.cancan :refer :all]
      '[ring.util.response :refer [~'redirect]]
      '[forum.middleware.wrap-current-user
        :refer [~'*current-user*]])))

(load-base)

(defn redirect-unauthorized []
  (-> (redirect "/")
      (assoc :flash [:danger "You aren't authorized to do that."])))
