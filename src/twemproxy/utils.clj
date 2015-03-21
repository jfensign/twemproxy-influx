(ns twemproxy.utils
	(:require [twemproxy.config    :as config]
              [slingshot.slingshot :as slingshot]
              [cheshire.core :refer :all]
              [capacitor.core :as influx]
              [capacitor.async :as influx-async]
              [clojure.core.async :as async])
	(use clj-tcp.client :reload)
	(import (java.io BufferedReader
		             IOException
		             InputStreamReader)
			      (java.net Socket)))


(def influx-c
  (influx/make-client {
    :db       config/influxdb-db
    :username config/influxdb-user
    :password config/influxdb-pass }))


(defn ^:public thread-loop [f s & f-args]
	(future (loop [] (apply f f-args) (Thread/sleep (* s 1000)) (recur))))


(defn ^:private -select-key-by-type
	[m t]
	(select-keys m (for [[k v] m :when (instance? t v)] k)))


(defn ^:public stats-tcp
  "TCP Client to read from Twemproxy stats socket"
  []
  (slingshot/try+
  	(do
  		(let [client (def tcp-cli (Socket. config/twemproxy-host (read-string config/twemproxy-port)))
  			     reader (BufferedReader. (InputStreamReader. (.getInputStream tcp-cli)))
  			     stats  (.readLine reader)]
  			(clojure.walk/keywordize-keys (parse-string stats))))
  	(catch Object _
  		(println (:throwable &throw-context) "Error")
  		{:body (.getMessage (:throwable &throw-context))})))



(defn ^:public influx-capture
	"Timestamp and save stats in influx db"
	[]
	(slingshot/try+
		(do
			(let [stats    (stats-tcp)
				     ts       (:timestamp stats)
				     clusters (-select-key-by-type stats clojure.lang.PersistentArrayMap)]
				(doseq [stat clusters]
					(let [cluster-dbs  (-select-key-by-type (second stat) clojure.lang.PersistentHashMap)
						     influx-data (mapv #(conj (second %) {
						     	:cluster_name (first stat)
									  	:server_name  (clojure.string/replace (str (first %)) #":" "") }) 
						     cluster-dbs)]
						(println "Capture")
						(influx/post-points influx-c config/influxdb-twemproxy-cluster-table influx-data)))))
		(catch Object _
			(println (.getMessage (:throwable &throw-context)) "Error"))))

