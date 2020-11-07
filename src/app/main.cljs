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
  (let [doc (load-html content)]
    (if (recipe/is-recipe-document doc)
      (scan-recipe doc content book-name) nil)))

(defn process-recipe [recipe]
  (scan (access/load-recipe-file (:file recipe)) (:book-name recipe)))

(defn run [& args]
  (let [path (first args)
        recipes (access/get-recipes-list path)
        cnt (count recipes)]
    (doseq [[idx recipe] (take 20 recipes)]
      (tools/show-progress idx cnt)
      (prn (process-recipe recipe)))))
