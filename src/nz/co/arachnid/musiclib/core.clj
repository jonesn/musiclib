(ns nz.co.arachnid.musiclib.core
  (:use     [nz.co.arachnid.musiclib.search])
  (:use     [nz.co.arachnid.musiclib.mp3])
  (:use     [nz.co.arachnid.musiclib.domain])
  (:use     [nz.co.arachnid.musiclib.filesystem])
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as string])
  (:require [clojure.term.colors :as clr])
  (:gen-class))

(def command-line-schema
  [["-h" "--help"]
   ["-p" "--path  The parent directory to scan I.e. C:\\Users\\Nick Jones\\Music OR /home/jonesn/Music"]
   ["-q" "--path2 If provided a diff report will be printed between the lib at 'path' and that at 'path2'"]
   ["-f" "--fix   Should orphaned files be moved to the correct place in the file system."]])

(defn exit! [status msg]
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

(defn print-artist-album!
  [orphan-albums-to-fix-seq]
  (doseq [rec orphan-albums-to-fix-seq]
    (let [strange-array-pair (first (key rec))]
      (printf "%-25s %-100s \n" (first strange-array-pair) (second strange-array-pair)))))


(defn print-lib-diff!
  [lib-a-grouping lib-b-grouping]
  (println (clr/blue "============================"))
  (println (clr/blue "          Lib A Only        "))
  (println (clr/blue "============================"))
  (print-artist-album! lib-a-grouping)
  (println (clr/blue "============================"))
  (println (clr/blue "          Lib B Only        "))
  (println (clr/blue "============================"))
  (print-artist-album! lib-b-grouping))


(defn print-stats-only!
  [stats orphan-albums-to-fix]
  (println (clr/blue "============================"))
  (println (clr/blue "          SUMMARY           "))
  (println (clr/blue "============================"))
  (printf "%-20s %6d \n" "Artist Total:" (:artist-count stats))
  (printf "%-20s %6d \n" "Album Total:"  (:album-count stats))
  (printf "%-20s %6d \n" "MP3 Total:"    (:mp3-count stats))
  (printf "%-20s %6d \n" "FLAC Total:"   (:flac-count stats))
  (printf "%-20s %6d \n" "Orphan Total:" (:orphan-count stats))
  (println)
  (when orphan-albums-to-fix
    (println (clr/blue "============================"))
    (println (clr/blue "         TO FIX             "))
    (println (clr/blue "============================"))
    (println)
    (print-artist-album! orphan-albums-to-fix))
  (println (clr/blue "============================")))

(defn run
  [path-string path2-string fix?]
  (let [lib-a                (generate-music-lib path-string)
        lib-b                (generate-music-lib path2-string)
        stats                (generate-library-stats lib-a)
        orphan-meta-data     (extract-orphan-meta-data lib-a)
        orphan-albums-to-fix (group-lib-by-artist-album orphan-meta-data)]
    (print-stats-only! stats orphan-albums-to-fix)
    (when lib-b
          (let [diff-libs  (diff-libs lib-a lib-b)
                lib-a-only (group-lib-by-artist-album (:lib-a-only diff-libs))
                lib-b-only (group-lib-by-artist-album (:lib-b-only diff-libs))]
            (print-lib-diff! lib-a-only lib-b-only)))
    (when fix?
      (fix-orphans-in-lib! path-string orphan-albums-to-fix))))


(defn -main
  [& args]
  (let [cli-params (parse-opts args command-line-schema)
        options    (:options cli-params)
        summary    (:summary cli-params)]
    (println options)
    (cond
      (:help options)                        (exit! 0 (usage summary))
      (< (count options) 1)                  (exit! 1 (usage summary))
      (and (:path options) (:fix options))   (run  (:path options) nil true)
      (and (:path options) (:path2 options)) (run  (:path options) (:path2 options) false)
      :default                               (run  (:path options) nil false))))

;; ================================
;;       REPL Test Functions
;; ================================

(comment
  (def path               "/home/jonesn/Music")
  (def lib-a              (generate-music-lib path))
  (def lib-b              (generate-music-lib "/run/media/jonesn/PONOPLAYER/Music"))
  lib-a
  (def orphaned-records (filter-for-artist ORPHAN lib-a))
  (def stats            (generate-library-stats   lib-a))
  (def orphan-seq       (extract-orphan-meta-data lib-a))
  (def orphan-stats     (group-lib-by-artist-album orphan-seq))
  (def diff             (diff-libs lib-a lib-b))
  (print-stats-only! stats orphan-stats)
  (fix-orphans-in-lib!  path orphan-stats)
  (run path false)
  (take 5 orphan-seq)
  (take 5 orphan-stats)
  (count orphan-seq)
  (count orphan-stats)
  (run path false)
  (user/rebl-start)
  (user/rebl-send lib-a)
  (user/rebl-send orphan-seq)
  (user/rebl-send orphan-stats)
  (user/rebl-send diff)
  (run path false))

