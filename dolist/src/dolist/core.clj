(ns dolist.core
  (:use ring.util.response
        ring.middleware.resource
        ring.middleware.content-type
        ring.middleware.not-modified
        hiccup.core
        )
  (:require [ring.middleware.logger :as logger]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [clj-postgresql.core :as pg]
            [clojure.java.jdbc :as jdbc]
            [hiccup.core :as hic]))

(def db-do (pg/spec
            :host "localhost"
            :dbname "dolist"
            :user "postgres"
            :stringtype "unspecified" ;; hack for enums
            ))

(defroutes app-routes
  (GET "/" [] (html [:div
                     [:h1 "To Do Or Not To Do"]
                     [:ul
                      (for [r (jdbc/query db-do "select task,status from item")]
                       [:li (str (:task r) ": " (subs (:status r) 1))])]]))

  (GET "/about" []
       (html [:center
              [:h1 "About To Do Or Not To Do"]
              "Oh, just cutting my teeth on Clojure"]))
  (route/not-found "Endpoint Not Found"))

(def loghandlerzz
   (wrap-defaults app-routes site-defaults)
  #_ (-> handler
     (logger/wrap-with-logger)
     (wrap-resource "public")
     ))
