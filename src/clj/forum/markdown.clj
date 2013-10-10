(ns forum.markdown
  (:require [markdown.core :as md]
            [markdown.transformers :refer [transformer-vector]]
            [clojure.string :as str]))

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

(defn to-html [markdown]
  (md/md-to-html-string markdown
                        :replacement-transformers
                        (cons escape-transformer transformer-vector)))
