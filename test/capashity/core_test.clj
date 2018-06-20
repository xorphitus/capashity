(ns capashity.core-test
  (:require [clojure.test :refer :all]
            [capashity.core :refer :all]
            [capashity.mock-server :as mock-server]))

(def db {:dbtype   "mysql"
         :dbname   "test_db"
         :host     "127.0.0.1"
         :user     "root"
         :password "password"
         :useSSL false})

(use-fixtures :once
  (fn [f]
    (mock-server/construct-db db)
    (f)
    (mock-server/destruct-db db)))

(deftest test-get-tables
  (testing "first call (without cache)"
    (is (= (get-tables db) ["fruit" "vegetable"])))
  (testing "second call (with cache)"
    (is (= (get-tables db) ["fruit" "vegetable"]))))

(deftest test-count-for-tables
  (testing "first call (without cache)"
    (is (= (count-for-tables db)
           [{:name "test_db.fruit"
             :count 0}
            {:name "test_db.vegetable"
             :count 0}]))))
