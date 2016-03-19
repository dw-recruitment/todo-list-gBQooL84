;;; supporting ~/.lein/profiles.clj
; {:repl {:plugins [[cider/cider-nrepl "0.8.1"]]
;;        :dependencies [[org.clojure/java.jdbc "0.4.2"]
;;                       [java-jdbc/dsl "0.1.0"]
;;                       [org.postgresql/postgresql "9.2-1003-jdbc4"]

(require '[clj-postgresql.core :as pg]
         '[clojure.java.jdbc :as jdbc])

;;; n.b. Following relies on password being defined in ~/.pgpass

(def db-pg (pg/spec
            :host "localhost"
            :dbname "postgres"
            :user "postgres"))

(jdbc/with-db-connection [c db-pg]
   (jdbc/execute! c ["create database dolist"]
                  :transaction? false))

(def db-do (pg/spec
            :host "localhost"
            :dbname "dolist"
            :user "postgres"))

(jdbc/db-do-commands db-do
  (jdbc/create-table-ddl :item
    [:sid :bigserial "PRIMARY KEY"]
    [:created :timestamptz "NOT NULL DEFAULT now()"]
    [:task :text "NOT NULL"]
    [:status :text "NOT NULL DEFAULT 'todo'"])
  "create index on item (sid)"
  "create index on item (created)")

(jdbc/insert! db-do :item [:task]
  ["register to vote"]
  ["file taxes"]
  ["call Mom"])
