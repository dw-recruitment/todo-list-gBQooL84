(ns dolist.core
  (:use
   ring.util.response
   ring.middleware.resource
   ring.middleware.content-type
   ring.middleware.not-modified
   hiccup.core)
  (:require
   [compojure.core :refer :all]
   [compojure.route :as route]
   [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
   [clj-postgresql.core :as pg]
   [clojure.java.jdbc :as jdbc]
   [hiccup.core :as hic]))

;;; --- I/O ---------------------------------

(def db-do (pg/spec
            :host "localhost"
            :dbname "dolist"
            :user "postgres"
            ;; next option stops jdbc from helpfully sticking ::text
            ;; coercion on strings, which then violates SQL enum
            ;; we use to enforce that status is either :todo or :done
            :stringtype "unspecified"
            ))

;;; --- landing page helpers ----------------------------------

(defn item-delete-client [do]
  (format "(function (e) {
     var r = new XMLHttpRequest(); 
     r.open('GET', '/itemdelete?sid=%s', true);
     r.onreadystatechange = function () {
	if (r.readyState != 4 || r.status != 200) return; 
        window.location.reload(true);
     };
     r.send();
   })(event);" (:sid do)))

(defn item-status-toggle-client [do]
  (format "(function (e) {
     var r = new XMLHttpRequest(); 
     r.open('GET', '/togglestatus?sid=%s', true);
     r.onreadystatechange = function () {
	if (r.readyState != 4 || r.status != 200) return; 

        var li = document.getElementById('do-%1$s');
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
   })(event);" (:sid do)))

(def item-new-client
   "(function (e) {
     var code = (e.keyCode ? e.keyCode : e.which);
     if (code !== 13) return;

     var tasker = document.getElementById('task');
     var task = tasker.value.trim();

     if (!task.length) return;

     var r = new XMLHttpRequest(); 
     r.open('GET', '/itemnew?task='+task, true);
     r.dataType='html';
     r.onreadystatechange = function () {
	if (r.readyState != 4 || r.status != 200) return; 
        tasker.value='';
        window.location.reload(true);
     };
     r.send();
   })(event);")

;;; --- landing page -----------------------------------

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
     [:label {:for "task"} "What do we need to do?"]
     [:input#task {:onkeypress item-new-client
                   :type "text"
                   :autofocus "autofocus"
                   :value ""
                   :style "margin-left:18"}]]))

;;; --- server-side handlers -----------------------------

(def toggle-status-template
  "update item set status = case status
                              when ':todo'::item_status_type
                              then ':done'::item_status_type
                              else ':todo'::item_status_type
                            end
	where sid = %s
        returning status;") ;; can't get jdbc/execute! to work

(defroutes app-routes
  (GET "/" []
       (landing-page))

  (GET "/about" []
       (html [:center
              [:h1 "About To Do Explorer"]
              "Oh, just cutting my teeth on Clojure"]))

  (GET "/togglestatus" [& params]
       (jdbc/query db-do
                   (format toggle-status-template
                           (:sid params))))

  (GET "/itemdelete" [& params]
       (jdbc/query db-do
                   (format "delete from item where sid = %s returning sid"
                           (:sid params))))

  (GET "/itemnew" [& params]
       (jdbc/query db-do
                   (format "insert into item (task) values ('%s') returning sid"
                           (:task params))))

  (route/not-found "We do not have such a beast."))

(def dolist-handler
  (wrap-defaults app-routes site-defaults))
