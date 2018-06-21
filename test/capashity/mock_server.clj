(ns capashity.mock-server
  (:require [clojure.java.jdbc :as jdbc]
            [cheshire.core :refer [generate-string]]
            [ring.adapter.jetty :as server]
            [taoensso.timbre :as timbre]))

(def db
  {:classname "org.h2.Driver"
   :subprotocol "h2"
   ;; `DB_CLOSE_DELAY=-1` is required.
   ;; otherwise, the content of the database is lost whenever the last connection is closed
   :subname "mem:test_db;DB_CLOSE_DELAY=-1"
   :user "root"
   :password "password"
   ;; H2 doesn't require dbname option
   ;; but Capashity use this item for identifier
   :dbname "dummy"})

(def dbs [db])

(defonce server (atom nil))

(defn construct-db [db]
  (jdbc/db-do-commands
   db
   [(jdbc/create-table-ddl :fruit
                           [[:name "varchar(32)"]
                            [:cost :int]])
    (jdbc/create-table-ddl :vegetable
                           [[:name "varchar(32)"]
                            [:cost :int]])]))

(defn destruct-db [db]
  (jdbc/db-do-commands
   db
   [(jdbc/drop-table-ddl :fruit)
    (jdbc/drop-table-ddl :vegetable)]))

(defn find-db [name]
  (first (filter #(= name (:dbname %))
                 dbs)))

(defn handler [req]
  (let [path (:uri req)
        params (remove empty?
                       (clojure.string/split path #"/"))
        db-name (first params)
        table (second params)]
    (jdbc/insert! (find-db db-name) (keyword table) {})
    (timbre/debug "record inserted")
    {:status 200
     :headers {"Content-Type" "text/plain"}
     :body (generate-string {:db db-name
                             :table table})}))

(defn start-server []
  (when-not @server
    (reset! server (server/run-jetty handler {:port 3000 :join? false}))))

(defn stop-server []
  (when @server
    (.stop @server)
    (reset! server nil)))
