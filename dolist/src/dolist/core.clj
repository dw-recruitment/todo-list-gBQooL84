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
            [formative.parse :as fp]))


;;; --- I/O ---------------------------------

(def db-do (pg/spec
            :host "localhost"
            :dbname "dolist"
            :user "postgres"
            :stringtype "unspecified" ;; hack for enums
            ))

;;; --- landing page helpers ----------------------------------
(def todo-form
  {:fields [{:name :to-do :type :text}]
   :submit-label "Save"
   :validations [[:required [:to-do]]]
   :values {:to-do ""}
   })

(defn item-delete-client [do]
  (format "(function (e) {
     var r = new XMLHttpRequest(); 
     r.open('GET', '/itemdelete?sid=%s', true);
     r.dataType='html';
     r.onreadystatechange = function () {
        console.log('rstate='+r.readyState
                    +',status='+r.status);
	if (r.readyState != 4 || r.status != 200) return; 
        //console.log('reloading!!!!');
        window.location.reload(true);
     };
 
     console.log('deleting item sid = ' + %s);
     r.send();
   })(event);" (:sid do)(:sid do)))

(defn item-status-toggle-client [do]
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

(defn landing-page []
  (html
   [:head
    [:style "p.done {display:inline-block;text-decoration:line-through;}"]
    [:style "p.todo {display:inline-block;font-weight:bold}"]
    [:style "button.saver {width:72;margin:10pxdisplay:inline-block}"]
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
         [:button {:class "saver" :onclick (item-status-toggle-client r)}
          (if (= (:status r) ":todo") "complete" "undo")]
         [:button {:style "margin-left:12"
                   :title "Click to delete item from list altogether."
                   :onclick (item-delete-client r)} "X"]
         [:p {:class (subs (:status r) 1)
              :style "margin-left:12"}
          (:task r)]
         
         ]])]
    ]
   [:div {:style "padding-left:96"}
    (f/render-form todo-form)]))

;;; --- server-side route handlers ------------
;;;
;;; --- new item entry ------------------------

(defn item-new-server [params]
  (let [values (fp/parse-params todo-form params)]
     (jdbc/query db-do
        (str "insert into item (task) values ('"
                            (:to-do params) "') returning sid"))
    (landing-page)))

;;; --- item deletion -------------------------


(defn item-delete-server [params]
 (jdbc/query db-do (format "delete from item where sid = %s returning sid" (:sid params)))
 (html [:p "boom"]))

;;; --- status toggling ------------------------

(def toggle-status-template
  "update item set status = case status
                          when ':todo'::item_status_type
                          then ':done'::item_status_type
                          else ':todo'::item_status_type
                         end
	where sid = %s
        returning status;")

(defn item-status-toggle-server [params]
 (jdbc/query db-do (format toggle-status-template (:sid params)))
 (html [:p "ignorable"]))


;;; --- routes ------------------------------------------

(defroutes app-routes
  (GET "/" []
       (landing-page))
  (GET "/about" []
       (html [:center
              [:h1 "About To Do Explorer"]
              "Oh, just cutting my teeth on Clojure"]))
  (GET "/togglestatus" [& args]
       (item-status-toggle-server args))
  (GET "/itemdelete" [& args]
       (item-delete-server args))
  (POST "/" [& params]
        (item-new-server params))
  (route/not-found "We do not have such a beast."))


(def loghandlerzz
  (wrap-defaults app-routes site-defaults)
  #_ (-> handler
         (logger/wrap-with-logger)
         (wrap-resource "public")))
