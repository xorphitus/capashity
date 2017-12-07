(ns capashity.core
  (:gen-class)
  (:require [integrant.core :as ig]
            [clojure.java.jdbc :as jdbc]))

(def mysql-db {:dbtype   "mysql"
               :dbname   "test_db"
               :user     "root"
               :password "password"})

(defn get-tables []
  (map #(val (first %)) (jdbc/query mysql-db "SHOW TABLES")))

(defn count-rows [table]
  (->
   (jdbc/query mysql-db (str "SELECT count(*) FROM " table))
   first
   first
   val))

(defn count-for-tables []
  (->>
    (get-tables)
    (map (fn[table] {:name table
                     :count (count-rows table)}))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
