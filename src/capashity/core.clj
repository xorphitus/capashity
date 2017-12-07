(ns capashity.core
  (:gen-class)
  (:require [integrant.core :as ig]
            [clojure.java.jdbc :as jdbc]))

(def mysql-db {:dbtype   "mysql"
               :dbname   "test_db"
               :user     "root"
               :password "password"})

(jdbc/query mysql-db "SELECT 1")

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
