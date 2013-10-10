(ns forum.markdown
  (:require [markdown.core :as md]
            [domina :as dom]
            [domina.events :refer [listen! target]]))

(listen! (dom/by-id "post-markdown")
         :keyup
         #(let [rendered-post
                (-> (dom/by-id "post-markdown")
                    (dom/value)
                    (md/mdToHtml
                     :custom-transformers
                     [auto-hyperlink]))]
            (dom/set-html! (dom/by-id "post-preview")
                       (dom/html-to-dom (str "<div>"
                                         rendered-post
                                         "</div>")))))
