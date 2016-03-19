
(require '[clojure.java.jdbc :as jdbc]
         '[java-jdbc.ddl :as ddl])

(def db-pg
  {:classname "org.postgresql.Driver"
   :subprotocol "postgresql"
   :subname "//localhost:5432/postgres"
   :user "postgres"
   :password "postgres"})

(jdbc/with-db-connection [c db-pg]
   (jdbc/execute! c ["create database dolist"]
                  :transaction? false))

(def db-do
  {:classname "org.postgresql.Driver"
   :subprotocol "postgresql"
   :subname "//localhost:5432/dolist"
   :user "postgres"
   :password "postgres"})

(jdbc/db-do-commands db-do
  (ddl/create-table :item6
    [:sid :bigserial "PRIMARY KEY"]
    [:task :text "NOT NULL"]
    [:doneness :text "NOT NULL DEFAULT 'todo'"]))

(jdbc/db-do-commands db-do
  (ddl/create-table :fruit
    [:name "varchar(16)" "PRIMARY KEY"]
    [:appearance "varchar(32)"]
    [:cost :int "NOT NULL"]
    [:unit "varchar(16)"]
    [:grade :real]))
