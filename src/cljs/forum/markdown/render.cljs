(ns forum.markdown
  (:require [markdown.core :as md :refer [mdToHtml]]
            [domina :as dom :refer [log]]
            [markdown.transformers
             :refer [*next-line* *substring* transformer-vector]]
            [forum.markdown.transformers
             :refer [escape-transformer autolink-transformer]]
            [clojure.string :as str]
            [domina.events :refer [listen! target]]))

(defn render-post-preview!
  "Hacks:
   - Appending \n to input forces last line of input to render
     correctly.
   - Prepending with \n\n forces first line of input to be
     wrapped with <p></p>.
   - Wrapping output in <div></div> groups NodeList siblings
     into single node so that it can be inserted."
  []
  (let [rendered-post
        (as-> (dom/by-id "post-markdown") _
              (dom/value _)
              (str "\n\n" _ "\n")
              (mdToHtml _
                        :replacement-transformers
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

(listen! (dom/by-id "post-preview")
        :click
        #(.focus (dom/by-id "post-markdown")))
