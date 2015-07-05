(defproject clj-osgi-namespaces "0.1.0-SNAPSHOT"
  :description "OSGIify Clojure Namepsace"
  :url "http://example.com/FIXME"
  :license {:name "MIT License"
            :url "http://opensource.org/licenses/MIT"}
  
  :source-paths ["src/main/clojure"]
  :java-source-paths ["src/main/java"]  ; Java source is stored separately.
  :test-paths ["test" "src/test/clojure"]
  :resource-paths ["src/main/resource"] ; Non-code files included in classpath/jar.
  
  :manifest {"Bundle-ManifestVersion" "2"
             "Bundle-SymbolicName" "clojure"
             "Bundle-Version" "1.7.0"
             "Bundle-Name" "Clojure"
             "Bundle-Activator" "com.vnetpublishing.clojure.osgi.namespaces.ClojureOSGIActivator"
             "Import-Package" "org.osgi.framework,org.osgi.framework.wiring"
             "Export-Package" "com.vnetpublishing.clojure.osgi.namespaces;version=\"0.1.0\",clojure;version=\"1.7.0\",clojure.asm;version=\"1.7.0\",clojure.asm.commons;version=\"1.7.0\",clojure.java.api;version=\"1.7.0\",clojure.lang;version=\"1.7.0\",clojure.core;version=\"1.7.0\",clojure.java.clojure.pprint;version=\"1.7.0\",clojure.reflect;version=\"1.7.0\",clojure.test;version=\"1.7.0\""
            }
  :dependencies [[org.clojure/clojure "1.7.0"]]
  
  :profiles {:dev {:dependencies [[org.apache.felix/org.apache.felix.framework "5.0.0"]]
                  }
            })
