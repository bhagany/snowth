(defproject snowth "0.1.0"
  :description ""
  :url ""
  :dependencies [[org.clojure/clojure "1.9.0-alpha12"]
                 [org.clojure/clojurescript "1.9.229"]]

  :plugins [[lein-figwheel "0.5.7"]]

  :source-paths ["src"]
  :clean-targets ^{:protect false} ["out" "target" "resources/public/cljs"]

  :profiles {:dev {:dependencies [[com.cemerick/piggieback "0.2.1"]
                                  [devcards "0.2.1-7"]
                                  [figwheel-sidecar "0.5.7"]
                                  [org.clojure/test.check "0.9.0"]
                                  [sablono "0.7.4"]]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}}

  :cljsbuild {:builds
              {:devcards {:source-paths ["src" "dev"]
                          :figwheel {:devcards true}
                          :compiler {:output-to     "resources/public/cljs/snowth.js"
                                     :output-dir    "resources/public/cljs/out"
                                     :main snowth.devcards
                                     :asset-path    "cljs/out"
                                     :source-map    true
                                     :optimizations :none
                                     :recompile-dependents true
                                     :parallel-build true
                                     :cache-analysis true}}}})
