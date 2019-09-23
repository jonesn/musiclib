(defproject nz.co.arachnid.musiclib/musiclib "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure     "1.10.1"]
                 [clj-commons/fs           "1.5.0"]
                 [org.clojure/tools.cli    "0.4.2"]
                 [com.mpatric/mp3agic      "0.9.1"]
                 [clojure-term-colors      "0.1.0"]
                 [org.clojure/core.async "0.4.490"]]
  :main ^:skip-aot nz.co.arachnid.musiclib.core
  :target-path "target/%s"

  :resource-paths ["resources/REBL-0.9.220.jar"]

  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [[org.clojure/tools.namespace "0.2.10"]
                                  [org.clojure/java.classpath   "0.3.0"]
                                  [walmartlabs/datascope        "0.1.1"]
                                  [cljfmt                       "0.6.4"]
                                  [org.openjfx/javafx-fxml      "12.0.1"]
                                  [org.openjfx/javafx-controls  "12.0.1"]
                                  [org.openjfx/javafx-swing     "12.0.1"]
                                  [org.openjfx/javafx-base      "12.0.1"]
                                  [org.openjfx/javafx-web       "12.0.1"]]
                   :plugins      [[lein-ancient "0.6.15"]
                                  [lein-cljfmt   "0.6.4"]]}
             :uberjar {:aot :all}})
