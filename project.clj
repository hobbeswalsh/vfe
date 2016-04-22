(defproject vfe "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins  [[lein-ring "0.9.7"]
             [lein-cljfmt "0.5.2"]
             ]
  :ring  {:handler vfe.core/app}
  :dependencies [
                 [org.clojure/clojure "1.8.0"]
                 [compojure "1.5.0"]
                 [com.stuartsierra/component "0.3.1"]
                 [counsyl/vault-clj "0.3.0-SNAPSHOT"]
                 [hiccup "1.0.5"]
                 [org.apache.httpcomponents/httpclient "4.3.5"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [com.cemerick/friend "0.2.1"]
                 ]
  :main ^:skip-aot vfe.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
