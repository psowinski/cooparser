(ns app.main
  (:require [fs :refer (readdirSync)]))

(defn run [& args]
  (prn args)
  (let [path (first args)
        dirs (readdirSync path)]
    (doseq [d dirs]
      (prn d)))

  ;js/__dirname
; ;   (let [x (.opendirSync fs)]
;   (prn (js->clj dirs))
;   (prn (count dirs))
;   (prn "przed")
;   (doseq [i (range 1 6)] (prn i))
;   (prn "po")
  ;(for [d (range 5)]
  ;  (println "taki jest"))

  0)
