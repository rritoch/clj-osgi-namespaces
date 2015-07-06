# clj-osgi-namespaces

A Clojure library designed to enable importing and exporting 
of clojure namespaces when run within an OSGI container.

## Compilation

```
lein javac
lein uberjar
```

Place the generated *-standalone.jar in your OSGI autoload (bundles/) folder


## Usage

This is for testing purposes only, use at your own risk.

The following non-standard OSGI Manfiest headers are supported

* Clojure-Imports
* Clojure-Exports
* Clojure-Enable
* Clojure-Activator


The *Clojure-Imports* and *Clojure-Exports* accept a cama seperated list of versioned imports. 
These function like the OSGI *Import-Package* and *Export-Package* headers accept they do not accept a uses clause.

The *Clojure-Enable* header value can only contain a value of true or false

The *Clojure-Activator* header contains a clojure namespace which MUST define start and stop functions which accept a single parameter which will be an instance of the BundleContext.

See: [clj-osgi-namespaces-hello-world](https://github.com/rritoch/clj-osgi-namespaces-hello-world)

## License

Copyright © 2015 Ralph Ritoch

Distributed under the MIT License