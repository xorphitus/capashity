(ns capashity.mock-server
  (:require [clojure.java.jdbc :as jdbc]
            [ring.adapter.jetty :as server]))

(defonce server (atom nil))

(def db (:setting/db capashity.core/system))

(defn construct-db []
  (jdbc/db-do-commands
   db
   [(jdbc/create-table-ddl :fruit
                           [[:name "varchar(32)"]
                            [:cost :int]])
    (jdbc/create-table-ddl :vegetable
                           [[:name "varchar(32)"]
                            [:cost :int]])]))

(defn destruct-db []
  (jdbc/db-do-commands
   db
   [(jdbc/drop-table-ddl :fruit)
    (jdbc/drop-table-ddl :vegetable)]))

(defn handler [req]
  (let [path (:uri req)
        params (filter #(not (empty? %))
                       (clojure.string/split path #"/"))
        table (first params)]
    (do
      (jdbc/insert! db (keyword table) {})
      {:status 200
       :headers {"Content-Type" "text/plain"}
       :body (format "A record is inserted into table \"%s\"" table)})))

(defn start-server []
  (when-not @server
    (reset! server (server/run-jetty handler {:port 3000 :join? false}))))

(defn stop-server []
  (when @server
    (.stop @server)
    (reset! server nil)))

(defn restart-server []
  (do
    (stop-server)
    (start-server)))

(defn start []
  (do
    (construct-db)
    (start-server)))

(defn stop []
  (do
    (destruct-db)
    (stop-server)))
