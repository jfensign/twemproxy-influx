(defproject twemproxy "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.3.1"]
                 [ring/ring-defaults "0.1.2"]
                 [slingshot "0.10.3"]
                 [clj-http "0.9.2"]
                 [ring/ring-json "0.3.1"]
                 [clj-tcp "0.4.9"]
                 [com.taoensso/carmine "2.9.0"]
                 [cheshire "5.4.0"]
                 [capacitor "0.4.2"]]
  :plugins [[lein-ring "0.8.13"]]
  :ring {:handler twemproxy.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})
