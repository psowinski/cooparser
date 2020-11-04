(ns app.main
  (:require [fs :refer (readdirSync readFileSync)]
            [path]
            [cheerio]
            [clojure.string :as str]))


(defn cheerio->array [^js/Cheerio coll]
  (into [] (for [idx (range (.-length coll))]
             (.eq coll idx))))

(defn get-text [x]
  (-> x cheerio/text str/trim))

(defn isRecipe [doc]
  (let [titleNode (doc "h1[class='qv-recipe-head']")
        cnt (.-length titleNode)]
    (> cnt 0)))

(defn get-icons [doc]
  (let [^js/Cheerio icons (doc "div[class='qv-info-icons'] > div > p")]
    {:preperation (get-text (.eq icons 0))
     :total (get-text (.eq icons 1))
     :portion (get-text (.eq icons 2))
     :dificulty (get-text (.eq icons 3))
     :favourite (get-text (.eq icons 4))}))

(defn get-title [doc]
  (-> (doc "h1[class='qv-recipe-head'] > span") .first get-text))

(defn recognize-icons [doc span]
  (prn (.find span "strong"))
  span)

(defn get-ingridient-rows [doc item]
  (->> (doc "span" item)
       cheerio->array
       (map get-text)))

(defn get-preperation-rows [doc item]
  (->> (doc "span" item)
       cheerio->array
       (map (partial recognize-icons doc))
       (map get-text)))

(defn get-ingridients-list [doc items]
  (map (partial get-ingridient-rows doc) items))

(defn get-preperation-list [doc items]
  (map (partial get-preperation-rows doc) items))

(defn get-group-name [doc group]
  (-> (doc "h4" group) get-text))

(defn get-ingridients-group [doc group]
  (let [ingridients (cheerio->array (doc "ul > li" group))]
    {:name (get-group-name doc group)
     :list [] ;(get-ingridients-list doc ingridients)
     }))

(defn get-preperation-group [doc group]
  (let [steps (cheerio->array (doc "ol > li" group))]
    {:name (get-group-name doc group)
     :list [] ;(get-preperation-list doc steps)
     }
    ))

(defn get-ingridients [doc]
  (let [groups (cheerio->array (doc "#qv-ingredient-section > div > div > div"))]
    (map (partial get-ingridients-group doc) groups)))

(defn get-preperation [doc]
  (let [groups (cheerio->array (doc "#qv-preparation-section > div > div[class='recipe-step-groups']"))]
    (map (partial get-preperation-group doc) groups)))

(defn scanRecipe [doc]
  (-> {:name (get-title doc)
       :summary (get-icons doc)
       :ingridients (get-ingridients doc)
       :preperation (get-preperation doc)} prn))

(defn scan [content]
  (let [doc (cheerio/load content)]
    (if (isRecipe doc)
      (scanRecipe doc) ())))

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
    (doseq [x (take 5 (readBooks path))]
      (procRecipe x)))
  ;js/__dirname
  )
