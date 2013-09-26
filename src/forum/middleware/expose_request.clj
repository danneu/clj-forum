(ns forum.middleware.expose-request
  (:require [forum.db :refer [generate-word]]
            [clojure.pprint :refer [pprint]]))

;; Exposes the request map so that controllers and views
;; can simply require the `req` var if they want to read
;; the current request.

(def ^:dynamic req nil)

(defn expose-request [handler]
  (fn [request]
    (binding [req request]
      (handler request))))
