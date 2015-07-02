(ns clojure.osgi.namespaces)

(defn osgi-active
  []
    (DeligatingNamespaceRegistry/isActive))

(defn osgi-start
  [bc cfg]
    (if (osgi-active)
        (Exception. "OSGI Handler Active"))
    (DeligatingNamespaceRegistry/startFramework bc (:exports cfg)))