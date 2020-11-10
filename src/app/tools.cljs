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

(defn find-element [^js/Cheerio element to-find]
  (.first (.find element to-find)))

(defn find-elements [^js/Cheerio element to-find]
  (cheerio->array (.find element to-find)))

(defn element-exist [^js/Cheerio element to-find]
  (> (.-length (.find element to-find)) 0))

(defn correct-file-name [name]
  (str/replace name #"[/\\?%*:|<>\"]" "-"))

(defn show-progress [idx cnt file]
  (prn (str "Processing " idx "/" cnt " - " (:file-path file))))

(defn release-memory [idx]
  (when (= 0 (rem (dec idx) 50))
    (js/global.gc)))
