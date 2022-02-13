(ns org.msync.spring-boost.ring-like
  (:require [clojure.string])
  (:import [org.springframework.http.server ServerHttpRequest]
           [org.springframework.util MultiValueMap]
           (org.springframework.web.servlet.function ServerRequest)
           (javax.servlet.http HttpServletRequest)
           (java.util List Map Collections)))

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
          request-method (keyword (clojure.string/lower-case (.getMethod http-request)))
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