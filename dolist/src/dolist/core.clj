(ns dolist.core
  (:use ring.util.response
        ring.middleware.resource
        ring.middleware.content-type
        ring.middleware.not-modified)
  (:require [ring.middleware.logger :as logger]))

(def x 42)

(defn handler [request]
  (-> (response "<h1>Ford to City: Drop Dead</h1>
          <img src='uconst.gif' alt='Under Construction'</img>")
      (content-type "text/html")))

(def loghandlerzz
 (-> handler
     (logger/wrap-with-logger
          :debug (fn [x] (print x)))
     (wrap-resource "public")
     ))
