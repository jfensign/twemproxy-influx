(ns twemproxy.utils
	(:require [twemproxy.config    :as config]
              [slingshot.slingshot :as slingshot]
              [cheshire.core :refer :all]
              [capacitor.core :as influx]
              [capacitor.async :as influx-async]
              [clojure.core.async :as async]
              [clj-yaml.core :as yaml]
              [taoensso.carmine :as car :refer (wcar)])
	(use  clj-tcp.client :reload
		     clj-ssh.ssh
		     [clojure.java.shell :only [sh]])
	(import (java.io BufferedReader
		             IOException
		             InputStreamReader)
			      (java.net Socket)))


(def influx-c
  (influx/make-client {
    :db       config/influxdb-db
    :username config/influxdb-user
    :password config/influxdb-pass }))


(def redis-conn 
	{:pool {} 
	 :spec {:port (read-string config/local-redis-port)}})


(defmacro wcar* [& body]
 "Redis be pipin'" 
	`(car/wcar redis-conn ~@body))


(defn ^:public thread-loop [f s & f-args]
	"Run indefinitely using futures"
	(future (loop [] (apply f f-args) (Thread/sleep (* s 1000)) (recur))))


(defn ^:private parse-lt
	"Latency parse"
	[stdin]
	(reduce #(assoc % (keyword (str "latency_" (clojure.string/replace (nth %2 1) #"2K" "")))
		                 (read-string (nth %2 2))) 
	        {} 
	        (re-seq #"(\w+): (\d+)" stdin)))


(defn- ^:private select-key-by-type
	"Filters keys by clojure.lang.THATSUPTOYOU!"
	[m t]
	(select-keys m (for [[k v] m :when (instance? t v)] k)))


(defn ^:public stats-tcp
  "TCP Client to read from Twemproxy stats socket"
  []
  (slingshot/try+
  	(do
  		(let [client (Socket. config/twemproxy-host (read-string config/twemproxy-port))
  			     reader (BufferedReader. (InputStreamReader. (.getInputStream client)))
  			     stats  (.readLine reader)]
  			(clojure.walk/keywordize-keys (parse-string stats))))
  	(catch Object _
  		(println (:throwable &throw-context) "Error")
  		{:body (.getMessage (:throwable &throw-context))})))


(defn ^:public parse-nutcracker-config
	"Parses nutcracker.yml"
	([]
		(parse-nutcracker-config (str "./nutcracker." config/twemproxy-host ".yml")))
	([^String f-path]
		(clojure.walk/keywordize-keys (yaml/parse-string (slurp f-path)))))


(defn- ^:private ssh-redis-info
	"Connect to remote Redis server via SSH tunnel. 
	 Useful if you can't configure a static IP address or your Redis server resides in a VPC."
	[server]
		(slingshot/try+
			(let [ssh-a (ssh-agent {})]
				(add-identity ssh-a {:private-key-path config/ssh-key-path})
				(let [sess  (session ssh-a config/twemproxy-host {:strict-host-key-checking :no 
																																																						:username config/ssh-user})
					     server-split (clojure.string/split server #":")]
			 (with-connection sess
			 	(with-local-port-forward [sess (read-string config/local-redis-port) 
			 																																(read-string (nth server-split 1)) 
			 																																(first server-split)]
      (clojure.walk/keywordize-keys (wcar* (car/info*)))))))
			(catch Object _
				(println (.getMessage (:throwable &throw-context)) "Errors"))))


(defn ^:public fetch-config
	"Retrieves nutcracker.yml via scp. 
	 Parses into clojure.lang.HashMap and stores in atom config/nutcracker-yml"
	([]
		(fetch-config config/twemproxy-config-path config/ssh-key-path config/ssh-user))
	([^String f-path ^String i-path ^String x-user]
		(slingshot/try+
			(let [ssh-a (ssh-agent {})]
				(add-identity ssh-a {:private-key-path i-path})
				(let [sess  (session ssh-a config/twemproxy-host {:strict-host-key-checking :no 
																																																						:username x-user})]
			 (with-connection sess
			 	(let [cp-to (str "./nutcracker." config/twemproxy-host ".yml")
			 		     conf-file (scp-from sess f-path cp-to)]
			 		(reset! config/nutcracker-yml (parse-nutcracker-config cp-to))
			 		@config/nutcracker-yml))))
			(catch Object _
				(println (.getMessage (:throwable &throw-context)) "Errors")))))


(defn ^:public cluster-latency
	"Run redis-benchmark against cluster"
	[cluster]
	(slingshot/try+
		(let [cluster-config ((keyword cluster) @config/nutcracker-yml)]
			(if cluster-config
					(parse-lt
						(:out 
							(sh "timeout"
	 									"5s"
						 				"redis-cli"
						 				"--latency"
						 	 		"-h" config/twemproxy-host 
						 	 		"-p" (last (clojure.string/split (:listen cluster-config) #":")))))
				{:status 404 
					:body {:error "Not found"}}))
		(catch Object _
			(println (.getMessage (:throwable &throw-context)) "Error"))))


(defn ^:public influx-capture-latency
	"Timestamp and save latency info in influx db"
	[]
	(slingshot/try+
		(doseq [cluster (keys @config/nutcracker-yml)]
			(let [latency (cluster-latency cluster)
				     influx-data (conj {:cluster_name (clojure.string/replace (str cluster) #":" "")} latency)]
				(influx/post-points influx-c config/influxdb-twemproxy-cluster-table [influx-data])))
		(catch Object _
			(println (.getMessage (:throwable &throw-context)) "Error"))))


(defn ^:public influx-capture-info
	"Timestamp and save stats in influx db"
	[]
	(slingshot/try+
		(do
			(let [stats    (stats-tcp)
				     ts       (:timestamp stats)
				     clusters (select-key-by-type stats clojure.lang.PersistentArrayMap)]
				(doseq [cluster clusters]
					(let [cluster-dbs  (select-key-by-type (second cluster) clojure.lang.PersistentHashMap)
						     influx-data (mapv #(conj (second %) {
						     	:cluster_name (first cluster)
									  	:server_name  (clojure.string/replace (str (first %)) #":" "") })
						     cluster-dbs)]
						(doseq [datum influx-data]
							(let [server (first (for [server (:servers ((:cluster_name datum) @config/nutcracker-yml)) 
																																	:let [server-split (clojure.string/split server #"\s+")
									                              server-id (last server-split)
									                              server-ad (first server-split)] 
									                        :when (= (:server_name datum) server-id)] server-ad))
								     server-stats (apply conj datum (ssh-redis-info server) {:connection_string server})]
								(influx/post-points influx-c config/influxdb-twemproxy-cluster-table [server-stats])))))))
		(catch Object _
			(println (.getMessage (:throwable &throw-context)) "Error"))))

 