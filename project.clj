(defproject com.palletops/uberimage "0.1.3-SNAPSHOT"
  :description "Leiningen plugin to create a docker image for a project uberjar"
  :url "http://github.com/palletops/lein-uberimage"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :eval-in-leiningen true
  :dependencies [[com.palletops/clj-docker "0.1.1"]
                 [org.clojure/core.async "0.1.303.0-886421-alpha"]
                 [org.clojure/tools.cli "0.3.1"]])
