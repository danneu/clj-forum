(ns forum.markdown
  (:require [markdown.core :as md :refer [mdToHtml]]
            [domina :as dom :refer [log]]
            [markdown.transformers
             :refer [*next-line* *substring* transformer-vector]]
            [forum.markdown.transformers
             :refer [escape-transformer autolink-transformer]]
            [clojure.string :as str]
            [domina.events :refer [listen! target]]))

(defn render-post-preview! []
  (let [rendered-post
        (-> (dom/by-id "post-markdown")
            (dom/value)
            ; (mdToHtml :custom-transformers [escape-transformer])
            (mdToHtml :replacement-transformers
                      (concat [escape-transformer
                               autolink-transformer]
                              transformer-vector)))]
    (dom/set-html! (dom/by-id "post-preview")
                   (dom/html-to-dom (str "<div>"
                                         rendered-post
                                         "</div>")))))

(listen! (dom/by-id "post-markdown")
         :keyup
         render-post-preview!)
