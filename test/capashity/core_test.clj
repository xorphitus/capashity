(ns capashity.core-test
  (:require [clojure.test :refer :all]
            [capashity.core :refer :all]
            [capashity.mock-server :as mock-server]))

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

(use-fixtures :once
  (fn [f]
    (mock-server/construct-db db)
    (f)
    (mock-server/destruct-db db)))

(deftest test-get-tables
  (testing "first call (without cache)"
    (is (= (get-tables db) ["FRUIT" "VEGETABLE"])))
  (testing "second call (with cache)"
    (is (= (get-tables db) ["FRUIT" "VEGETABLE"]))))

(deftest test-count-for-tables
  (testing "first call (without cache)"
    (is (= (count-for-tables db)
           [{:name "dummy.FRUIT"
             :count 0}
            {:name "dummy.VEGETABLE"
             :count 0}]))))
