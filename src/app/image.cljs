(ns app.image
  (:require [app.tools :as tools]
            [clojure.string :as str]
            [fs :refer (writeFileSync)]
            [path :rename {resolve path-resolve}]))

(defn extract-recipe-image [content name]
  (let [from (str/index-of content "savepage_PageLoader")
        key "resourceBase64Data[8] = \""
        start (+ (count key) (str/index-of content key from))
        end (str/index-of content "\";" start)
        image (subs content start end)
        file-name (str (tools/correct-file-name name) ".jpg")]
    {:image-file-name file-name
     :image-data "image"}))

(defn save-image [path img]
  (writeFileSync (path-resolve path (:image-file-name img)) (:image-data img) "base64"))
