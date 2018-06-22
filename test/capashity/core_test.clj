(ns capashity.core-test
  (:require [clojure.test :refer :all]
            [capashity.core :refer :all]
            [capashity.mock-server :as mock]))

(use-fixtures :once
  (fn [f]
    (mock/construct-db mock/db)
    (mock/start-server)
    (f)
    (mock/destruct-db mock/db)
    (mock/stop-server)))

(deftest test-get-tables
  (testing "first call (without cache)"
    (is (= (get-tables mock/db) ["FRUIT" "VEGETABLE"])))
  (testing "second call (with cache)"
    (is (= (get-tables mock/db) ["FRUIT" "VEGETABLE"]))))

(deftest test-count-for-tables
  (testing "first call"
    (is (= (count-for-tables mock/db)
           [{:name "dummy.FRUIT"
             :count 0}
            {:name "dummy.VEGETABLE"
             :count 0}])))
  (testing "after api call"
    (do
      (fire {:method "GET" :url "http://localhost:3000/dummy/FRUIT"})
      (is (= (count-for-tables mock/db)
             [{:name "dummy.FRUIT"
               :count 1}
              {:name "dummy.VEGETABLE"
               :count 0}])))))
