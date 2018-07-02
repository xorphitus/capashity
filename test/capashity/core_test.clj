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

(deftest test-sum-up
  (testing "no data"
    (let [input    [{}]
          expected [{}]]
      (is expected (sum-up input))))
  (testing "has one datum"
    (let [input    [{:event "POST foo"
                     :counts [{:name "dummy.FRUIT", :count 0}]}]
          expected [{:event "POST foo"
                     :counts [{:name "dummy.FRUIT", :count 0}]}]]
      (is expected (sum-up input))))
  (testing "has sum data"
    (let [input    [{:event "POST foo"
                     :counts [{:name "dummy.FRUIT",     :count 1}
                              {:name "dummy.VEGETABLE", :count 2}]}
                    {:event "POST foo"
                     :counts [{:name "dummy.FRUIT",     :count 2}
                              {:name "dummy.VEGETABLE", :count 4}]}
                    {:event "POST bar"
                     :counts [{:name "dummy.FRUIT",     :count 0}
                              {:name "dummy.VEGETABLE", :count 5}]}]
          expected [{:event "POST foo"
                     :counts [{:name "dummy.FRUIT",     :count 1}
                              {:name "dummy.VEGETABLE", :count 2}]}
                    {:event "POST foo"
                     :counts [{:name "dummy.FRUIT",     :count 1}
                              {:name "dummy.VEGETABLE", :count 2}]}
                    {:event "POST bar"
                     :counts [{:name "dummy.FRUIT",     :count -1}
                              {:name "dummy.VEGETABLE", :count 1}]}]]
      (is expected (sum-up input)))))

(deftest test-labelize-event
  (testing "generic case"
    (let [event {:method :post :url "https://example.com/foo"}]
      (is "POST https://example.com/foo" (labelize-event event)))))

(deftest test-parse-events
  (testing "just one event"
    (let [cnt (atom 0)
          events [{:method :get, :url "http://example.com"}]]
      (parse-events events
                    {}
                    (fn [_ _] nil)
                    (fn [_] (swap! cnt inc)))
      (is @cnt 1)))
  (testing "two events"
    (let [cnt (atom 0)
          events [{:method :get,  :url "http://example.com"}
                  {:method :post, :url "http://example.com"}]]
      (parse-events events
                    {}
                    (fn [_ _] nil)
                    (fn [_] (swap! cnt inc)))
      (is @cnt 2)))
  (testing "with decoy"
    (let [cnt (atom 0)
          events [{:method :get,  :url "http://example.com", :decoy true}
                  {:method :post, :url "http://example.com", :decoy false}]]
      (parse-events events
                    {}
                    (fn [_ _] nil)
                    (fn [_] (swap! cnt inc)))
      (is @cnt 1)))
  (testing "with takeover"
    (let [params (atom [])
          events [{:method :get,  :url "http://example.com", :takeover true}
                  {:method :post, :url "http://example.com/{{foo.bar}}"}]]
      (parse-events events
                    {}
                    (fn [_ param]
                      (do (swap! params conj param)
                          {:foo {:bar 42}}))
                    identity)
      (is params [{}
                  {:foo {:bar 42}}])))
  (testing "without takeover"
    (let [params (atom [])
          events [{:method :get,  :url "http://example.com", :takeover false}
                  {:method :post, :url "http://example.com/{{foo.bar}}"}]]
      (parse-events events
                    {}
                    (fn [_ param]
                      (do (swap! params conj param)
                          {:foo {:bar 42}}))
                    identity)
      (is params [{}
                  {}]))))
