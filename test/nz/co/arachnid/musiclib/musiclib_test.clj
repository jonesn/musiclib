(ns nz.co.arachnid.musiclib.musiclib-test
  (:require [clojure.test :refer :all]
            [nz.co.arachnid.musiclib.core :refer :all]
            [nz.co.arachnid.musiclib.search :refer :all]
            [nz.co.arachnid.musiclib.domain :refer :all]
            [me.raynes.fs :as fs]))

;; ==========================
;;    File Extension Tests
;; ==========================

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

(deftest test-upper-extension
  (testing "Extension Generation"
    (is (= ".MP3" (upper-extension "12 Back to Bare Bones.mp3")))
    (is (= nil    (upper-extension                         nil)))))

(deftest test-filter
  (testing "Orphaned extensions are correctly recognized."
    (let [results (filter-orphan-extensions files-with-extensions)]
      (is (= 8 (count results))))))

(deftest test-define-format
  (testing "All extensions will be covered."
    (is (= nil               (define-format nil)))
    (is (= :not-supported    (define-format "abcd")))
    (is (= :not-supported    (define-format "abcd.efg")))
    (is (= :not-supported    (define-format "abcd.e")))
    (is (= :mp3              (define-format "abcd.mp3")))
    (is (= :m3u              (define-format "boo.m3u")))
    (is (= :m3u              (define-format "Programming.m3u")))
    (is (= :flac             (define-format "Sigur Rós - Popplagið (Heima umdesch4 headphone mix).flac")))
    (is (= :m3u              (define-format (first #{"Programming.m3u" "Sigur Rós - Popplagið (Heima umdesch4 headphone mix).flac"}))))
    (is (= :m3u              (define-format (first (filter-orphan-extensions ["Programming.m3u" "Sigur Rós - Popplagið (Heima umdesch4 headphone mix).flac"])))))
    (is (= :flac             (define-format "abcd.flac")))))

;; ==========================
;;       Library Tests
;; ==========================

(def lib-a
  [(create-artist-song-set
    {:artist "Mastodon"
     :album "Blood Mountain"
     :root-file "/home/jonesn/Music/Mastodon/Blood Mountain"
     :song-set
     #{"04 - Capillarian Crest.mp3"
       "07 - Colony Of Birchmen.mp3"
       "06 - Bladecatcher.mp3"
       "03 - Sleeping Giant.mp3"
       "08 - Hunters Of The Sky.mp3"
       "01 - The Wolf Is Loose.mp3"
       "09 - Hand Of Stone.mp3"
       "05 - Circle Of Cysquatch.mp3"
       "10 - This Mortal Soil.mp3"
       "11 - Siberian Divide.mp3"
       "12 - Pendulous Skin.mp3"
       "02 - Crystal Skull.mp3"}
     :format :mp3})
   (create-artist-song-set
    {:artist "Nirvana"
     :album "In Utero"
     :root-file "/home/jonesn/Music/Nirvana/In Utero"
     :song-set
     #{"03 - Nirvana - Heart-Shaped Box.flac"
       "07 - Nirvana - Very Ape.flac"
       "04 - Nirvana - Rape Me.flac"
       "06 - Nirvana - Dumb.flac"
       "13 - Nirvana - Gallons Of Rubing Alcohol Flow.flac"
       "01 - Nirvana - Serve The Servants.flac"
       "09 - Nirvana - Pennyroyal Tea.flac"
       "08 - Nirvana - Milk It.flac"
       "11 - Nirvana - Tourette's.flac"
       "12 - Nirvana - All Apologies.flac"
       "10 - Nirvana - Radio Friendly Unit Shifter.flac"
       "02 - Nirvana - Scentless Apprentice.flac"
       "05 - Nirvana - Frances Farmer Will Have Her Revenge On Seattle.flac"}
     :format :flac})])

(def lib-b
  [{:artist "Nirvana",
    :album "In Utero",
    :root-file "/home/jonesn/Music/Nirvana/In Utero",
    :song-set
    #{"03 - Nirvana - Heart-Shaped Box.flac"
      "07 - Nirvana - Very Ape.flac"
      "04 - Nirvana - Rape Me.flac"
      "06 - Nirvana - Dumb.flac"
      "13 - Nirvana - Gallons Of Rubing Alcohol Flow.flac"
      "01 - Nirvana - Serve The Servants.flac"
      "09 - Nirvana - Pennyroyal Tea.flac"
      "08 - Nirvana - Milk It.flac"
      "11 - Nirvana - Tourette's.flac"
      "12 - Nirvana - All Apologies.flac"
      "10 - Nirvana - Radio Friendly Unit Shifter.flac"
      "02 - Nirvana - Scentless Apprentice.flac"
      "05 - Nirvana - Frances Farmer Will Have Her Revenge On Seattle.flac"},
    :format :flac}
   {:artist "Nirvana",
    :album "Nevermind",
    :root-file "/home/jonesn/Music/Nirvana/Nevermind",
    :song-set
    #{"07 - Nirvana - Territorial Pissings.flac"
      "05 - Nirvana - Lithium.flac"
      "08 - Nirvana - Drain You.flac"
      "04 - Nirvana - Breed.flac"
      "11 - Nirvana - On A Plain.flac"
      "03 - Nirvana - Come As You Are.flac"
      "02 - Nirvana - In Bloom.flac"
      "09 - Nirvana - Lounge Act.flac"
      "10 - Nirvana - Stay Away.flac"
      "01 - Nirvana - Smells Like Teen Spirit.flac"
      "12 - Nirvana - Something In The Way - [Hidden Track].flac"
      "06 - Nirvana - Polly.flac"},
    :format :flac}])

(deftest test-generate-library-stats
  (testing "Empty Lib checks"
    (is (= nil                                   (generate-library-stats nil)))
    (is (= (create-lib-stats {:artist-count 0
                              :album-count  0
                              :mp3-count    0
                              :flac-count   0
                              :orphan-count 0})  (generate-library-stats []))))
  (testing "Standard calls"
    (is (= (create-lib-stats {:artist-count 2
                              :album-count  2
                              :mp3-count    12
                              :flac-count   13
                              :orphan-count 0})  (generate-library-stats lib-a)))))

