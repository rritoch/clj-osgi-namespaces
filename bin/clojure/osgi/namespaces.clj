(ns clojure.osgi.namespaces)

(defn osgi-active
  []
    (ClojureOSGINamespaceHandler/active))

(defn register-osgi-environment
  [cl exports imports]
    (ClojureOSGINamespaceHandler/register cl imports exports))

(defn start-clj-bundle
  [bundle cfg]
  (let [cl (.getClassLoader (.adapt bundle BundleWiring))]
    (register-osgi-environment cl (:exports cfg) (:imports cfg))))

(defn osgi-start
  [bc cfg]
    (if (osgi-active)
        (Exception. "OSGI Handler Active"))
    (ClojureOSGINamespaceHandler/start (Thread/currentContextLoader) (:exports cfg)))