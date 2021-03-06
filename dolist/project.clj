(defproject dolist "Do List Explorer"
  :description "Hello world Clojure Web app"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.1.1"]
                 [ring/ring-defaults "0.2.0"]
                 [ring/ring-core "1.4.0"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [org.clojure/java.jdbc "0.4.2"]
                 [clj-postgresql "0.4.0"]]
  :plugins [[lein-ring "0.8.11"]]
  :ring {:handler dolist.core/dolist-handler})
