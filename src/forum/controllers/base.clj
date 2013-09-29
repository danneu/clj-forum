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
      '[ring.util.response :refer [~'redirect]]
      '[forum.middleware.wrap-current-user :refer [~'current-user]])))

;; (defmacro defcontroller [controller-name & body]
;;   (let [controller-ns (str "forum.controllers." controller-name)]
;;     `(ns ~(symbol controller-ns)
;;        (:require [clojure.string :as ~'str]
;;                  [forum.db :as ~'db]
;;                  [forum.views forums master posts topics users])
;;        ~@body)))

;; (defmacro defcontroller [controller-name & body]
;;   (let [controller-ns (str "forum.controllers." controller-name)]
;;     `(do
;;        (ns ~(symbol controller-ns))
;;        (require '[forum.db :as ~'db]))))
