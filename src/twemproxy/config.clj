(ns twemproxy.config)

(def twemproxy-host (or (System/getenv "TWEMPROXY_HOST") "localhost"))

(def twemproxy-port (or (System/getenv "TWEMPROXY_PORT") "22222"))

(def influxdb-host (System/getenv "INFLUXDB_HOST"))

(def influxdb-port (System/getenv "INFLUXDB_PORT"))

(def influxdb-user (or (System/getenv "INFLUXDB_USER") "root"))

(def influxdb-pass (or (System/getenv "INFLUXDB_PASS") "root"))

(def influxdb-twemproxy-cluster-table (or (System/getenv "CLUSTER_TABLE") "clusters"))

(def influxdb-twemproxy-health-table (or (System/getenv "HEALTH_TABLE") "health"))

(def influxdb-db (or (System/getenv "DB_NAME") "twemproxy"))

(def capture-interval (or (System/getenv "CAPTURE_INTERVAL") 15))

(def ssh-user (or (System/getenv "SSH_USER") "ubuntu"))

(def ssh-key-path (or (System/getenv "SSH_KEY_PATH") "~/.ssh/id_rsa"))

(def twemproxy-config-path (or (System/getenv "TWEMPROXY_CONFIG_PATH") "/tmp/nutcracker.yml"))