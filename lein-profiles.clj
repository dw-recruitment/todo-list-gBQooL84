{:repl {:plugins [[cider/cider-nrepl "0.8.1"]
                  [lein-datomic "0.2.0"]]
        :dependencies [[org.clojure/java.jdbc "0.4.2"]
                       [java-jdbc/dsl "0.1.0"]
                       [org.postgresql/postgresql "9.2-1003-jdbc4"]
                       [clj-postgresql "0.4.0"]
                       [hiccup "1.0.5"]
                       [formative "0.8.8"]
                       [prismatic/dommy "1.1.0"]
                       ]
        :datomic {:install-location "~/dev/datomic-free-0.9.5350"}}
 :user {:plugins [[lein-datomic "0.2.0"]]
        :dependencies [[prismatic/dommy "1.1.0"]]
        :datomic {:install-location "/home/***REMOVED***/dev/datomic-free-0.9.5350"}}}
