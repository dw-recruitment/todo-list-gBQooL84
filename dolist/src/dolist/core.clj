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
            ;; [dommy.core :refer-macros [sel sel1] :as dom]
            [dommy.core :as dom]
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
     [:h1 (str "Just do "  (:new-todo params) "!")]
     [:br][:br]
     [:p [:a {:href "/"} "Do more!"]])))

(def delpost
  "(function (e) {
     var r = new XMLHttpRequest(); 
     r.open('GET', '/delpost?a=1&b=2', true);
     r.onreadystatechange = function () {
	if (r.readyState != 4 || r.status != 200) return; 
	console.log('evt=' + e
                    + ',r=' + r.responseText);
     };
     console.log('sending '+r);
     r.send();
   })(event);")


(defroutes app-routes
  
  (GET "/delpost" [& args]
       (println (str (rest args) "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"))
       (html [:center
              [:h1 "Delpost!"]
              [:p (str "args=" args)]]))

  (POST "/" [& params] (submit-new-todo params))
 
  (GET "/" [] 
       (html
         [:head
          [:link
           {:href "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css", :rel "stylesheet"}]
          [:link
           {:href "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap-theme.min.css", :rel "stylesheet"}]]
         [:div
          [:h1 "To Do Navigator 1"]
          [:ul {:id "my-todos"}
           (for [r (jdbc/query db-do "select task,status from item
                                        order by created")]
             [:li [:div 
                   [:p {:style (str "display:inline-block"
                                        (when (not= (:status r) ":todo") 
                                          ";text-decoration:line-through"))}
                    (:task r)]
                   [:button {:style "display:inline-block;margin-left:9"
                             :onclick delpost}
                    (if (= (:status r) ":todo") "complete" "undo")]]])]
          ]
         (f/render-form todo-form)))
  
  (GET "/about" []
       (html [:center
              [:h1 "About To Do Or Not To Do"]
              "Oh, just cutting my teeth on Clojure"]))
  (route/not-found "We do not have such a beast."))

(def loghandlerzz
   (wrap-defaults app-routes site-defaults)
  #_ (-> handler
     (logger/wrap-with-logger)
     (wrap-resource "public")
     ))

