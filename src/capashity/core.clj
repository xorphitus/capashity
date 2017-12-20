(ns capashity.core
  (:gen-class)
  (:require [capashity.report :refer [publish-html]]
            [clojure.java.jdbc :as jdbc]
            [cheshire.core :refer [generate-string parse-string]]
            [clj-http.client :as client]
            [integrant.core :as ig]
            [taoensso.timbre :as timbre]
            [selmer.parser :as selmer-parser]))

;; TODO: specify file pathes via command line arguments or environmental variables
(def config
  {:setting/dbs "databases.edn"
   :setting/events "events.edn"
   :result/tables (atom {})
   :result/histories (atom [])})

;; TODO: validate file contents
(defmethod ig/init-key :setting/dbs [_ path]
  (ig/read-string (slurp path)))

;; TODO: validate file contents
;; TODO: enable variables
(defmethod ig/init-key :setting/events [_ path]
  (remove :skip
    (ig/read-string (slurp path))))

(defmethod ig/init-key :result/tables [_ conf]
  "currently, there's nothing to do"
  conf)

(defmethod ig/init-key :result/histories [_ conf]
  "currently, there's nothing to do"
  conf)

(defmethod ig/halt-key! :result/tables [_ tables]
  (reset! tables {}))

(defmethod ig/halt-key! :result/histories [_ histories]
  (reset! histories []))

(def system
  (ig/init config))

(defn get-tables [db]
  (let [cache-key (format "%s:%d/%s" (:host db) (:port db) (:dbname db))]
    (if-let [tbls (get (deref (:result/tables system)) cache-key)]
      tbls
      (let [tbls (map #(val (first %)) (jdbc/query db "SHOW TABLES"))]
        (swap! (:result/tables system) assoc cache-key tbls)
        tbls))))

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
(defn fire
  ([event] (fire event {}))
  ([event params]
   (do
     (timbre/debug "event fired" (:url event))
     (-> {:method (:method event)
          :headers (:headers event)
          :url (selmer-parser/render (:url event) params)
          :body (-> event
                    :param ; TODO: rename :param -> :body
                    generate-string
                    (selmer-parser/render params))}
         client/request
         :body
         (parse-string true)))))

(defn labelize-event [ev]
  (timbre/debug {:method ev})
  (format "%s %s"
          (.toUpperCase (name (:method ev)))
          (:url ev)))

(defn -main [& args]
  (do
    (measure "initial state")
    (loop [[fst & rst] (:setting/events system)
           params {}]
      (let [response (fire fst params)]
        (when-not (:decoy fst)
          (measure (labelize-event fst)))
        (when-not (empty? rst)
          (recur rst response))))
    (let [histories (:result/histories system)]
      (spit "report.html"
            (publish-html
             (sum-up @histories))))))
