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
            :user "postgres"
            :stringtype "unspecified" ;; hack for enums
            ))

(jdbc/db-do-commands db-do
  "drop table if exists item"
  "drop type if exists item_status_type"
  "create type item_status_type as enum (':todo', ':done')"
  (jdbc/create-table-ddl :item
    [:sid :bigserial "PRIMARY KEY"]
    [:created :timestamptz "NOT NULL DEFAULT now()"]
    [:task :text "NOT NULL"]
    [:status "item_status_type" "NOT NULL DEFAULT ':todo'"]
    )
  "create index on item (sid)"
  "create index on item (created)"
  )


(do
  (jdbc/db-do-commands db-do
                       "truncate item")

  (jdbc/insert! db-do :item [:task :status]
                ["Take Clojure off resume" ":todo"]
                ["Register to vote" ":done"]
                ["Wash car" ":todo"]
                ["Puff up resume" ":done"]
                ["Wash dishes" ":done"]
                ["Finish PhD thesis" ":todo"]
                ["Buy new microwave" ":todo"]
                ["File taxes" ":todo"]
                ["Call Mom" ":done"]
                ["Replace muffler" ":todo"]))
