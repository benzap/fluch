(defproject fluch "0.1.0-SNAPSHOT"
  :description "Clojurescript Terminal Emulator with $#!*%&'s"
  :url "http://github.com/benzap/fluch"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.14"]
                 [org.clojure/core.async "0.2.374"]
                 [prismatic/schema "1.1.1"]
                 [com.rpl/specter "0.10.0"]
                 [funcool/cats "1.2.1"]]
  :plugins [[lein-cljsbuild "1.1.3"]]
  :hooks [leiningen.cljsbuild]
  :repositories [["clojars" {:sign-releases false}]]
  :source-paths ["src/clj" "src/cljs"]
  :doo {:build "test"}
  :cljsbuild {:builds {:dev
                       {:source-paths ["src/cljs" "src/cljs_dev"]
                        :compiler {:output-dir "resources/public/js/out"
                                   :output-to "resources/public/js/dev_fluch.js"
                                   :asset-path "js/out"
                                   :optimizations :none
                                   :main fluch.dev.core
                                   :pretty-print true
                                   :source-map true}}
                       :prod
                       {:source-paths ["src/cljs"]
                        :compiler {:output-dir "dist/out_prod"
                                   :output-to "dist/fluch.min.js"
                                   :jar true
                                   :main fluch.core
                                   :optimizations :whitespace
                                   :pretty-print false}}
                       :test
                       {:source-paths ["src/cljs" "test/cljs"]
                        :compiler {:output-to "resources/public/js/test_fluch.js"
                                   :optimizations :none
                                   :main fluch.test-runner}}
                       }}
  
  
  :profiles {:dev {:dependencies [[doo "0.1.7-SNAPSHOT"]
                                  [binaryage/devtools "0.6.1"]]
                   :plugins [[lein-doo "0.1.6"]]}})
