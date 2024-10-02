(ns org.msync.spring-boost.application-context
  (:require [clojure.walk :refer [keywordize-keys]])
  (:import [org.springframework.core.env Environment]
           [java.util Map]
           [org.springframework.context ApplicationContext]
           [java.util.logging Logger]))

(defonce state (atom {}))
(defonce ^Logger log (Logger/getLogger (str *ns*)))

(defn -component-init [^ApplicationContext ctx]
  (.info log "Initializing the ClojureComponent")
  (swap! state assoc :ctx ctx))

(defn ^ApplicationContext get-application-context []
  (:ctx @state))

(defn ^String id []
  (.getId (get-application-context)))

(defn ^String application-name []
  (.getApplicationName (get-application-context)))

(defn ^ApplicationContext parent []
  (.getParent (get-application-context)))

(defn ^Environment environment []
  (.getEnvironment (get-application-context)))

(defn ^Map beans
  ([] (beans Object))
  ([^Class clazz]
   (keywordize-keys (.getBeansOfType (get-application-context) clazz))))

(defn ^Map beans-with-annotation [^Class annotation]
  (keywordize-keys
    (.getBeansWithAnnotation (get-application-context) annotation)))
