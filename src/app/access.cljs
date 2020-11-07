(ns app.access
  (:require [fs :refer (readdirSync readFileSync)]
            [path :rename {resolve path-resolve}]))

(defn read-recipes-book [path book-name]
  (let [recipes (readdirSync path)]
    (for [recipe recipes]
      {:book-name book-name
       :file (path-resolve path recipe)})))

(defn read-books [path]
  (let [books (readdirSync path)]
    (flatten (for [book books]
               (read-recipes-book (path-resolve path book) book)))))

(defn load-recipe-file [file]
  (readFileSync file "utf8"))

(defn get-recipes-list [path]
  (map-indexed vector (read-books path)))
