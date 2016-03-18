(defproject dolist "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [ring/ring-core "1.4.0"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [ring.middleware.logger "0.5.0"]
                 ]
  :plugins [[lein-ring "0.8.11"]]
  :ring {:handler dolist.core/loghandlerzz})
