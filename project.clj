(def version "0.1.0-SNAPSHOT")
(def core-version "1.11.1")
(def spring-version "5.3.23")
(def spring-boot-version "2.7.4")

(defproject org.msync/spring-boost-classic version

  :description "spring-boost, but for the classic mvc spring-boot"
  :url "https://github.com/jaju/spring-boost-classic"

  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}

  :scm {:name "git" :url "https://github.com/jaju/spring-boost-classic"}

  :pom-addition [:developers [:developer [:name "Ravindra R. Jaju"]]]

  :profiles {:provided
             {:dependencies
              [[org.springframework.boot/spring-boot ~spring-boot-version]
               [org.springframework/spring-context ~spring-version]
               [org.springframework/spring-webmvc ~spring-version]
               [javax.servlet/javax.servlet-api "4.0.1"]
               ]}

             :dev
             {:dependencies
              [[org.springframework.boot/spring-boot-starter-web ~spring-boot-version]]}}

  :plugins [[org.msync/lein-javadoc "0.4.0-SNAPSHOT"]]

  :source-paths ["src" "src-java"]

  :java-source-paths ["src-java"]

  :javac-options ["-source" "11" "-target" "11"]

  :dependencies [[org.clojure/clojure ~core-version]
                 [nrepl/nrepl "1.0.0"]]

  :javadoc-opts {:package-names   ["org.msync.spring_boost"]
                 :additional-args ["-windowtitle" "Spring Boost Javadoc"
                                   "-quiet"
                                   "-link" "https://docs.oracle.com/en/java/javase/11/docs/api/"
                                   "-link" ~(str "https://www.javadoc.io/static/org.clojure/clojure/" core-version)
                                   "-link" ~(str "https://javadoc.io/doc/org.springframework/spring-beans/" spring-version)
                                   "-link" ~(str "https://javadoc.io/doc/org.springframework/spring-web/" spring-version)]}

  :classifiers {:sources {:prep-tasks ^:replace []}
                :javadoc {:prep-tasks  ^:replace ["javadoc"]
                          :omit-source true
                          :filespecs   ^:replace [{:type :path, :path "javadoc"}]}}

  :repl-options {:init-ns org.msync.spring-boost}

  :aot [org.msync.spring-boost.application-context]

  :jar-inclusions [#"spring-boost-classic-*-sources.jar"]

  :deploy-repositories {
                        "releases"  :clojars
                        "snapshots" :clojars
                        }
  )
