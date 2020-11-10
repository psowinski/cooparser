(ns app.symbols
    (:require [app.tools :as tools]))

(defn recognize-symbol-name [name]
  (case name
    "icon-dough-mode" "(wyrabianie ciasta)"
    "icon-reverse" "(obroty wsteczne)"
    "icon-stirring" "(mieszanie)"
    "icon-lid-closed" "(zamknięta pokrywa)"
    (throw (str "(NIEZNANY SYMBOL: " name ")"))))

(defn update-symbol-text [symbol name]
  (.text symbol (recognize-symbol-name name))
  symbol)

(defn get-symbol-name [symbol]
  (let [class (tools/get-attr symbol "class")
        name (subs class 5)]
    name))

(defn convert-symbol-to-text [symbol]
  (update-symbol-text symbol (get-symbol-name symbol)))

(defn load-symbols [span]
  (tools/cheerio->array (.find span ".icon")))

(defn update-symbols [span]
  (let [symbols (load-symbols span)]
    (doseq [symbol symbols]
      (convert-symbol-to-text symbol))
    span))
