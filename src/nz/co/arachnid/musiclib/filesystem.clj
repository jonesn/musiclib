(ns nz.co.arachnid.musiclib.filesystem
  (:require [me.raynes.fs :as fs])
  (:use     [nz.co.arachnid.musiclib.domain]))

(defn create-directories-if-not-present
  [root-path artist album]
  (let [absolute-artist-path  (str root-path WINDOWS_PATH_SEPARATOR artist)
        absolute-album-path   (str absolute-artist-path WINDOWS_PATH_SEPARATOR album)]
    (when (not (fs/exists? absolute-artist-path))
      (println (str "Will create artist directory: " absolute-artist-path))
      (fs/mkdir absolute-artist-path))
    (when (not (fs/exists? absolute-album-path))
      (println (str "Will create album directory: " absolute-album-path))
      (fs/mkdir absolute-album-path))))


(defn fix-orphans-in-lib!
  [root-path orphan-albums-to-fix-seq]
  (doseq [rec orphan-albums-to-fix-seq]
    (let [strange-array-pair (first (key rec))
          seq-of-song-maps   (val rec)]
      (create-directories-if-not-present
        root-path
        (first strange-array-pair)
        (second strange-array-pair))
      (doseq [rec seq-of-song-maps]
        (fs/copy   (:source-path rec) (:dest-path rec))
        (fs/delete (:source-path rec))))))
  



