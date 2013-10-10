(ns forum.markdown.render
  (:require [markdown.core :as md]
            [forum.markdown.transformers
             :refer [escape-transformer autolink-transformer]]
            [markdown.transformers :refer [transformer-vector]]
            [clojure.string :as str]))

(defn to-html [markdown]
  (md/md-to-html-string markdown
                        :replacement-transformers
                        (concat [escape-transformer
                                 autolink-transformer]
                                transformer-vector)))
