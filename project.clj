(defproject snowth "0.1.2"
  :description "Analemma calculations"
  :url "https://github.com/bhagany/snowth"
  :dependencies [[org.clojure/clojure "1.9.0-alpha12"]
                 [org.clojure/clojurescript "1.9.293"]]

  :plugins [[lein-cljsbuild "1.1.4"]
            [lein-figwheel "0.5.7"]
            [lein-doo "0.1.7"]]

  :source-paths ["src"]
  :clean-targets ^{:protect false} ["out" "target" "resources/public/cljs"]

  :profiles {:dev {:dependencies [[cljsjs/chroma "1.1.1-0"]
                                  [cljsjs/d3 "4.2.2-0"]
                                  [com.cemerick/piggieback "0.2.1"]
                                  [devcards "0.2.1-7" :exclusions [org.clojure/clojure
                                                                   org.clojure/clojurescript
                                                                   cljsjs/react
                                                                   cljsjs/react-dom
                                                                   cljsjs/react-dom-server
                                                                   cljsjs/react-server]]
                                  [doo "0.1.7"]
                                  [figwheel-sidecar "0.5.7" :exclusions [org.clojure/core.async
                                                                         org.clojure/tools.analyzer.jvm
                                                                         org.clojure/tools.analyzer
                                                                         org.clojure/core.memoize
                                                                         org.clojure/core.cache
                                                                         org.clojure/data.priority-map]]
                                  [org.clojure/core.async "0.2.391"]
                                  [org.clojure/test.check "0.9.0"]
                                  [reagent "0.6.0"]
                                  [sablono "0.7.4"]]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}}

  :cljsbuild {:builds
              [{:id "devcards"
                :source-paths ["src" "dev"]
                :figwheel {:devcards true}
                :compiler {:output-to     "resources/public/cljs/snowth.js"
                           :output-dir    "resources/public/cljs/out"
                           :source-map    true
                           :source-map-timestamp true
                           :main snowth.devcards.core
                           :asset-path    "cljs/out"
                           :optimizations :none
                           :recompile-dependents true
                           :parallel-build true
                           :cache-analysis true}}
               {:id "github-pages"
                :source-paths ["src" "dev"]
                :compiler {:output-to "docs/gh-pages.js"
                           :output-dir "docs/out"
                           :source-map "docs/gh-pages.js.map"
                           :source-map-timestamp true
                           :asset-path "out"
                           :main snowth.devcards.core
                           :devcards true
                           :optimizations :advanced}}]})
