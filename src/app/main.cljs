(ns app.main
  (:require
   [cheerio :rename {load load-html}]
   [app.tools :as tools]
   [app.image :as image]
   [app.recipe :as recipe]
   [app.access :as access]))

(defn scan-recipe [doc content book-name]
  (let [recipe (recipe/extract-recipe doc)
        image (image/extract-recipe-image content (:name recipe))
        book {:book book-name}]
    (conj book recipe image)))

(defn scan [content book-name]
  (let [doc (load-html content)
        root (.root doc)]
    (if (recipe/is-recipe-document root)
      (scan-recipe root content book-name) nil)))

(defn process-recipe [recipe]
  (scan (access/load-recipe-file (:file recipe)) (:book-name recipe)))

(defn prn-recipe [recipe]
  (prn (dissoc recipe :image-data)))

(defn run [& args]
  (let [path (first args)
        recipes (access/get-recipes-list path)
        cnt (count recipes)]
    (doseq [[idx recipe] (take 2 recipes)]
      (tools/show-progress idx cnt)
      (prn-recipe (process-recipe recipe)))))
