(ns capashity.core-test
  (:require [clojure.test :refer :all]
            [capashity.core :refer :all]
            [capashity.mock-server :as mock-server]))

(def db {:dbtype   "mysql"
         :dbname   "test_db"
         :host     "127.0.0.1"
         :user     "root"
         :password "password"})

(use-fixtures :once
  (fn [f]
    (mock-server/construct-db db)
    (f)
    (mock-server/destruct-db db)))

(deftest test-get-tables
  (testing "get tables"
    (is (= (get-tables db) ["fruit" "vegetable"]))))
