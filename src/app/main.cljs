(ns app.main
  (:require
   [cheerio :rename {load load-html}]
   [app.tools :as tools]
   [app.image :as image]
   [app.recipe :as recipe]
   [app.access :as access]
   [path :rename {resolve path-resolve}]))

(defn extract-recipe [doc content book-name]
  (let [recipe (recipe/extract-recipe doc)
        image (image/extract-recipe-image content (:name recipe))
        book {:book book-name}]
    (conj book recipe image)))

(defn export-recipe [file img-path]
  (let [content (access/load-recipe-file file)
        doc (load-html content)
        root (.root doc)]
    (when (recipe/is-recipe-document root)
      (let [recipe (extract-recipe root content (:book-name file))]
        (image/save-recipe-image img-path recipe)
        (image/remove-recipe-image-data recipe)))))

(defn process-files [files out-path]
  (let [cnt (count files)]
    (for [[idx file] files]
      (do (tools/show-progress idx cnt)
          (export-recipe file out-path)))))

(defn run [& args]
  (let [in-path (first args)
        out-path (second args)
        files (access/get-recipes-list in-path)]
    (prn (into () (process-files (take 5 files) out-path)))))
