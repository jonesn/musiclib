(ns nz.co.arachnid.musiclib.domain
  (:require [clojure.set :as set]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [me.raynes.fs :as fs]))

;; ===============
;;    Constants
;; ===============

(def FLAC_EXT ".FLAC")
(def MP3_EXT ".MP3")
(def M4A_EXT ".M4A")
(def M3U_EXT ".M3U") ;; M3U is a playlist format.
(def ORPHAN "Orphan")
(def WINDOWS_PATH_SEPARATOR "\\")
(def ORPHAN-EXTENSIONS #{FLAC_EXT MP3_EXT M3U_EXT M4A_EXT})

;; ==================
;;  Helper Functions
;; ==================

(defn nil-default
  [nilable-a default]
  (if (nil? nilable-a)
    default
    nilable-a))

(defn define-format
  "Takes the extension off the end of a file name and upper cases it.
   It then categorizes the extension as a format.
   :mp3
   :flac
   :not-supported"
  [file-path]
  (if file-path
    (let [extn        (fs/extension file-path)
          upper-extn  (when extn (str/upper-case extn))]
      (case upper-extn
        ;; The values to match must be straight literals.
        ".MP3"    :mp3
        ".FLAC"   :flac
        ".M3U"    :m3u
        ".M4A"    :m4a
        :not-supported))
    :not-supported))

(defn define-format-for-song-set
  [song-set]
  (nil-default
    (->> song-set
        (map define-format)
        (filter (fn [format] (not (= :not-supported format))))
        (first))
    :not-supported))

;; ===============
;;     Specs
;; ===============

(defn non-blank-string?
  [s]
  (and (string? s)
       (not (str/blank? s))))

(defn not-nil?
  [ex]
  (not (nil? ex)))

(defn upper
  [s]
  (when s
    (str/upper-case s)))

(s/check-asserts true)

(s/def ::artist    non-blank-string?)
(s/def ::album     non-blank-string?)
(s/def ::root-file non-blank-string?)
(s/def ::song-set  (s/coll-of non-blank-string? :kind set?))
(s/def ::format    (hash-set :mp3 :flac :m3u :m4a :not-supported))
(s/def ::artist-count nat-int?)
(s/def ::album-count  nat-int?)
(s/def ::artist-count nat-int?)
(s/def ::mp3-count    nat-int?)
(s/def ::flac-count   nat-int?)
(s/def ::orphan-count nat-int?)

(s/def ::artist-song-set  (s/keys :req-un [::artist ::album ::root-file ::song-set ::format]))
(s/def ::lib-stat         (s/keys :req-un [::album-count ::artist-count ::mp3-count ::flac-count ::orphan-count]))
(s/def ::artist-song-sets (s/and not-nil? (s/coll-of ::artist-song-set :kind set? :into #{})))

;; ===============
;;     Records
;; ===============

(defrecord ArtistSongSet
  [artist album root-file song-set format])

(defrecord LibStats
  [album-count artist-count mp3-count flac-count orphan-count])

(defn artist-song-key-set
  [artist-song-set]
  {:artist (upper (:artist artist-song-set))
   :album  (upper (:album  artist-song-set))
   :format (upper (:format  artist-song-set))})

(defn create-artist-song-set
  ([m-params]
   "Map element constructor takes a map that needs to conform to ::artist-song-set"
   (s/assert ::artist-song-set (map->ArtistSongSet m-params)))
  
  ([artist album root-file song-set]
   "All elements constructor takes an artist, album, the root file path and a set of songs.
    The resulting ArtistSongSet will conform to spec ::artist-song-set"
   (let [sanitised-song-set (cond
                              (set? song-set)  song-set
                              (seq? song-set)  (into (sorted-set) song-set)
                              (coll? song-set) (into (sorted-set) song-set)
                              :else            (sorted-set song-set))
         format             (define-format-for-song-set sanitised-song-set)]
      (s/assert ::artist-song-set (->ArtistSongSet artist album root-file sanitised-song-set format)))))


(defn create-lib-stats
  ([m-params]
   "Map element constructor takes a map that needs to conform to the ::lib-stats spec"
   (s/assert ::lib-stat (map->LibStats m-params)))


  ([album-count artist-count mp3-count flac-count orphan-count]
   "All elements constructor. Result conforms to ::lib-stats spec."
   (s/assert ::lib-stat (->LibStats album-count artist-count mp3-count flac-count orphan-count))))

;; ================
;;    Assertions
;; ================

(defn artist-song-in-lib?
  "Compares ArtistSongSet on artist, album, format"
  [song-to-check lib]
  (not
    (empty?
      (set/intersection
        #{(artist-song-key-set song-to-check)}
        (into #{} lib)))))

(defn assert-valid-lib!
  [lib]
  (s/assert ::artist-song-sets lib))

(defn valid-lib?
  [lib]
  (s/valid? ::artist-song-sets lib))


