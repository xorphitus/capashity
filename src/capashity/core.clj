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

(def row-counts-histories (atom []))

(defn measure [event-name]
  (swap! row-counts-histories #(conj % {:event event-name
                                        :counts (count-for-tables)})))

(defn sub-counts [a b]
  ;; TODO subtract counts
  b)

(defn sum-up [data]
  (->> data
       (partition 2 1)
       (map (fn[datum]
              (let [prev (first datum)
                    next (last datum)
                    event (:event next)]
                {:event event
                 :counts (sub-counts (:counts prev) (:counts prev))})))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
