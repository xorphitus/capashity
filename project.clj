(defproject capashity "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha17"]
                 [integrant "0.6.1"]
                 [org.clojure/java.jdbc "0.7.3"]
                 [mysql/mysql-connector-java "5.1.45"]]
  :main ^:skip-aot capashity.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
