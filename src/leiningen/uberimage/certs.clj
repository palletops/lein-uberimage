(ns leiningen.uberimage.certs
  "Cert handling"
  (:require
   [clojure.data.codec.base64 :refer [decode]]
   [clojure.java.io :as io]
   [clojure.string :as string])
  (:import
   java.io.File
   [java.security AlgorithmParameters Key KeyFactory KeyStore]
   [java.security.cert Certificate CertificateFactory X509Certificate]
   [java.security.spec ECPrivateKeySpec X509EncodedKeySpec PKCS8EncodedKeySpec]
   [javax.crypto EncryptedPrivateKeyInfo]
   [net.oauth.signature.pem PKCS1EncodedKeySpec]))

(defn ^Key load-key [^File key-file]
  (let [der (with-open [s (java.io.BufferedInputStream.
                           (io/input-stream key-file))]
              (let [chars (slurp s)
                    b64chars (->> (string/split-lines chars)
                                  (remove #(re-matches #"---.*---" %))
                                  string/join)]
                (decode (.getBytes b64chars))))
        key-spec (PKCS1EncodedKeySpec. der)
        key-factory (KeyFactory/getInstance "RSA")
        key (.generatePrivate key-factory (.getKeySpec key-spec))]
    key))

(defn ^X509Certificate load-cert [^CertificateFactory cert-factory ^File path]
  (with-open [s (java.io.BufferedInputStream. (io/input-stream path))]
    (.generateCertificate cert-factory s)))

(defn ^String key-store
  [cert-path]
  (let [ca-f (io/file cert-path "ca.pem")]
    (if (.exists ca-f)
      (let [ks-path (io/file cert-path "client.ks")
            ks (KeyStore/getInstance "JKS")
            ts (KeyStore/getInstance "JKS")
            pw (.toCharArray "")]
        (if (and (.exists ks-path) (pos? (.length ks-path)))
          (str ks-path)
          (let [cert-factory (CertificateFactory/getInstance "X.509")
                ca-cert (load-cert cert-factory ca-f)
                server-cert (load-cert cert-factory
                                       (io/file cert-path "cert.pem"))
                key (load-key (io/file cert-path "key.pem"))]
            (doto ks
              (.load nil pw)
              (.setEntry
               "ca"
               (java.security.KeyStore$TrustedCertificateEntry. ca-cert)
               nil)
              (.setEntry
               "server"
               (java.security.KeyStore$TrustedCertificateEntry. server-cert)
               nil)
              (.setKeyEntry
               "client" key pw
               (into-array X509Certificate [server-cert ca-cert])))
            (with-open [os (io/output-stream ks-path)]
              (.store ks os pw))
            (str ks-path))))
      (println "No such file: " (str ca-f)))))
