(ns org.msync.spring-boost.ring-like
  (:require [clojure.string])
  (:import [org.springframework.util MultiValueMap]
           [org.springframework.web.servlet.function ServerRequest]
           [javax.servlet.http HttpServletRequest]
           [java.util Map Collections]))

;; Reference - https://github.com/ring-clojure/ring/blob/master/SPEC

(defn to-ring-headers [^Map spring-headers]
  (persistent!
    (reduce
      (fn [acc [^String k ^String v]]
        (assoc! acc (clojure.string/lower-case k) v))
      (transient {})
      spring-headers)))

(defn to-query-string [^HttpServletRequest request]
  (when-let [^MultiValueMap spring-query-params (.getParameterMap request)]
    (clojure.string/join "&"
      (flatten
        (map
          (fn [[k vs]]
            (map #(str k "=" %) vs))
          spring-query-params)))))

(defn extract-headers [^HttpServletRequest http-request]
  (let [header-keys (.getHeaderNames http-request)
        header-keys (Collections/list header-keys)]
    (persistent!
      (reduce
        (fn [m k]
          (assoc! m k (.getHeader http-request k)))
        (transient {})
        header-keys))))

(comment

  "Ref: https://github.com/ring-clojure/ring/blob/master/SPEC"

  "Ring request map contains the following key-value pairs."

  {:server-port "Required, integer"
   :server-name "Required, string"
   :remote-addr "Required, string"
   :uri "Required, string"
   :query-string "Optional, string"
   :scheme "Required, clojure.lang.Keyword"
   :request-method "Required, clojure.lang.Keyword"
   :protocol "Required, string"
   :content-type #{:DEPRECATED "Optional, string"}
   :content-length #{:DEPRECATED "Optional, integer"}
   :character-encoding #{:DEPRECATED "Optional, string"}
   :ssl-client-cert "Optional, java.security.cert.X509Certificate"
   :headers "Required, clojure.lang.IPersistentMap, downcased string keys to value strings. Multiple
   values should be concatenated with a comma"
   :body "Optional, java.io.InputStream"}

  "Ring response map contains the following key-value pairs"

  {:status "Required, integer"
   :headers "Required, clojure.lang.IPersistentMap, string keys, and each value is either a string, or a seq of strings"
   :body "Optional, ring.core.protocols/StreamableResponseBody. By default, the types that support this
   protocol are String (verbatim), ISeq (each element is sent as a string), File and InputStream (auto-closed)"})

(let [scheme :http
      http-protocol "HTTP/1.1"]
  (defn to-ring-spec
    "Extracts various attributes, approximating the ring-spec request.
    But does not consume the body. The body being a Publisher, is better handled in Java."
    [^String uri ^ServerRequest request]
    (let [^HttpServletRequest http-request (.servletRequest request)
          server-port (.getLocalPort http-request)
          server-name (.getServerName http-request)
          remote-addr (.getRemoteHost http-request)
          uri uri
          scheme scheme
          request-method (-> (.getMethod http-request) clojure.string/lower-case keyword)
          protocol http-protocol
          spring-headers (extract-headers http-request)
          headers (to-ring-headers spring-headers)]
      (merge
        {:server-port server-port
         :server-name server-name
         :remote-addr remote-addr
         :uri uri
         :scheme scheme
         :request-method request-method
         :protocol protocol
         :headers headers}
        (when-let [query-string (to-query-string http-request)]
          ;; Inefficient because spring-framework already parses it, but we re-create an approximation of the query-string
          {:query-string query-string})))))