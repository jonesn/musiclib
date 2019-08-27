(ns nz.co.arachnid.musiclib.domain
  (:require [clojure.spec.alpha :as s]
            [me.raynes.fs :as fs]))

;; ===============
;;    Constants
;; ===============

(def FLAC_EXT ".FLAC")
(def MP3_EXT ".MP3")
;; M3U is a playlist format.
(def M3U_EXT ".M3U")
(def ORPHAN "Orphan")
(def WINDOWS_PATH_SEPARATOR "\\")

(def ORPHAN-EXTENSIONS #{FLAC_EXT MP3_EXT M3U_EXT})

(defn- define-format
  "Takes the extension off the end of a file name and upper cases it.
   It then categorizes the extension as a format.
   :mp3
   :flac
   :not-supported"
  [file-path]
  (when (and file-path (fs/extension file-path))
    (let [extn (clojure.string/upper-case (fs/extension file-path))]
      (case extn
        ;; The values to match must be straight literals.
        ".MP3"    :mp3
        ".FLAC"   :flac
        :not-supported))))


;; ===============
;;     Records
;; ===============

(defn non-blank-string?
  [s]
  (and (string? s)
       (not (clojure.string/blank? s))))

(defn not-nil?
  [ex]
  (not (nil? ex)))

(s/check-asserts true)

(s/def ::artist    non-blank-string?)
(s/def ::album     non-blank-string?)
(s/def ::root-file non-blank-string?)
(s/def ::song-set  (s/coll-of non-blank-string? :kind set?))
(s/def ::format    (hash-set :mp3 :flac :not-supported))

(s/def ::artist-song-set  (s/keys :req-un [::artist ::album ::root-file ::song-set ::format]))
(s/def ::artist-song-sets (s/and not-nil? (s/+ ::artist-song-set)))

(defrecord ArtistSongSet
  [artist album root-file song-set format])

(defrecord LibStats
  [album-count artist-count mp3-count flac-count orphan-count])

(defn create-artist-song-set
  [artist album root-file song-set]
  (let [song-set (cond
                    (set? song-set) song-set
                    (seq? song-set) (into (sorted-set) song-set)
                    :else           (sorted-set song-set))
        format   (define-format (first song-set))]
    (s/assert ::artist-song-set (->ArtistSongSet artist album root-file song-set format))))


;; ================
;;    Assertions
;; ================

(defn assert-valid-lib
  [lib]
  (s/assert ::artist-song-sets lib))