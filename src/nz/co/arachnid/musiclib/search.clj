(ns
  ^{:doc
    "Search
     ======

    This namespace provides 2 facilities
    1. To create a sequence of artist-entries.
    2. To provide stats for those entries."}
  nz.co.arachnid.musiclib.search
  (:require [me.raynes.fs :as fs])
  (:require [clojure.set  :as set])
  (:use     [nz.co.arachnid.musiclib.domain]))

;; ===============
;;    Utilities
;; ===============

(defn exists?
  "Returns true if item extists in the given collection otherwise false."
  [item collection]
  (if (some #{item} collection)
    true))


(defn upper-extension
  "Takes the extension off the end of a file name and upper cases it. Results include the '.'
   I.e. '.MP3'"
  [file-path]
  (when (and file-path (fs/extension file-path))
    (clojure.string/upper-case (fs/extension file-path))))

;; ===============
;;    Internals
;; ===============

(defn- extract-artist
  [root-file]
  (when root-file
    (second
      (reverse
        (fs/split
          (fs/absolute root-file))))))


(defn- count-extensions
  [music-lib-seq extn-string]
  (->>
    music-lib-seq
    (map :song-set)
    (map seq)
    (flatten)
    (map upper-extension)
    (filter #(= extn-string %))
    (count)))


(defn- count-entry-by-key
  [key music-lib-seq]
  (->>
    music-lib-seq
    (map key)
    (apply sorted-set)
    (count)))


;; ===============
;;      API
;; ===============

(defn filter-orphan-extensions
  [file-set]
  (into []
        (filter (fn [file-path] (exists? (upper-extension file-path) ORPHAN-EXTENSIONS))
                file-set)))

(defn- extract-music-file-details
  [root-file directory-set file-set]
  (cond
    ;; Check if we are at a leaf node
    (empty? directory-set)             (create-artist-song-set
                                         (extract-artist root-file)
                                         (fs/name root-file)
                                         (.getAbsolutePath root-file)
                                         file-set)

    ;; Otherwise check for orphaned files
    (and (not (empty? directory-set))
         (not (empty? file-set)))      (create-artist-song-set
                                         ORPHAN
                                         ORPHAN
                                         (.getAbsolutePath root-file)
                                         (filter-orphan-extensions file-set))))

(defn filter-for-artist
  [artist-str music-lib-seq]
  (->>
    music-lib-seq
    (filter #(= (:artist %) artist-str))
    (map :songs)
    (flatten)))


(defn count-songs-for-artist
  [artist-str music-lib-seq]
  (count
    (filter-for-artist artist-str music-lib-seq)))


(defn generate-music-lib
  [file-path-string]
  (when (string? file-path-string)
    (->>
      (fs/walk extract-music-file-details file-path-string)
      (filter #(not (nil? %)))
      (into #{})
      (assert-valid-lib!))))


(defn generate-library-stats
  [lib]
  (when lib
    (let [mp3-count    (count-extensions lib MP3_EXT)
          flac-count   (count-extensions lib FLAC_EXT)
          artist-count (count-entry-by-key :artist lib)
          album-count  (count-entry-by-key :album lib)
          orphan-count (count-songs-for-artist ORPHAN lib)]
      (create-lib-stats album-count artist-count mp3-count flac-count orphan-count))))


(defn group-lib-by-artist-album
  [orphan-rec-seq]
  (when (not (empty? orphan-rec-seq))
   (group-by (fn [rec] {(:artist rec) (:album rec)})
             orphan-rec-seq)))

;; Change this to use filter based on a subset of the keys in the lib so we can match regardless of path.
(defn diff-libs
  [lib-a lib-b]
  (when
    (and
      (valid-lib? lib-a)
      (valid-lib? lib-b))
    (let [lib-a-keys      (map artist-song-key-set lib-a)
          lib-b-keys      (map artist-song-key-set lib-b)
          lib-a-only      (into #{} (for [rec-a lib-a
                                          :when (not (artist-song-in-lib? rec-a lib-b-keys))]
                                      rec-a))
          lib-a-and-lib-b (into #{} (for [rec-a lib-a
                                          :when (artist-song-in-lib? rec-a lib-b-keys)]
                                      rec-a))
          lib-b-only      (into #{} (for [rec-b lib-b
                                          :when (not (artist-song-in-lib? rec-b lib-a-keys))]
                                      rec-b))]
      {:lib-a-only          lib-a-only
       :lib-a-and-lib-b     lib-a-and-lib-b
       :lib-b-only          lib-b-only})))


