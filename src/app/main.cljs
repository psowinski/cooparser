(ns app.main
  (:require [fs :refer (readdirSync readFileSync)]
            [path]
            [cheerio]))

; (defn html-collection-to-vec
;   "Converts nodelist to (not lazy) seq."
;   [hc]
;   (into [] (for [k (range (aget hc "length"))]
;              (aget hc k))))



(defn testsome [doc]
  (def ^js/Cheerio x (doc "h1"))
  ;(def z (.html x))
  ;(-> z js->clj prn))
  ;(prn (cheerio/html x))
  (prn (.eq x 1))
  (prn (.-length x))
  ;(prn (. x get 0))
  ;(prn (html-collection-to-vec x))
  ;; (doseq [it x]
  ;;   (prn it))
)

(defn scan [content]
  (let [doc (cheerio/load content)]
    ;(prn (doc "h1"))
     (isRecipe doc)
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
