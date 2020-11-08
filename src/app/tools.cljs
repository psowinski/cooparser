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

(defn show-progress [idx cnt]
  (if (= 0 (rem (dec idx) 10)) 
    (prn (str "Processing " idx "/" cnt))
    nil))

(defn find-element [element to-find]
  (.first (.find element to-find)))

(defn find-elements [element to-find]
  (cheerio->array (.find element to-find)))

(defn doc-elements [doc to-find]
  (cheerio->array (doc to-find)))
