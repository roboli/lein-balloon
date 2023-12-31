(ns leiningen.balloon
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as string]
            [clojure.edn :as edn]
            [cheshire.core :as json]
            [clipboard :as cb]
            [balloon.core :as b]))

;; Following example from https://github.com/clojure/tools.cli

(def cli-options
  [;; First three strings describe a short-option, long-option with optional
   ;; example argument description, and a description. All three are optional
   ;; and positional.
   ["-t" "--input-type FORMAT" "Input format in edn or json"
    :default "edn"
    :validate [#(or (= "edn" %)
                    (= "json" %)) "Must be edn or json"]]
   ["-T" "--output-type FORMAT" "Output format in edn or json"
    :default "edn"
    :validate [#(or (= "edn" %)
                    (= "json" %)) "Must be edn or json"]]
   ["-f" "--file NAME" "File with value to read"]
   ["-c" "--input-clipboard BOOLEAN" "Use value from clipboard"
    :validate [#(= "true" %) "Must be true"]]
   ["-C" "--output-clipboard BOOLEAN" "Use value from clipboard"
    :validate [#(= "true" %) "Must be true"]]
   ["-h" "--help"]])

(defn usage [options-summary]
  (->> [""
        "Usage: command [arguments] [options]"
        ""
        "Commands:"
        "  deflate  Flat a nested hash-map"
        "  inflate  Unflat hash-map into a nested one"
        ""
        "Options:"
        options-summary
        ""
        "Examples: lein balloon inflate '{:a.b \"c\"}'"
        "          lein balloon deflate '{:a {:b \"c\"}}' :delimiter '*'"
        ""]
       (string/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn validate-args
  "Validate command line arguments. Either return a map indicating
  balloon should exit (with an error message, and optional ok status),
  or a map indicating the command balloon should take and the value
  with options provided."
  [args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
      (:help options)             ; help => exit OK with usage summary
      {:exit-message (usage summary) :ok? true}
      errors               ; errors => exit with description of errors
      {:exit-message (error-msg errors)}
      ;; custom validation on arguments
      (and (or (<= 2 (count arguments))
               (and (<= 1 (count arguments))
                    (:file options))
               (and (<= 1 (count arguments))
                    (:input-clipboard options)))
           (#{"deflate" "inflate"} (first arguments)))
      {:command (first arguments)
       :arguments (rest arguments)
       :options options}
      :else      ; failed custom validation => exit with usage summary
      {:exit-message (usage summary)})))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn parse-delimiter-args
  "Do not parse value for the :delimiter option"
  [args]
  (reverse (:args
            (reduce
             (fn [acc v]
               (if (= v ":delimiter")
                 (-> acc
                     (assoc :prev-delim true)
                     (update :args conj :delimiter))
                 (if (:prev-delim acc)
                   (-> acc
                       (assoc :prev-delim false)
                       (update :args conj v))
                   (update acc :args conj (edn/read-string v)))))
             {:prev-delim false
              :args '()}
             args))))

(defn ^{:pass-through-help true
        :no-project-needed true}
  balloon
  "Use \"help\" for more info"
  [project & args]
  (let [{:keys [command arguments options exit-message ok?]} (validate-args args)]
    (if exit-message
      (exit (if ok? 0 1) exit-message)
      (let [input-args  (cond (:file options)
                              (conj arguments (slurp (:file options)))

                              (:input-clipboard options)
                              (conj arguments (cb/paste))

                              :else arguments)
            parsed-args (if (= "json" (:input-type options))
                          (conj (parse-delimiter-args (rest input-args))
                                (json/parse-string (first input-args) true))
                          (parse-delimiter-args input-args))
            result      (if (= "inflate" command)
                          (apply b/inflate parsed-args)
                          (apply b/deflate parsed-args))]
        (let [output (if (= "json" (:output-type options))
                       (json/generate-string result)
                       result)]
          (if (:output-clipboard options)
            (do
              (cb/copy output)
              ;; Keep process running to allow value go into clipboard
              ;; More here https://stackoverflow.com/a/14447799/2202143
              (leiningen.core.main/info "Go paste your stuff and press enter when done!")
              (read-line))
            (leiningen.core.main/info (str output))))))))
