(ns capashity.core
  (:gen-class)
  (:require [clojure.java.jdbc :as jdbc]
            [cheshire.core :refer :all]
            [clj-http.client :as client]
            [integrant.core :as ig]))

(def config
  {:setting/db "database.edn"
   :setting/events "events.edn"
   :result/histories (atom [])})

(defmethod ig/init-key :setting/db [_ path]
  (ig/read-string (slurp path)))

(defmethod ig/init-key :setting/events [_ path]
  (ig/read-string (slurp path)))

(defmethod ig/init-key :result/histories [_ conf]
  "currently, there's nothing to do"
  conf)

(defmethod ig/halt-key! :result/histories [_ histories]
  (reset! histories []))

(def system
  (ig/init config))

(defn get-tables []
  (map #(val (first %)) (jdbc/query (:setting/db system) "SHOW TABLES")))

(defn count-rows [table]
  (->
   (jdbc/query (:setting/db system) (str "SELECT count(*) FROM " table))
   first
   first
   val))

(defn count-for-tables []
  (->> (get-tables)
       (map (fn[table] {:name table
                        :count (count-rows table)}))))

(defn measure [event-name]
  (swap! (:result/histories system) #(conj % {:event event-name
                                              :counts (count-for-tables)})))

(defn sub-counts [next prev]
  (->> (concat prev next)
       (group-by :name)
       vals
       (map (fn[x] {:name (:name (first x))
                    :count (- (:count (second x))
                              (:count (first x)))}))))

(defn sum-up [data]
  (->> data
       (partition 2 1)
       (map (fn[datum]
              (let [prev (first datum)
                    next (second datum)
                    event (:event next)]
                {:event event
                 :counts (sub-counts (:counts next) (:counts prev))})))))

(defn fire [event]
  (let [verb (condp = (:method event)
               :get client/get
               :post client/post)]
    (verb (:url event) (:body (:param event)))))

(defn -main [& args]
  (do
    (measure "initial state")
    (doseq [event (:setting/events system)]
      (do (fire event)
          (measure (:url event))))
    (let [histories (:result/histories system)]
      (println (sum-up @histories)))))
