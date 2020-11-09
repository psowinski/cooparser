(ns app.image
  (:require [app.tools :as tools]
            [clojure.string :as str]
            [fs :refer (writeFileSync existsSync mkdirSync)]
            [path :rename {resolve path-resolve}]))

(defn extract-recipe-image-data [content]
  (let [from (str/index-of content "savepage_PageLoader")
        key "resourceBase64Data[8] = \""
        start (+ (count key) (str/index-of content key from))
        end (str/index-of content "\";" start)
        image (subs content start end)]
    image))

(defn extract-recipe-image [content name]
  {:image-file-name (str (tools/correct-file-name name) ".jpg")
   :image-data (extract-recipe-image-data content)})

(defn remove-recipe-image-data [recipe]
  (dissoc recipe :image-data))

(defn save-recipe-image [out-path recipe]
  (let [file-name (:image-file-name recipe)
        dir-path (path-resolve out-path "img")
        file-path (path-resolve dir-path file-name)
        data (:image-data recipe)]
    (when-not (existsSync dir-path) (mkdirSync dir-path (clj->js {:recursive true})))
    (writeFileSync file-path data "base64")))