(ns leiningen.balloon
  (:require [clojure.tools.cli :refer [parse-opts]]
            [balloon.core :as b]))

(defn balloon
  "Pass in your hash-map."
  [project & args]
  (let [{:keys [arguments] :as result} (parse-opts args [])
        command                        (first arguments)
        value                          (second arguments)]
    (if (= "inflate" command)
      (println (b/inflate (read-string value)))
      (println (b/deflate (read-string value))))))
