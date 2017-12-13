(defproject capashity "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha17"]
                 [org.clojure/java.jdbc "0.7.3"]
                 [cheshire "5.8.0"]
                 [clj-http "3.7.0"]
                 [hiccup "1.0.5"]
                 [integrant "0.6.1"]
                 [mysql/mysql-connector-java "5.1.45"]
                 [com.taoensso/timbre "4.10.0"]
                 ;; for mock server
                 [ring/ring-core "1.6.3"]
                 [ring/ring-jetty-adapter "1.6.3"]]
  :main ^:skip-aot capashity.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
