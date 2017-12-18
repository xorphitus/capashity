(ns capashity.core
  (:gen-class)
  (:require [capashity.report :refer [publish-html]]
            [clojure.java.jdbc :as jdbc]
            [cheshire.core :refer [generate-string]]
            [clj-http.client :as client]
            [integrant.core :as ig]
            [taoensso.timbre :as timbre]))

(def config
  {:setting/dbs "databases.edn"
   :setting/events "events.edn"
   :result/histories (atom [])})

;; TODO: validate file contents
(defmethod ig/init-key :setting/dbs [_ path]
  (ig/read-string (slurp path)))

;; TODO: validate file contents
(defmethod ig/init-key :setting/events [_ path]
  (ig/read-string (slurp path)))

(defmethod ig/init-key :result/histories [_ conf]
  "currently, there's nothing to do"
  conf)

(defmethod ig/halt-key! :result/histories [_ histories]
  (reset! histories []))

(def system
  (ig/init config))

(ig/halt! system)
(-main)

;; TODO: memoize
(defn get-tables [db]
  (map #(val (first %)) (jdbc/query db "SHOW TABLES")))

;; TODO: concatinate count queries for performance
;;   SELECT
;;     (SELECT count(*) FROM foo) AS foo,
;;     (SELECT count(*) FROM bar) AS bar,
(defn count-rows [db table]
  (->
   (jdbc/query db (str "SELECT count(*) FROM " table))
   first
   first
   val))

(defn count-for-tables [db]
  (->> (get-tables db)
       (map (fn[table] {:name (format "%s.%s" (:dbname db) table)
                        :count (count-rows db table)}))))

(defn measure [event-name]
  (let [all-counts (doall (mapcat count-for-tables (:setting/dbs system)))]
    (timbre/debug "counts measured")
    (swap! (:result/histories system) #(conj % {:event event-name
                                                :counts all-counts}))))

(defn sub-counts [next prev]
  (->> (concat prev next)
       (group-by :name)
       vals
       (map (fn[x] {:name (:name (first x))
                    :count (- (:count (second x))
                              (:count (first x)))}))))

;; TODO: trim "initial state" data
(defn sum-up [data]
  (->> data
       (partition 2 1)
       (map (fn[datum]
              (let [prev (first datum)
                    next (second datum)
                    event (:event next)]
                {:event event
                 :counts (sub-counts (:counts next) (:counts prev))})))))

;; TODO: print event name when an http call is failed
(defn fire [event]
  (do
    (timbre/debug "event fired" (:url event))
    (client/request
     {:method (:method event)
      :headers (:headers event)
      :url (:url event)
      :body (generate-string (:param event))})))

(defn lablize-event [ev]
  (timbre/debug {:method ev})
  (format "%s %s"
          (.toUpperCase (name (:method ev)))
          (:url ev)))

(defn -main [& args]
  (do
    (measure "initial state")
    (doseq [event (:setting/events system)]
      (do (fire event)
          (measure (lablize-event event))))
    (let [histories (:result/histories system)]
      (spit "report.html"
            (publish-html
             (sum-up @histories))))))
