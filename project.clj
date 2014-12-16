(defproject com.palletops/uberimage "0.4.2-SNAPSHOT"
  :description "Leiningen plugin to create a docker image for a project uberjar"
  :url "http://github.com/palletops/lein-uberimage"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :eval-in-leiningen true
  :min-lein-version "2.4.3"
  :dependencies [[com.palletops/clj-docker "0.2.0"]
                 [org.clojure/tools.cli "0.3.1"]
                 [net.oauth.core/oauth "20100527"]
                 [clj-time "0.8.0"]]
  :global-vars {*warn-on-reflection* true})
