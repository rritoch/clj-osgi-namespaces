(ns clojure.osgi.namespaces
  (:import (com.vnetpublishing.clojure.osgi.namespaces 
             DeligatingNamespaceRegistry
             NamespaceUtil)))

(defn start-bundle
  [bnd]
    (NamespaceUtil/startBundle bnd))

(defn stop-bundle
  [bnd]
    (NamespaceUtil/stopBundle bnd))

(defn osgi-active
  []
    (DeligatingNamespaceRegistry/isActive))

(defn osgi-start
  [bc cfg]
    (if (osgi-active)
        (Exception. "OSGI Handler Active"))
    (DeligatingNamespaceRegistry/startFramework bc (:exports cfg)))