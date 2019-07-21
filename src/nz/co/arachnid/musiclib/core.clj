(ns nz.co.arachnid.musiclib.core
  (:use     [nz.co.arachnid.musiclib.search])
  (:use     [nz.co.arachnid.musiclib.mp3])
  (:use     [nz.co.arachnid.musiclib.domain])
  (:use     [nz.co.arachnid.musiclib.filesystem])
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as string])
  (:gen-class))

(def command-line-schema
  [["-h" "--help"]
   ["-p" "--path  The parent directory to scan I.e.
                  - C:\\Users\\Nick Jones\\Music
                  - /home/jonesn/Music"]
   ["-f" "--fix   Should orphaned files be moved to the correct place in the file system."]])

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn usage
  [options-summary]
  (->> ["This program prints the statistics of the tree of Music under the given path."
        "It also provides the option to try and correct files that don't fall into the
         artist/album/song hierarchy."
        ""
        "Example Usage Windows: musicLib -p C:\\Users\\Nick Jones\\Music"
        "Example Usage Linux:   musicLib -p /home/jonesn/Music"
        "java -jar C:\\dev\\arachnid\\clojure\\musiclib\\target\\uberjar\\musiclib-0.1.0-SNAPSHOT-standalone.jar --path \"C:\\Users\\Nick Jones\\Music\" --fix true"
        ""
        "Options:"
        options-summary]
       (string/join \newline)))

(defn print-orphaned-albums
  [orphan-albums-to-fix-seq]
  (doseq [rec orphan-albums-to-fix-seq]
    (let [strange-array-pair (first (key rec))]
      (printf "%-25s %-100s \n" (first strange-array-pair) (second strange-array-pair)))))
  

(defn print-stats-only
  [stats orphan-albums-to-fix]
  (printf "%-20s %6d \n" "Artist Total:" (:artist-count stats))
  (printf "%-20s %6d \n" "Album Total:"  (:album-count stats))
  (printf "%-20s %6d \n" "MP3 Total:"    (:mp3-count stats))
  (printf "%-20s %6d \n" "FLAC Total:"   (:flac-count stats))
  (printf "%-20s %6d \n" "Orphan Total:" (:orphan-count stats))
  (println)
  (println "============================")
  (println "         TO FIX             ")
  (println "============================")
  (println)
  (print-orphaned-albums orphan-albums-to-fix)
  (println "============================"))

(defn run
  [path-string fix?]
  (let [lib                   (generate-music-lib       path-string)
        stats                 (generate-library-stats   lib)
        orphan-meta-data      (extract-orphan-meta-data lib)
        orphan-albums-to-fix  (generate-orphan-stats    orphan-meta-data)]
    (print-stats-only stats orphan-albums-to-fix)
    (when fix?
      (fix-orphans-in-lib! path-string orphan-albums-to-fix))))


(defn -main
  [& args]
  (let [cli-params (parse-opts args command-line-schema)
        options    (:options cli-params)
        summary    (:summary cli-params)]
    (cond
      (:help options)                       (exit 0 (usage summary))
      (< (count options) 1)                 (exit 1 (usage summary))
      (and (:path options) (:fix options))  (run  (:path options) true)
      :default                              (run  (:path options) false))))

;; ================================
;;       REPL Test Functions
;; ================================

(comment
  (def path             "C:\\Users\\Nick Jones\\Music")
  (def lib              (generate-music-lib "C:\\Users\\Nick Jones\\Music"))
  (def orphaned-records (filter-for-artist ORPHAN lib))
  (def stats            (generate-library-stats   lib))
  (def orphan-seq       (extract-orphan-meta-data lib))
  (def orphan-stats     (generate-orphan-stats    orphan-seq))
  (print-stats-only stats orphan-stats)
  (fix-orphans-in-lib!  path orphan-stats)
  (run path false)
  (take 5 orphan-seq)
  (take 5 orphan-stats)
  (count orphan-seq)
  (count orphan-stats)
  (run path false)
  (user/rebl-start)
  (user/rebl-send orphan-seq)
  (user/rebl-send orphan-stats))

