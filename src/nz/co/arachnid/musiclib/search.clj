(ns
  ^{:doc
    "Search
     ======

    This namespace provides 2 facilities
    1. To create a sequence of artist-entries.
    2. To provide stats for those entries."}
  nz.co.arachnid.musiclib.search
  (:require [me.raynes.fs :as fs])
  (:use     [nz.co.arachnid.musiclib.domain]))

;; ===============
;;    Utilities
;; ===============

(defn exists?
  "Returns true if item extists in the given collection otherwise false."
  [item collection]
  (if (some #{item} collection)
    true))


(defn- upper-extension
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


(defn- filter-orphan-extensions
  [file-set]
  (filter
    (fn [file-path] (exists? (upper-extension file-path) ORPHAN-EXTENSIONS))
    file-set))


(defn- extract-music-file-details
  [root-file directory-set file-set]
  (cond
    ;; Check if we are at a leaf node
    (empty? directory-set)             (create-artist-entry
                                         (extract-artist root-file)
                                         (fs/name root-file)
                                         file-set
                                         (.getAbsolutePath root-file))
    ;; Otherwise check for orphaned files
    (and (not (empty? directory-set))
         (not (empty? file-set)))      (create-artist-entry
                                         ORPHAN
                                         ORPHAN
                                         (filter-orphan-extensions file-set)
                                         (.getAbsolutePath root-file))))

(defn- count-extensions
  [music-lib-seq extn-string]
  (->>
    music-lib-seq
    (map :songs)
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
      (filter #(not (nil? %))))))


(defn generate-library-stats
  [lib]
  (let [mp3-count    (count-extensions lib MP3_EXT)
        flac-count   (count-extensions lib FLAC_EXT)
        artist-count (count-entry-by-key :artist lib)
        album-count  (count-entry-by-key :album lib)
        orphan-count (count-songs-for-artist ORPHAN lib)]
    (create-result-entry album-count artist-count mp3-count flac-count orphan-count)))


(defn generate-orphan-stats
  [orphan-rec-seq]
  (group-by (fn [rec] {(:artist rec) (:album rec)})
            orphan-rec-seq))