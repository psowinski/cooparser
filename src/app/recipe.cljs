(ns app.recipe
  (:require
   [cheerio]
   [app.tools :as t]
   [app.symbols :as symbols]))

(defn is-recipe-document [root]
  (t/element-exist root "h1[class='qv-recipe-head']"))

(defn get-icons [root]
  (let [icons (->> (t/find-elements
                    root "div[class='qv-info-icons'] > div > p")
                   (map t/get-text))]
    {:preperation (nth icons 0)
     :total (nth icons 1)
     :portion (nth icons 2)
     :dificulty (nth icons 3)
     :favourite (nth icons 4)}))

(defn get-nutritional [root]
  (let [section (t/find-element root "#qv-nutritional-section > div > div")
        per-unit (t/get-text (t/find-element section "p"))
        values (->> (t/find-elements section "div[class='nutritional-values'] > div > span")
                    (map t/get-text))]
    (if (>= (count values) 4)
      {:per per-unit
       :energy (nth values 0)
       :protein (nth values 1)
       :carb (nth values 2)
       :fat (nth values 3)}
      nil)))

(defn get-name [root]
  (t/get-text (t/find-element root "h1[class='qv-recipe-head'] > span")))

(defn get-recipe-row [row]
  (->> (t/find-elements row "span")
       (map symbols/update-symbols)
       (map t/get-text)))

(defn get-group-name [group]
  (t/get-text (t/find-element group "h4")))

(defn get-recipe-group [group row-name]
  (let [rows (t/find-elements group row-name)]
    {:name (get-group-name group)
     :list (map get-recipe-row rows)}))

(defn get-ingridients-group [group]
  (get-recipe-group group "ul > li"))

(defn get-preperation-group [group]
  (get-recipe-group group "ol > li"))

(defn get-ingridients [root]
  (let [groups (t/find-elements
                root "#qv-ingredient-section > div > div > div")]
    (map get-ingridients-group groups)))

(defn get-preperation [root]
  (let [groups (t/find-elements
                root
                "#qv-preparation-section > div > div[class='recipe-step-groups']")]
    (map get-preperation-group groups)))

(defn extract-recipe [root]
  (let [name (get-name root)]
    {:name name
     :summary (get-icons root)
     :ingridients (get-ingridients root)
     :preperation (get-preperation root)
     :nutritional (get-nutritional root)}))
