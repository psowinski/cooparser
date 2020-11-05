(ns app.main
  (:require [fs :refer (readdirSync readFileSync writeFileSync)]
            [path :rename {resolve path-resolve}]
            [cheerio]
            [clojure.string :as str]
            [app.tools :as tools]
            [app.symbols :as symbols]
            [app.image :as image]))

(defn is-recipe [doc]
  (let [titleNode (doc "h1[class='qv-recipe-head']")
        cnt (.-length titleNode)]
    (> cnt 0)))

(defn get-icons [doc]
  (let [^js/Cheerio icons (->> (doc "div[class='qv-info-icons'] > div > p")
                               tools/cheerio->array
                               (take 5)
                               (map tools/get-text))]
    {:preperation (nth icons 0)
     :total (nth icons 1)
     :portion (nth icons 2)
     :dificulty (nth icons 3)
     :favourite (nth icons 4)}))

(defn get-nutritional [doc]
  (let [^js/Cheerio section (doc "#qv-nutritional-section > div > div")
        ^js/Cheerio per-unit (-> (.find section "p") .first tools/get-text)
        ^js/Cheerio vlaues (->> (.find section "div[class='nutritional-values'] > div > span")
                                tools/cheerio->array
                                (take 4)
                                (map tools/get-text))]
    {:per per-unit
     :energy (nth vlaues 0)
     :protein (nth vlaues 1)
     :carb (nth vlaues 2)
     :fat (nth vlaues 3)}))

(defn get-name [doc]
  (-> (doc "h1[class='qv-recipe-head'] > span") .first tools/get-text))

(defn get-recipe-row [row]
  (->> (.find row "span")
       tools/cheerio->array
       (map symbols/update-symbols)
       (map tools/get-text)))

(defn get-group-name [group]
  (-> (.find group "h4") tools/get-text))

(defn get-recipe-group [group row-name]
  (let [rows (tools/cheerio->array (.find group row-name))]
    {:name (get-group-name group)
     :list (map get-recipe-row rows)}))

(defn get-ingridients-group [group]
  (get-recipe-group group "ul > li"))

(defn get-preperation-group [group]
  (get-recipe-group group "ol > li"))

(defn get-ingridients [doc]
  (let [groups (tools/cheerio->array (doc "#qv-ingredient-section > div > div > div"))]
    (map get-ingridients-group groups)))

(defn get-preperation [doc]
  (let [groups (tools/cheerio->array (doc "#qv-preparation-section > div > div[class='recipe-step-groups']"))]
    (map get-preperation-group groups)))

(defn extract-recipe [doc]
  (let [name (get-name doc)]
    {:name name
     :summary (get-icons doc)
     :ingridients (get-ingridients doc)
     :preperation (get-preperation doc)
     :nutritional (get-nutritional doc)}))

(defn scan-recipe [doc content]
  (let [recipe (extract-recipe doc)
        image (image/extract-recipe-image content (:name recipe))]
    (conj recipe image)))

(defn scan [content]
  (let [doc (cheerio/load content)]
    (if (is-recipe doc)
      (scan-recipe doc content) nil)))

(defn read-recipes [path book]
  (let [recipes (readdirSync path)]
    (for [recipe recipes]
      {:book book
       :file (path-resolve path recipe)})))

(defn process-recipe [recipe]
  (scan (readFileSync (:file recipe) "utf8")))

(defn read-books [path]
  (let [books (readdirSync path)]
    (flatten (for [book books]
               (read-recipes (path-resolve path book) book)))))

(defn run [& args]
  (let [path (first args)
        recipes (map-indexed vector (read-books path))
        cnt (count recipes)]
    (doseq [[idx recipe] (take 20 recipes)]
      (tools/show-progress idx cnt)
      (prn (process-recipe recipe)))))
