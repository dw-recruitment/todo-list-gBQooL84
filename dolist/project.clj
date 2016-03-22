(defproject dolist "0.8 Do List Rizing"
  :description "Hello world Clojure Web app"
  :url "http://example.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.1.1"]
                 [ring/ring-defaults "0.2.0"]
                 [ring/ring-core "1.4.0"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [ring.middleware.logger "0.5.0"]
                 [org.clojure/java.jdbc "0.4.2"]
                 [clj-postgresql "0.4.0"]
                 [prismatic/dommy "1.1.0"]
                 [formative "0.8.8"]]
  :plugins [[lein-ring "0.8.11"]]
  :ring {:handler dolist.core/loghandlerzz})
