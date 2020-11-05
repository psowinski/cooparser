(ns app.main
  (:require [fs :refer (readdirSync readFileSync writeFileSync)]
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
  (let [^js/Cheerio icons (->> (doc "div[class='qv-info-icons'] > div > p")
                               cheerio->array
                               (take 5)
                               (map get-text))]
    {:preperation (nth icons 0)
     :total (nth icons 1)
     :portion (nth icons 2)
     :dificulty (nth icons 3)
     :favourite (nth icons 4)}))

(defn get-nutritional [doc]
  (let [^js/Cheerio section (doc "#qv-nutritional-section > div > div")
        ^js/Cheerio perunit (-> (.find section "p") .first get-text)
        ^js/Cheerio vlaues (->> (.find section "div[class='nutritional-values'] > div > span")
                                cheerio->array
                                (take 4)
                                (map get-text))]
    {:per perunit
     :energy (nth vlaues 0)
     :protein (nth vlaues 1)
     :carb (nth vlaues 2)
     :fat (nth vlaues 3)}))

(defn get-title [doc]
  (-> (doc "h1[class='qv-recipe-head'] > span") .first get-text))

(defn recognize-symbol-name [name]
  (case name
    "icon-dough-mode" "wyrabianie ciasta"
    "icon-reverse" "obroty wsteczne"
    "icon-stirring" "mieszanie"
    (throw (str "NIEZNANY SYMBOL: " name))))

(defn fill-symbol-text [symbol name]
  (.text symbol (str "(" (recognize-symbol-name name) ")"))
  symbol)

(defn convert-symbol [symbol]
  (let [class (.attr ^js/Cheerio symbol "class")
        name (subs class 5)]
    (fill-symbol-text symbol name)))

(defn recognize-symbols [span]
  (let [icons (cheerio->array (.find span ".icon"))]
    (doseq [icon icons]
      (convert-symbol icon))
    span))

(defn get-ingridient-row [row]
  (->> (.find row "span")
       cheerio->array
       (map get-text)))

(defn get-preperation-row [row]
  (->> (.find row "span")
       cheerio->array
       (map recognize-symbols)
       (map get-text)))

(defn get-ingridients-list [rows]
  (map get-ingridient-row rows))

(defn get-preperation-list [rows]
  (map get-preperation-row rows))

(defn get-group-name [group]
  (-> (.find group "h4") get-text))

(defn get-ingridients-group [group]
  (let [ingridients (cheerio->array (.find group "ul > li"))]
    {:name (get-group-name group)
     :list (get-ingridients-list ingridients)}))

(defn get-preperation-group [group]
  (let [steps (cheerio->array (.find group "ol > li"))]
    {:name (get-group-name group)
     :list (get-preperation-list steps)}))

(defn get-ingridients [doc]
  (let [groups (cheerio->array (doc "#qv-ingredient-section > div > div > div"))]
    (map get-ingridients-group groups)))

(defn get-preperation [doc]
  (let [groups (cheerio->array (doc "#qv-preparation-section > div > div[class='recipe-step-groups']"))]
    (map get-preperation-group groups)))

(defn extract-image [content title]
  (let [from (str/index-of content "savepage_PageLoader")
        start (+ (str/index-of content "resourceBase64Data[8] =" from) 25)
        end (str/index-of content "\";" start)
        image (subs content start end)
        fileName (str (str/replace title #"[/\\?%*:|<>]" "-") ".jpg")]
    (writeFileSync (str "s:\\cimg\\" fileName) image "base64")
    fileName))

(defn scanRecipe [doc content]
  (let [title (get-title doc)]
    {:name title
     :summary (get-icons doc)
     :ingridients (get-ingridients doc)
     :preperation (get-preperation doc)
     :nutritional (get-nutritional doc)
     :image (extract-image content title)}))

(defn scan [content]
  (let [doc (cheerio/load content)]
    (if (isRecipe doc)
      (scanRecipe doc content) ())))

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
  (let [content (readFileSync (:file recipe) "utf8")]
    (scan content)))

(defn show-progress [idx cnt]
  (if (= (rem (dec idx) 10) 0) (prn (str "Processing " idx "/" cnt)) ()))

(defn run [& args]
  (let [path (first args)
        recipes (map-indexed vector (readBooks path))
        cnt (count recipes)]
    (doseq [[idx recipe] (take 20 recipes)]
      (show-progress idx cnt)
      (prn (procRecipe recipe)))))
