(ns leiningen.uberimage
  (:require
   [clojure.core.async :refer [thread]]
   [clojure.java.io :refer [copy file]]
   [clojure.stacktrace :refer [root-cause]]
   [clojure.string :as string]
   [clojure.tools.cli :refer [parse-opts]]
   [com.palletops.docker :refer [build image-create]]
   [com.palletops.docker.utils
    :refer [tar-output-stream tar-entry-from-file tar-entry-from-string]]
   [leiningen.core.main :as main]
   [leiningen.jar :refer [get-jar-filename]]
   [leiningen.uberimage.certs :refer [key-store]]
   [leiningen.uberjar :refer [uberjar]]
   [taoensso.timbre :as timbre :refer [merge-config! str-println]]))

(defn dockerfile
  "Return a dockerfile string"
  [{:keys [cmd files base-image instructions]}]
  (let [cmd (or cmd ["/usr/bin/java" "-jar" "/uberjar.jar"])
        cmd (if (sequential? cmd) cmd [cmd])
        cmd-str (->> (for [s cmd] (str "\"" s "\""))
                     (string/join ","))]
    (->> (concat [(str "FROM " base-image)]
                 instructions
                 ["ADD uberjar.jar uberjar.jar"
                  (str "CMD [" cmd-str "]")]
                 (for [[tar-path local-path] files]
                   (str "ADD " tar-path " " tar-path)))
         (filter identity)
         (string/join "\n"))))

(defn buildtar
  "Return an InputStream that will deliver a tar archive with a Dockerfile
  and the uberjar."
  [^java.io.OutputStream tar-output-stream
   ^java.io.OutputStream piped-output-stream
   standalone-filename
   {:keys [files] :as options}]
  (with-open [piped-output-stream piped-output-stream
              tar-output-stream tar-output-stream]
    (tar-entry-from-string
     tar-output-stream "Dockerfile" (dockerfile options))
    (tar-entry-from-file
     tar-output-stream "uberjar.jar" (file standalone-filename))
    (doseq [[tar-path local-path] files]
      (tar-entry-from-file tar-output-stream tar-path (file local-path)))))

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


(defn endpoint-from-boot2docker
  "Return an endpoint from boot2docker env vars"
  []
  (if-let [v (System/getenv "DOCKER_HOST")]
    (let [tls-verify (System/getenv "DOCKER_TLS_VERIFY")
          tls (System/getenv "DOCKER_TLS")]
      (if (.startsWith v "tcp:")
        (let [ssl (or tls-verify (not= tls "no"))]
          {:endpoint (str (if ssl "https" "http") (subs v 3))
           :ssl ssl
           :verify (not= tls-verify "0")
           :cert-path (System/getenv "DOCKER_CERT_PATH")})
        (main/warn "Ignoring DOCKER_HOST: unsupported protocol")))))



(def cli-options
  (let [b2d-endpoint (endpoint-from-boot2docker)]
    ;; An option with a required argument
    [["-H" "--endpoint ENDPOINT" "Endpoint for docker TCP port"
      :default (or (System/getenv "DOCKER_ENDPOINT")
                   (if b2d-endpoint (:endpoint b2d-endpoint))
                   "http://localhost:2375")
      :validate [#(java.net.URL. %) "Must be a URL"]]
     ["-b" "--base-image BASE-IMAGE" "Base image to use for the image"
      :validate [string? "Must be a string"]]
     ["-t" "--tag TAG"
      (str "Repository name (and optionally a tag) to be applied to the "
           "resulting image in case of success")
      :validate [string? "Must be a string"]]
     ["-T" "--tlsverify TLSVERIFY"
      "Use TLS and verify the remote"
      :default (:verify b2d-endpoint)]
     ["-C" "--cert-path CERT-PATH"
      "Patch to client certs"
      :default (:cert-path b2d-endpoint)]]))

(defn help
  []
  (str
   "Generate a docker image to run an uberjar."
   \newline \newline
   (:summary (parse-opts [] cli-options))
   \newline \newline
   "The docker endpoint defaults to the DOCKER_HOST environment variable."
   \newline
   "If you use DOCKER_HOST for something else and wish to override its"
   \newline
   "value, use the DOCKER_ENDPOINT environment variable."))

(defn filter-api-params
  "filter API parameters, retaining only those with non-nil values"
  [params]
  (->> params
       (filter (fn [[k v]] v))
       (into {})))

(defn ^{:doc (help)} uberimage
  [project & args]
  (let [{:keys [options arguments summary errors]} (parse-opts args cli-options)
        {:keys [base-image endpoint]} options
        options (merge {:base-image "pallet/java"}
                       (:uberimage project)
                       options)]
    (when errors
      (throw (ex-info
              (str "Invalid arguments: " (string/join " " errors))
              {:cli-options cli-options
               :args args
               :exit-code 1})))
    (main/debug "options" options)
    (configure-logging)
    (let [{:keys [piped-input-stream piped-output-stream tar-output-stream]}
          (tar-output-stream)
          jarfile (try
                    (uberjar project)
                    (catch Exception e
                      (when main/*debug*
                        (.printStackTrace e))
                      (throw
                       (ex-info "Uberimage aborting because uberjar failed:"
                                {} e))))
          keystore (if-let [cert-path (:cert-path options)]
                     (key-store cert-path))]
      (main/info "Using jar file" jarfile)
      (when (or (nil? jarfile) (not (.exists (file jarfile))))
        (throw (ex-info "Jar file does not exist" {:exit-code 1})))
      (thread
        (buildtar
         tar-output-stream
         piped-output-stream
         jarfile
         options))
      (let [ks-pw "" ; (into-array java.lang.Character/TYPE "")
            resp (try
                   (build
                    {:url (:endpoint options)
                     :insecure? (= "0" (:tlsverify options))
                     :keystore keystore
                     :keystore-pass ks-pw
                     :trust-store keystore
                     :trust-store-pass ks-pw}
                    (filter-api-params {:body piped-input-stream
                                        :t (:tag options)}))
                   (catch java.net.ConnectException e
                     (throw
                      (ex-info
                       (str "Error in docker build using "
                            (:endpoint options) ".  "
                            (.getMessage ^Throwable (root-cause e)))
                       {:exit-code 1}
                       e))))
            s (-> resp :body last :stream)
            id (if s
                 (second (re-find #"Successfully built ([0-9a-f]+)" s)))
            tag (if-let [tag (:tag options)]
                  (str "[" tag "]")
                  "")]
        (println "Built image" id tag)))))
