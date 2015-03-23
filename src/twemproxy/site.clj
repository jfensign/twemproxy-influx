(ns twemproxy.site
 (:require [twemproxy.config :as config]
          [compojure.core :refer :all]
  		  [compojure.handler :as handler]
          [compojure.route :as route]
          [ring.middleware.json :as middleware]
          [ring.util.response :refer [resource-response response]]
          [clojure.java.io :as io]))

(defroutes site-routes
  (GET "/" [] (slurp (str config/public-dir "/html/index.html"))))

(def site (handler/site site-routes))