(ns twemproxy.handler
  (:require [twemproxy.utils :as utils]
            [twemproxy.config :as config]
  	        [compojure.core :refer :all]
  			    [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.json :as middleware]
            [ring.util.response :refer [resource-response response]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(utils/thread-loop utils/influx-capture config/capture-interval)

(defroutes app-routes
  (GET  "/" []
  	(resource-response "index.html" {:root "public/"}))
  (context "/configure" []
    (GET "/" []
      (utils/fetch-config)
      (response {:start true})))
  (context "/poll" []
  	(GET "/" []
  	  (response (utils/stats-tcp)))))

;;;MIDDLEWARE
(def app 
  (-> (handler/api app-routes)
      (middleware/wrap-json-body)
      (middleware/wrap-json-response)))
