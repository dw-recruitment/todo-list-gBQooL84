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

;;; --- status toggling ------------------------

(def toggle-status-template
  "update item set status = case status
                          when ':todo'::item_status_type
                          then ':done'::item_status_type
                          else ':todo'::item_status_type
                         end
	where sid = %s
        returning status;")

(defn toggle-todo-status [params]
 (jdbc/query db-do (format toggle-status-template (:sid params)))
 (html [:p "ignorable"]))

(defn dostatus [do]
  (format "(function (e) {
     var r = new XMLHttpRequest(); 
     r.open('GET', '/togglestatus?sid=%s', true);
     r.dataType='html';
     r.onreadystatechange = function () {
	if (r.readyState != 4 || r.status != 200) return; 
        console.log('response='+r.response);
        var li = document.getElementById('do-%s');
        var lip = li.getElementsByTagName('p')[0];

        if (lip.className == 'todo') {
           lip.className = 'done';
           li.getElementsByTagName('button')[0].innerHTML = 'Undo';
        } else {
           lip.className = 'todo';
           li.getElementsByTagName('button')[0].innerHTML = 'Complete';
        }
     };
     r.send();
   })(event);" (:sid do)(:sid do)))


(defroutes app-routes
  
  (GET "/togglestatus" [& args] (toggle-todo-status args))

  (POST "/" [& params] (submit-new-todo params))
 
  (GET "/" [] 
       (html
         [:head
          [:style "p.done {display:inline-block;text-decoration:line-through;}"]
          [:style "p.todo {display:inline-block;font-weight:bold}"]
          [:style "button {width:72;display:inline-block}"]
          [:link
           {:href "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css", :rel "stylesheet"}]
          [:link
           {:href "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap-theme.min.css", :rel "stylesheet"}]]
         [:div {:style "padding-left:96"}
          [:h1 "To Do Explorer"]
          [:ul {:id "my-todos"
                :style "list-style-type:none"}
           (for [r (jdbc/query db-do "select sid,task,status from item
                                        order by created")]
             [:li {:id (str "do-" (:sid r))}
              [:div 
               [:button {:onclick (dostatus r)}
                (if (= (:status r) ":todo") "complete" "undo")]
               [:p {:class (subs (:status r) 1)
                    :style "margin-left:12"}
                (:task r)]
               
               ]])]
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

