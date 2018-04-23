(ns leiningen.four
  "A plugin for working on 4clojure problems."
  (:use clojure.java.io)
  (:require [clojure.string :as str]))

(defn indent-rest-lines
  "Indents the lines after the first with the specified number of spaces.

e.g. (indent-rest-lines \"A\\nB\\nC\" 3) => \"A\\n   B\\n   C\""
  [text columns]
  (let [lines (str/split-lines text)]
    (str/join "\n" (cons (first lines)
                         (for [line (rest lines)]
                           (str (apply str (repeat columns " "))
                                line))))))

(defn get-prob-title [body]
  (let [matches (re-seq #"<div id=\"prob-title\">(.+?)<\/div>" body)]
    (if-not (nil? matches)
      (->> matches
           (first)
           (second)))))

(defn get-prob-desc [body]
  (let [matches (re-seq #"<div id=\"prob-desc\">(.+?)<br \/>" body)]
    (if-not (nil? matches)
      (->> matches
           (first)
           (second)))))

(defn fetch-body
  [problem]
  (println "Fetching test cases from the 4clojure website...")
  (try
    (slurp (str "http://www.4clojure.com/problem/" problem))
    (catch Exception e
      nil)))

(defn get-test-cases
  [body]
  (if-not body
    (do (println "Could not reach 4clojure website.")
        (str "(def test-cases\n"
             " '[\n"
             "    ; copy the 4clojure test cases here\n"
             "  ])"))
    (let [matches (re-seq #"(?<=\<pre class=\"test\"\>)[\s\S]+?(?=\</pre\>)" body)]
      (str "(def test-cases\n"
           " '["
           (str/join "\n   " (for [test-case matches]
                               (indent-rest-lines test-case 3)))
           "])"))))

(defn file-template
  [nsname problem]
  (let [body (fetch-body problem)]
    (str "(ns " nsname ")\n\n"
         "; " (get-prob-title body) "\n"
         "; " (get-prob-desc body) "\n\n"
         (get-test-cases body) "\n\n"
         "(def __\n"
         "  ; fill in the blank!\n"
         "  )\n\n"
         "(defn test-code\n"
         "  []\n"
         "  (doseq [[test-case test-number] (map vector test-cases (range))]\n"
         "    (if (eval `(let [~'__ __]\n"
         "                 ~test-case))\n"
         "      (printf \"Test #%d passed!\\n\" (inc test-number))\n"
         "      (printf \"Test #%d failed!\\n\" (inc test-number)))))\n")))

(defn ^:no-project-needed four
  "A plugin for working on 4clojure problems in the comfort of your own IDE.

lein four :ns <name>
          :problem <4clojure-problem-number>
          :filename <path>

Will create a namespace with the given ns name (or resort to \"problemN\" if :problem is given),
at the filename given (or create appropriate subdirectories in your src folder based on the ns name).
Some tools will be found inside the namespace, including test cases fetched directly from the 4clojure website
(if :problem is given).

These keywords are mostly optional: at least :ns or :problem must be provided to name the namespace,
and :filename is required if you're not inside a lein project."
  [project & args]
  (let [args (for [arg args]
               (if (= (first arg) \:)
                 (keyword (apply str (rest arg)))
                 arg))
        args (try (apply hash-map args)
                  (catch Exception e
                    nil))]
    (cond
      (not args) (println "Args don't form a proper map.")
      (not (or (nil? (:problem args))
               (try (Integer/parseInt (:problem args))
                    (catch Exception e nil)))) (println ":problem must be an integer, or ommitted from the arg map.")
      (not (or (:problem args)
               (:ns args))) (println "Please provide at least :problem or :ns; I need to know what to call the namespace!")
      (not (or (:root project)
               (:filename args))) (println "A filename must be provided if you are not in a lein project.")
      :else
      (let [src (first (:source-paths project))
            filename (:filename args)
            problem (try (Integer/parseInt (:problem args))
                         (catch Exception e nil))
            nsname (or (:ns args) (str "problem" problem))
            file-path (or filename
                          (str src "/" (clojure.string/replace nsname #"\." "/") ".clj"))
            source (file-template nsname problem)]
        (spit file-path source)
        (printf "A 4clojure template for problem #%d has been created with namespace %s.\n\n"
                problem nsname)
        (flush)))))
