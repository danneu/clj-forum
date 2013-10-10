(ns forum.markdown.render
  (:require [markdown.core :as md]
            [forum.markdown.transformers :refer [autolink-transformer
                                                 escape-transformer]]
            [markdown.transformers :refer [transformer-vector]]))

(defn to-html [markdown]
  (md/md-to-html-string markdown
                        :replacement-transformers
                        (concat [escape-transformer
                                 autolink-transformer]
                                transformer-vector)))
