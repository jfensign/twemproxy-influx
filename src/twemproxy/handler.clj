(ns twemproxy.handler
  (:require [twemproxy.utils :as utils]
            [twemproxy.config :as config]
            [twemproxy.api  :refer [api]]
            [twemproxy.site :refer [site]]
  	        [compojure.core :refer :all]
  			    [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.json :as middleware]
            [ring.util.response :refer [resource-response response]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]))

;Get/Set Twemproxy configuration
(utils/fetch-config)

;Periodically save redis stats in influxDB every X seconds where x = config/intrerval
(utils/thread-loop utils/influx-capture-info config/capture-interval)
(utils/thread-loop utils/influx-capture-latency config/capture-interval)

;;;MIDDLEWARE
(def app 
  (-> (routes site api)
      (middleware/wrap-json-body)
      (middleware/wrap-json-response)
      (wrap-defaults api-defaults)))