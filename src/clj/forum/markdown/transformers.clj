(ns forum.markdown.transformers
  (:require [clojure.string :as str]))


(defn autolink [text]
  (str/replace text
               #"(https?://[\S]+)"
               "<a href='$1'>$1</a>"))

(defn escape [text]
  (str/join (for [c text]
              (case c
                \< "&lt;"
                \> "&gt;"
                \& "&amp;"
                c))))

;; Transformer plugins for markdown-clj

(defn escape-transformer [text state]
  [(escape text) state])

(defn autolink-transformer [text state]
  [(autolink text) state])
