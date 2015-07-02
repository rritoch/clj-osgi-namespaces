(defproject clj-osgi-namespaces "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  
  :source-paths ["src/main/clojure"]
  :java-source-paths ["src/main/java"]  ; Java source is stored separately.
  :test-paths ["test" "src/test/clojure"]
  :resource-paths ["src/main/resource"] ; Non-code files included in classpath/jar.
  
  :manifest {"Bundle-ManifestVersion" "2"
             "Bundle-SymbolicName" "clojure"
             "Bundle-Version" "1.7.0"
             "Bundle-Name" "Clojure"
             "Export-Package" "clojure;version=\"1.7.0\",clojure.asm;version=\"1.7.0\",clojure.asm.commons;version=\"1.7.0\",clojure.java.api;version=\"1.7.0\",clojure.lang;version=\"1.7.0\",clojure.core;version=\"1.7.0\",clojure.java.clojure.pprint;version=\"1.7.0\",clojure.reflect;version=\"1.7.0\",clojure.test;version=\"1.7.0\""
            }
  :dependencies [[org.clojure/clojure "1.7.0"]
[org.apache.felix/org.apache.felix.framework "5.0.0"]])
