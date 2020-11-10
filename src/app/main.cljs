(ns app.main
  (:require
   [cheerio :rename {load load-html}]
   [app.tools :as tools]
   [app.image :as image]
   [app.recipe :as recipe]
   [app.access :as access]
   [path :rename {resolve path-resolve}]))

(defn extract-recipe [id book content]
  (let [root (.root (load-html content))]
    (when (recipe/is-recipe-document root)
      (let [recipe (recipe/extract-recipe root)]
        (conj {:id id :book book} recipe)))))

(defn export-image [content name out-path]
  (let [image (image/extract-recipe-image content name)]
    (image/save-recipe-image out-path image)
    (:image-file-name image)))

(defn export-recipe [id file out-path]
  (let [content (access/load-recipe-file file)
        recipe (extract-recipe id (:book-name file) content)]
    (when (some? recipe)
      (let [name (str "[" id "] " (:name recipe))
            image-name (export-image content name out-path)]
        (clj->js (conj recipe {:image image-name}))))))

(defn process-file [[idx file] out-path cnt]
  (tools/show-progress idx cnt file)
  (tools/release-memory idx)
  (export-recipe (inc idx) file out-path))

(defn process-files [files out-path]
  (let [cnt (count files)]
    (map #(process-file % out-path cnt) files)))

(defn run [in-path out-path &args]
  (let [files (access/get-recipes-list in-path)
        processed (into () (process-files files out-path))
        result (reverse (filter some? processed))]
    (access/save-result out-path result)))
