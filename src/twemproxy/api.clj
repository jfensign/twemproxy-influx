(ns twemproxy.api
 (:require [twemproxy.utils :as utils]
          [twemproxy.config :as config]
          [compojure.core :refer :all]
  		  [compojure.handler :as handler]
          [compojure.route :as route]
          [ring.middleware.json :as middleware]
          [ring.util.response :refer [resource-response response]]))


(defroutes api-routes
  (context "/configuration" []
    (GET "/" []
      (response (utils/fetch-config))))
  (context "/stats" []
  	(GET "/" []
  	  (response (utils/stats-tcp))))
  (route/files "/public" {:root config/public-dir})
  (route/not-found {:status 404 :body {:error "Not Found"}}))


(def api (handler/api api-routes))