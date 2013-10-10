(ns forum.markdown
  (:require [markdown.core :as md]))

(defn to-html [markdown]
  (md/md-to-html-string markdown))
