(ns app.access
  (:require [fs :refer (readdirSync readFileSync)]
            [path :rename {resolve path-resolve}]))

(defn read-book-recipes [path book-name]
  (let [recipes (readdirSync path)]
    (for [recipe recipes]
      {:book-name book-name
       :file-path (path-resolve path recipe)})))

(defn read-books-dir [path]
  (let [books (readdirSync path)]
    (flatten (for [book books]
               (read-book-recipes (path-resolve path book) book)))))

(defn load-recipe-file [file]
  (readFileSync (:file-path file) "utf8"))

(defn get-recipes-list [path]
  (map-indexed vector (read-books-dir path)))
