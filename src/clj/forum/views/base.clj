(ns forum.views.base)

;; TODO: Better way to do something like this?

;; This is the standard view stack that every view can
;; expect to have. Preferably references will be added
;; here unless they truly are ad-hoc and unique to one view.
;;
;; For controllers, there is forum.controllers.base

(defmacro load-base-view []
  `(do
     (use '[hiccup.core]
          '[hiccup.element]
          '[hiccup.form]
          '[hiccup.page]
          '[hiccup.util])
     (require
      '[clojure.string :as ~'str]
      '[forum.middleware.expose-request :refer [~'req]]
      '[forum.markdown.render :refer [~'to-html]]
      '[forum.views ~'forums ~'master ~'topics ~'users ~'posts]
      '[forum.models :refer :all]
      '[forum.helpers :refer :all]
      '[forum.cancan :refer :all]
      '[ring.util.anti-forgery :refer [~'anti-forgery-field]]
      '[forum.middleware.wrap-current-user
        :refer [~'*current-user*]])))
