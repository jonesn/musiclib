(ns nz.co.arachnid.musiclib.mp3
  (:use [nz.co.arachnid.musiclib.domain])
  (:use [nz.co.arachnid.musiclib.search])
  (:import (com.mpatric.mp3agic Mp3File)))

(defn- extract-metadata-from-mp3
  [mp3-file-path]
  (let [mp3-file (new Mp3File mp3-file-path)]
    (when-let [id3v2Tag (.getId3v2Tag mp3-file)]
      {:artist       (.getAlbumArtist id3v2Tag)
       :album        (.getAlbum       id3v2Tag)
       :title        (.getTitle       id3v2Tag)
       :track        (first (clojure.string/split (.getTrack id3v2Tag) #"/"))
       :source-path  mp3-file-path})))

(defn construct-destination-path
  [root-path-str metadata-map]
  (str root-path-str
       WINDOWS_PATH_SEPARATOR
       (:artist metadata-map)
       WINDOWS_PATH_SEPARATOR
       (:album metadata-map)
       WINDOWS_PATH_SEPARATOR
       (:track metadata-map) " - " (clojure.string/replace (:title metadata-map) "/" "_")
       MP3_EXT))

(defn filter-orphan-records
  [music-lib]
  (when music-lib
    (->> music-lib
         (filter #(= (:artist %) ORPHAN)))))


(defn extract-orphan-meta-data
  [music-lib]
  (when music-lib
    (let [orphaned-recs (filter-orphan-records music-lib)
          root-path-str (:path (first orphaned-recs))]
      (->> orphaned-recs
           (filter #(= (:format %) :mp3))
           (map :songs)
           (flatten)
           (map (fn [file-str] (str root-path-str WINDOWS_PATH_SEPARATOR file-str)))
           (map #(extract-metadata-from-mp3 %))
           (filter #(:artist %))
           (map #(assoc % :dest-path (construct-destination-path root-path-str %)))))))





