(ns nz.co.arachnid.musiclib.musiclib-test
  (:require [clojure.test :refer :all]
            [nz.co.arachnid.musiclib.core :refer :all]
            [nz.co.arachnid.musiclib.search :refer :all]
            [me.raynes.fs :as fs]))

(def files-with-extensions
  #{"10 Keeping Warm.ABC"
    ""
    nil
    "07 Quiet Little Voices.MP3"
    "04 Conductor.r2d2"
    "03 Roll up your Sleeves.mp3"
    "09 Short Bursts.mp3"
    "01 It's Thunder and it's Lightning.mp3"
    "05 A Half Built House.flac"
    "13 The Fan.boo"
    "06 This is my House, this is my Home.mp3"
    "12 Back to Bare Bones.mp3"
    "11 An Almighty Thud.txt"
    "02 Ships with Holes will Sink.mp3"})

(deftest test-extract-artist-name
  (testing "Extraction of album name based on path."
    (is
      (= "Windhand" (extract-artist (fs/file "C:\\Users\\Nick Jones\\Music\\Windhand\\Soma")))
      (= nil        (extract-artist (fs/file nil))))))

(deftest test-upper-extension
  (testing "Extension Generation"
    (is (= ".MP3" (upper-extension "12 Back to Bare Bones.mp3"))
        (= nil    (upper-extension                         nil)))))

(deftest test-filter
  (testing "Orphaned extensions are correctly recognized."
    (let [results (filter-orphan-extensions files-with-extensions)]
      (is (= 8 (count results))))))


