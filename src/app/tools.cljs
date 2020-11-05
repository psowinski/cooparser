(ns app.tools
  (:require [cheerio]
            [clojure.string :as str]))

(defn cheerio->array [^js/Cheerio coll]
  (into [] (for [idx (range (.-length coll))]
             (.eq coll idx))))

(defn get-text [^js/Cheerio node]
  (-> node cheerio/text str/trim))

(defn get-attr [^js/Cheerio node name]
  (-> (.attr node name) str/trim))

(defn correct-file-name [name]
  (str/replace name #"[/\\?%*:|<>]" "-"))