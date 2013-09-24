(ns forum.controllers.forums
  (:require [forum.db]
            [forum.views.forums]
            [forum.views.master :refer :all]))

(defn index []
  (let [forums (forum.db/get-all-forums)]
    (layout (forum.views.forums/index forums))))

(defn show [fuid]
  (when-let [forum (forum.db/get-forum fuid)]
    (let [topics (:forum/topics forum)]
      (clojure.pprint/pprint topics)
      (layout
       {:crumbs []}
       (forum.views.forums/show forum
                                (sort-by :topic/uid > topics))))))
