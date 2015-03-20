(ns twemproxy.config)

(def twemproxy-host (System/getenv "TWEMPROXY_HOST"))

(def twemproxy-port (System/getenv "TWEMPROXY_PORT"))