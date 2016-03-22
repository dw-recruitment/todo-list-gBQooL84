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
            [hiccup.core :as hic]
            [formative.core :as f]
            [formative.parse :as fp]
            ))

(def db-do (pg/spec
            :host "localhost"
            :dbname "dolist"
            :user "postgres"
            :stringtype "unspecified" ;; hack for enums
            ))

(def todo-form
  {:fields [{:name :new-todo :type :text}]
   :submit-label "Save"
   :validations [[:required [:new-todo]]]
   :values {:new-todo (str "test " (rand-int 32767))}})

(defn submit-new-todo [params]
  (let [values (fp/parse-params todo-form params)]
     (jdbc/query db-do
        (str "insert into item (task) values ('"
                            (:new-todo params) "') returning sid"))
    (html
     [:h1 "Just do it!"]
     [:pre (prn-str values)]
     [:p [:a {:href "/"} "Do more!"]])))

(defroutes app-routes
  (POST "/" [& params] (submit-new-todo params))
  (GET "/" [] 
       (html
         [:head
          [:link
           {:crossorigin "anonymous",
            :integrity
            "sha384-1q8mTJOASx8j1Au+a5WDVnPi2lkFfwwEAa8hDDdjZlpLegxhjVME1fgjWPGmkzs7",
            :href
            "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css",
            :rel "stylesheet"}]
          "<!-- Optional theme -->"
          [:link
           {:crossorigin "anonymous",
            :integrity
            "sha384-fLW2N01lMqjakBkx3l/M9EahuwpSfeNvV63J5ezn3uZzapT0u7EYsXMjQV+0En5r",
            :href
            "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap-theme.min.css",
            :rel "stylesheet"}]]
         [:div
          [:h1 "To Do"]
          [:ul
           (for [r (jdbc/query db-do "select task,status from item
                                        order by created")]
             [:li (str (:task r) ": " (subs (:status r) 1))])]
          ]
         (f/render-form todo-form)))

  (GET "/about" []
       (html [:center
              [:h1 "About To Do Or Not To Do"]
              "Oh, just cutting my teeth on Clojure"]))
  (route/not-found "We do not recorgnize that address."))

(def loghandlerzz
   (wrap-defaults app-routes site-defaults)
  #_ (-> handler
     (logger/wrap-with-logger)
     (wrap-resource "public")
     ))

