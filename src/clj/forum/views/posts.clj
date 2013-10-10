(ns forum.views.posts
  (:use [hiccup core element form util])
  (:require [forum.helpers :refer :all]))

;; (defn post-form
;;   "`form-action` is same [method url] vector that
;;    Hiccup's form-to expects."
;;   ([method url]
;;      (post-form method url {}))
;;   ([method url post]
;;      (list
;;       ;; Tabs
;;       [:ul.nav.nav-tabs
;;        [:li.active (link-to {:data-toggle "tab"}
;;                             "#post-markdown-pane" "Write")]
;;        [:li (link-to {:data-toggle "tab"}
;;                      "#post-preview-pane" "Preview")]]
;;       ;; Post form
;;       [:div.tab-content
;;        [:div#post-markdown-pane.tab-pane.active
;;         (form-to {:role "form"} [method url]
;;                  [:div.form-group
;;                   (text-area {:id "post-markdown"
;;                               :class "form-control"
;;                               :placeholder "Text"}
;;                              "post[text]"
;;                              (:post/text post))]
;;                  (submit-button {:class "btn btn-default"}
;;                                 "Submit"))]
;;        ;; Post preview
;;        [:div#post-preview-pane.tab-pane
;;         [:div#post-preview]]])))


(defn post-text-area [text-area-name]
  [:div.row
   [:div.col-sm-6
    "Text: "
    (text-area {:id "post-markdown"
                :class "form-control"}
               text-area-name)]
   [:div.col-sm-6
    "Preview: "
    [:div#post-preview.well
     "&larr; Start writing!"
     ]]])

(defn post-form
  "`form-action` is same [method url] vector that
   Hiccup's form-to expects."
  ([method url]
     (post-form method url {}))
  ([method url post]
     (list
      ;; Post form
      [:div.row
       [:div.col-sm-6
        "Text: "
        (form-to {:role "form"} [method url]
                 [:div.form-group
                  (text-area {:id "post-markdown"
                              :class "form-control"
                              :placeholder "Text"}
                             "post[text]"
                             (:post/text post))]
                 (submit-button {:class "btn btn-default"}
                                "Submit"))]
       [:div.col-sm-6
        "Preview: "
        [:div#post-preview.well]]])))

(defn edit [topic post]
  (html
   [:h3 "Edit Post"]
   (post-form :put (url-for post) post)))
