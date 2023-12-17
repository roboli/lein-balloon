(ns leiningen.balloon-test
  (:require [clojure.test :refer :all]
            [cheshire.core :as json]
            [balloon.core :as b]
            [leiningen.balloon :as lb]))

(def dc "deflate-called")
(def jpsc "json/parse-string-called")
(def jgsc "json/generate-string-called")

(defn mock-deflate [& args] (conj args dc))
(defn mock-json-parse-string [& args] (conj args jpsc))
(defn mock-json-generate-string [& args] (conj args jgsc))

(deftest deflate
  (testing "Calling deflate command"
    (with-redefs [b/deflate                mock-deflate
                  leiningen.core.main/info identity]
      (let [value  {:a "b"}
            result (lb/balloon nil "deflate" (str value))]
        (is (= result (list dc value))))))

  (testing "Calling deflate command with delimiter"
    (with-redefs [b/deflate                mock-deflate
                  leiningen.core.main/info identity]
      (let [value  {:a "b"}
            result (lb/balloon nil "deflate" (str value) ":delimiter" "*")]
        (is (= result (list dc value :delimiter "*"))))))

  (testing "Calling deflate command with json format input"
    (with-redefs [b/deflate                mock-deflate
                  json/parse-string        mock-json-parse-string
                  leiningen.core.main/info identity]
      (let [value  "{\"a\": \"b\"}"
            result (lb/balloon nil "deflate" value "-t" "json")]
        (is (= result (list dc (list jpsc value true)))))))

  (testing "Calling deflate command with json format output"
    (with-redefs [b/deflate                mock-deflate
                  json/generate-string     mock-json-generate-string
                  leiningen.core.main/info identity]
      (let [value  {:a "b"}
            result (lb/balloon nil "deflate" (str value) "-T" "json")]
        (is (= result (list jgsc (list dc value)))))))

  (testing "Calling deflate command with delimiter and json format input/output"
    (with-redefs [b/deflate                mock-deflate
                  json/parse-string        mock-json-parse-string
                  json/generate-string     mock-json-generate-string
                  leiningen.core.main/info identity]
      (let [value  "{\"a\": \"b\"}"
            result (lb/balloon nil "deflate" value ":delimiter" "*" "-t" "json" "-T" "json")]
        (is (= result (list jgsc
                            (list dc
                                  (list jpsc value true)
                                  :delimiter "*"))))))))
