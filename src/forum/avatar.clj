(ns forum.avatar
  (:require [mikera.image.core :refer :all]
            [mikera.image.colours :refer :all]
            [ring.util.response :refer [file-response]])
  (:import [javax.imageio ImageIO]
           [java.io File]))


(defn random-image [x y]
  (let [img (new-image x y)
        color-pixels (int-array (* x y) (rand-colour))]
    (set-pixels img color-pixels)
    img))

(defn uid-to-avatar-path [uid]
  (str "resources/public/avatars/" uid ".png"))

(defn create-and-write-avatar
  "Creates avatar and saves it to
   resources/public/avatars/<uid>.png"
  [uid]
  (ImageIO/write (random-image 80 80)
                 "png"
                 (File. (uid-to-avatar-path uid))))

(def default-avatar-path
  "resources/public/avatars/default.png")

(defn avatar-path [uid]
  (if (.exists (File. (uid-to-avatar-path uid "png")))
    (uid-to-avatar-path uid)
    default-avatar-path))

(defn avatar-response [uid]
  (or (file-response (uid-to-avatar-path uid))
      (file-response default-avatar-path)))
