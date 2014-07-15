(ns leiningen.uberimage
  (:require
   [clojure.core.async :refer [thread]]
   [clojure.java.io :refer [copy file]]
   [com.palletops.docker :refer [build image-create]]
   [com.palletops.docker.utils
    :refer [tar-output-stream tar-entry-from-file tar-entry-from-string]]
   [leiningen.core.main :as main]
   [leiningen.jar :refer [get-jar-filename]]
   [leiningen.uberjar :refer [uberjar]]
   [taoensso.timbre :as timbre :refer [merge-config! str-println]]))

(def defaults
  {:base-image "pallet/java"
   :endpoint "http://localhost:4243"
   ;; "http://localhost:2375"
   })

(defn dockerfile
  "Return a dockerfile string"
  [{:keys [base-image]}]
  (format "FROM %s
ADD uberjar.jar uberjar.jar
CMD [\"/usr/bin/java\", \"-jar\", \"uberjar.jar\"]"
          base-image))

(defn buildtar
  "Return an InputStream that will deliver a tar archive with a Dockerfile
  and the uberjar."
  [tar-output-stream piped-output-stream standalone-filename options]
  (with-open [piped-output-stream piped-output-stream
              tar-output-stream tar-output-stream]
    (tar-entry-from-string
     tar-output-stream "Dockerfile" (dockerfile options))
    (tar-entry-from-file
     tar-output-stream "uberjar.jar" (file standalone-filename))))

(def info-timbre-config
  "A basic timbre configuration for use with info level logging."
  {:appenders
   {:standard-out
    {:doc "Prints to *out*/*err*. Enabled by default."
     :min-level :info :enabled? true :async? false :rate-limit nil
     :fn (fn [{:keys [error? output]}] ; Can use any appender args
           (binding [*out* (if error? *err* *out*)]
             (str-println output)))}}})

(defn configure-logging
  "Configure logging"
  []
  (if-not (System/getenv "DEBUG")
    (merge-config! info-timbre-config)))

(defn uberimage
  "Generate a docker image to run an uberjar.'"
  [project & args]
  (let [options defaults]
    (configure-logging)
    (try
      (uberjar project)
      (catch Exception e
        (when main/*debug*
          (.printStackTrace e))
        (throw (ex-info "Uberimage aborting because uberjar failed:" {} e))))
    (let [{:keys [piped-input-stream piped-output-stream tar-output-stream]}
          (tar-output-stream)
          jarfile (get-jar-filename project :standalone)]
      (main/info "Using jar file" jarfile)
      (when-not (.exists (file jarfile))
        (throw (ex-info "Jar file does not exist" {:exit-code 1})))
      (thread
        (buildtar
         tar-output-stream
         piped-output-stream
         jarfile
         options))
      (let [resp (try
                   (build {:url (:endpoint options)} {:body piped-input-stream})
                   (catch java.net.ConnectException e
                     (throw
                      (ex-info
                       (str "Error in docker build using " (:endpoint options))
                       {} e))))
            s (-> resp :body last :stream)
            id (if s
                 (second (re-find #"Successfully built ([0-9a-f]+)" s)))]
        (println "Built image" id)))))
