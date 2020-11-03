(ns app.main
  (:require [fs :refer (readdirSync readFileSync)]
            [path]
            [cheerio]))

(defn scan [content]
  (let [doc (cheerio/load content)]
    (prn (doc "h3"))
    ))

(defn readRecipes [path book]
  (let [recipes (readdirSync path)]
    (for [recipe recipes]
      {:book book
       :file (path/resolve path recipe)})))

(defn readBooks [path]
  (let [books (readdirSync path)]
    (flatten (for [book books]
               (readRecipes (path/resolve path book) book)))))

(defn procRecipe [recipe]
  (let [content (readFileSync (:file recipe))]
    (scan content)))

(defn run [& args]
  (let [path (first args)]
    (doseq [x (take 1 (readBooks path))]
      (procRecipe x)))
  ;js/__dirname
  )
