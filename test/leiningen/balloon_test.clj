(ns leiningen.balloon-test
  (:require [clojure.test :refer :all]
            [cheshire.core :as json]
            [clipboard :as cb]
            [balloon.core :as b]
            [leiningen.balloon :as lb]))

(def dc "deflate-called")
(def ic "inflate-called")
(def jpsc "json/parse-string-called")
(def jgsc "json/generate-string-called")

(defn mock-deflate [& args] (conj args dc))
(defn mock-inflate [& args] (conj args ic))
(defn mock-json-parse-string [& args] (conj args jpsc))
(defn mock-json-generate-string [& args] (conj args jgsc))
(defn mock-slurp [v] (str {:a v}))
(defn mock-paste [] (str {:a "clipboard"}))
(defn mock-read-line [] (list (cb/paste) "wait enter"))

(deftest deflate
  (testing "Calling deflate command"
    (with-redefs [b/deflate                mock-deflate
                  leiningen.core.main/info identity]
      (let [value  {:a "b"}
            result (lb/balloon nil "deflate" (str value))]
        (is (= result (str (list dc value)))))))

  (testing "Calling deflate command with delimiter"
    (with-redefs [b/deflate                mock-deflate
                  leiningen.core.main/info identity]
      (let [value  {:a "b"}
            result (lb/balloon nil "deflate" (str value) ":delimiter" "*")]
        (is (= result (str (list dc value :delimiter "*")))))))

  (testing "Calling deflate command with json format input"
    (with-redefs [b/deflate                mock-deflate
                  json/parse-string        mock-json-parse-string
                  leiningen.core.main/info identity]
      (let [value  "{\"a\": \"b\"}"
            result (lb/balloon nil "deflate" value "-t" "json")]
        (is (= result (str (list dc (list jpsc value true))))))))

  (testing "Calling deflate command with json format output"
    (with-redefs [b/deflate                mock-deflate
                  json/generate-string     mock-json-generate-string
                  leiningen.core.main/info identity]
      (let [value  {:a "b"}
            result (lb/balloon nil "deflate" (str value) "-T" "json")]
        (is (= result (str (list jgsc (list dc value))))))))

  (testing "Calling deflate command with delimiter and json format input/output"
    (with-redefs [b/deflate                mock-deflate
                  json/parse-string        mock-json-parse-string
                  json/generate-string     mock-json-generate-string
                  leiningen.core.main/info identity]
      (let [value  "{\"a\": \"b\"}"
            result (lb/balloon nil "deflate" value ":delimiter" "*" "-t" "json" "-T" "json")]
        (is (= result (str (list jgsc
                                 (list dc
                                       (list jpsc value true)
                                       :delimiter "*"))))))))

  (testing "Calling deflate command using file"
    (with-redefs [b/deflate                mock-deflate
                  slurp                    mock-slurp
                  leiningen.core.main/info identity]
      (let [filename "file.edn"
            result   (lb/balloon nil "deflate" "-f" filename)]
        (is (= result (str (list dc {:a filename})))))))

  (testing "Calling deflate command using file with delimiter and json format input/output"
    (with-redefs [b/deflate                mock-deflate
                  slurp                    mock-slurp
                  json/parse-string        mock-json-parse-string
                  json/generate-string     mock-json-generate-string
                  leiningen.core.main/info identity]
      (let [filename  "file.json"
            result (lb/balloon nil "deflate" ":delimiter" "_" "-f" filename "-t" "json" "-T" "json")]
        (is (= result (str (list jgsc
                                 (list dc
                                       (list jpsc (str {:a filename}) true)
                                       :delimiter "_"))))))))

  (testing "Calling deflate command using clipboard input"
    (with-redefs [b/deflate                mock-deflate
                  cb/paste                 mock-paste
                  leiningen.core.main/info identity]
      (let [result (lb/balloon nil "deflate" "-c" "true")]
        (is (= result (str (list dc {:a "clipboard"})))))))

  (testing "Calling deflate command using clipboard output"
    (with-redefs [b/deflate                mock-deflate
                  read-line                mock-read-line
                  leiningen.core.main/info identity]
      (let [value  {:a "b"}
            result (lb/balloon nil "deflate" (str value) "-C" "true")]
        (is (= result (list
                       (str (list dc value))
                       "wait enter"))))))

  (testing "Calling deflate command using clipboard with delimiter and json format output"
    (with-redefs [b/deflate                mock-deflate
                  json/generate-string     mock-json-generate-string
                  read-line                mock-read-line
                  leiningen.core.main/info identity]
      (let [value  {:a "b"}
            result (lb/balloon nil "deflate" (str value) ":delimiter" "&" "-C" "true" "-T" "json")]
        (is (= result (list (str (list jgsc
                                       (list dc value :delimiter "&")))
                            "wait enter")))))))

(deftest inflate
  (testing "Calling inflate command"
    (with-redefs [b/inflate                mock-inflate
                  leiningen.core.main/info identity]
      (let [value  {:a.b "c"}
            result (lb/balloon nil "inflate" (str value))]
        (is (= result (str (list ic value)))))))

  (testing "Calling inflate command with delimiter"
    (with-redefs [b/inflate                mock-inflate
                  leiningen.core.main/info identity]
      (let [value  {:a*c "c"}
            result (lb/balloon nil "inflate" (str value) ":delimiter" "*")]
        (is (= result (str (list ic value :delimiter "*")))))))

  (testing "Calling inflate command with json format input"
    (with-redefs [b/inflate                mock-inflate
                  json/parse-string        mock-json-parse-string
                  leiningen.core.main/info identity]
      (let [value  "{\"a.b\": \"c\"}"
            result (lb/balloon nil "inflate" value "-t" "json")]
        (is (= result (str (list ic (list jpsc value true))))))))

  (testing "Calling inflate command with json format output"
    (with-redefs [b/inflate                mock-inflate
                  json/generate-string     mock-json-generate-string
                  leiningen.core.main/info identity]
      (let [value  {:a.b "c"}
            result (lb/balloon nil "inflate" (str value) "-T" "json")]
        (is (= result (str (list jgsc (list ic value))))))))

  (testing "Calling inflate command with delimiter and json format input/output"
    (with-redefs [b/inflate                mock-inflate
                  json/parse-string        mock-json-parse-string
                  json/generate-string     mock-json-generate-string
                  leiningen.core.main/info identity]
      (let [value  "{\"a.b\": \"c\"}"
            result (lb/balloon nil "inflate" value ":delimiter" "*" "-t" "json" "-T" "json")]
        (is (= result (str (list jgsc
                                 (list ic
                                       (list jpsc value true)
                                       :delimiter "*"))))))))

  (testing "Calling inflate command using file"
    (with-redefs [b/inflate                mock-inflate
                  slurp                    mock-slurp
                  leiningen.core.main/info identity]
      (let [filename "file.edn"
            result   (lb/balloon nil "inflate" "-f" filename)]
        (is (= result (str (list ic {:a filename})))))))

  (testing "Calling inflate command using file with delimiter and json format input/output"
    (with-redefs [b/inflate                mock-inflate
                  slurp                    mock-slurp
                  json/parse-string        mock-json-parse-string
                  json/generate-string     mock-json-generate-string
                  leiningen.core.main/info identity]
      (let [filename  "file.json"
            result (lb/balloon nil "inflate" ":delimiter" "_" "-f" filename "-t" "json" "-T" "json")]
        (is (= result (str (list jgsc
                                 (list ic
                                       (list jpsc (str {:a filename}) true)
                                       :delimiter "_"))))))))

  (testing "Calling inflate command using clipboard input"
    (with-redefs [b/inflate                mock-inflate
                  cb/paste                 mock-paste
                  leiningen.core.main/info identity]
      (let [result (lb/balloon nil "inflate" "-c" "true")]
        (is (= result (str (list ic {:a "clipboard"})))))))

  (testing "Calling inflate command using clipboard output"
    (with-redefs [b/inflate                mock-inflate
                  read-line                mock-read-line
                  leiningen.core.main/info identity]
      (let [value  {:a "b"}
            result (lb/balloon nil "inflate" (str value) "-C" "true")]
        (is (= result (list
                       (str (list ic value))
                       "wait enter"))))))

  (testing "Calling inflate command using clipboard with delimiter and json format output"
    (with-redefs [b/inflate                mock-inflate
                  json/generate-string     mock-json-generate-string
                  read-line                mock-read-line
                  leiningen.core.main/info identity]
      (let [value  {:a "b"}
            result (lb/balloon nil "inflate" (str value) ":delimiter" "&" "-C" "true" "-T" "json")]
        (is (= result (list (str (list jgsc
                                       (list ic value :delimiter "&")))
                            "wait enter")))))))
