(ns forum.recaptcha
  (:use [hiccup core form page])
  (:require [forum.config :as config]
            [clojure.string :as str]
            [clojure.edn :as edn]
            [clj-http.client :as client]
            [forum.middleware.expose-request :refer [req]]))

(defn public-key []
  (:public-key config/recaptcha))

(defn private-key []
  (:private-key config/recaptcha))

(defn js-url []
  (str "http://www.google.com/recaptcha/api/challenge?k="
       (public-key)))

(defn noscript-url []
  (str "http://www.google.com/recaptcha/api/noscript?k="
       (public-key)))

;; TODO: Should I checked if ReCaptcha is :enabled? where this
;;       code is being used, or should I let this module handle
;;       the check? For now I'll experiment with the latter.

(defn recaptcha-input-html
  "Prints recaptcha inputs that should be embedded in a form.
   If recatpcha is not :enabled?, it just prints nothing."
  []
  (when (:enabled? config/recaptcha)
    (html
     (include-js (js-url))
     [:noscript
      [:iframe {:src (noscript-url)
                :height 300
                :width 500
                :frameborder 0}]
      [:br]
      (text-area {:rows 3 :cols 40} "recaptcha_challenge_field")
      (hidden-field "recaptcha_response_field" "manual_challenge")])))

;; https://developers.google.com/recaptcha/docs/verify
(defn send-verify-request
  "Returns {:status Boolean
            :error-code String}
   If config/recaptcha :enabled? is not true, then it returns
   a dummy response that is always true."
  [{:keys [challenge user-input]}]
  (if-not (:enabled? config/recaptcha)
    {:status true}
    ;; Make request
    (let [response (client/post
                    "http://www.google.com/recaptcha/api/verify"
                    {:form-params {:privatekey (private-key)
                                   :remoteip (:remote-addr req)
                                   :challenge challenge
                                   :response user-input}})]
      ;; Parse response body
      ;; Example body: "false\ninvalid-request-cookie"
      (let [body (:body response)]
        (let [[status error-code] (str/split body #"\n")]
          {:status (edn/read-string status)
           :error-code error-code})))))
