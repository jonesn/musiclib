(ns nz.co.arachnid.musiclib.domain)

;; ===============
;;    Constants
;; ===============

(def FLAC_EXT ".FLAC")
(def MP3_EXT ".MP3")
(def ORPHAN "Orphan")

(def ORPHAN-EXTENSIONS #{FLAC_EXT MP3_EXT})

(defn create-artist-entry
  [artist album song-set root-file]
  {:artist artist
   :album  album
   :songs  song-set
   :path   root-file})

(defn create-result-entry
  [album-count artist-count mp3-count flac-count orphan-count]
  {:album-count  album-count
   :artist-count artist-count
   :mp3-count    mp3-count
   :flac-count   flac-count
   :orphan-count orphan-count})