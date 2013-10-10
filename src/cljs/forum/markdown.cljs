(ns forum.markdown
  (:require [markdown.core :as md :refer [mdToHtml]]
            [domina :as dom :refer [log]]
            [markdown.transformers
             :refer [*next-line* *substring* transformer-vector]]
            [clojure.string :as str]
            [domina.events :refer [listen! target]]))

;; TODO: Crossover this function
(defn escape [text]
  (str/join (for [c text]
              (case c
                \< "&lt;"
                \> "&gt;"
                \& "&amp;"
                c))))

;; TODO: Crossover this function
(defn escape-transformer [text state]
  [(escape text) state])

(defn render-post-preview! []
  (let [rendered-post
        (-> (dom/by-id "post-markdown")
            (dom/value)
            ; (mdToHtml :custom-transformers [escape-transformer])
            (mdToHtml :replacement-transformers (cons escape-transformer transformer-vector))
            )]
    (dom/set-html! (dom/by-id "post-preview")
                   (dom/html-to-dom (str "<div>"
                                         rendered-post
                                         "</div>")))))

(listen! (dom/by-id "post-markdown")
         :keyup
         render-post-preview!)
