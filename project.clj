(defproject fluch "0.1.0-SNAPSHOT"
  :description "CLJS Frontend CLI"
  :url "http://github.com/benzap/fluch"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.494"]
                 [org.clojure/core.async "0.2.395"]
                 [com.rpl/specter "1.0.0"]]

  :plugins [[lein-cljsbuild "1.1.5"]
            [lein-environ "1.1.0"]]

  :min-lein-version "2.6.1"

  :source-paths ["src"]

  :test-paths ["test"]

  :clean-targets ^{:protect false} [:target-path :compile-path "resources/public/js"]

  :uberjar-name "fluch.jar"

  :repl-options {:init-ns dev.fluch.user}

  :cljsbuild {:builds
              {:dev
               {:source-paths ["src" "dev"]

                :figwheel true

                :compiler {:main fluch.core
                           :asset-path "js/compiled/out"
                           :output-to "resources/public/js/compiled/fluch.js"
                           :output-dir "resources/public/js/compiled/out"
                           :optimizations :none
                           :source-map-timestamp true}}

               :prod
               {:source-paths ["src"]
                :compiler {:main fluch.core
                           :asset-path "js/compiled/out_prod"
                           :output-to "dist/js/compiled/fluch.min.js"
                           :output-dir "dist/js/compiled/out_prod"
                           :optimizations :advanced
                           }}}}

  :figwheel {:css-dirs ["resources/public/css"]  ;; watch and update CSS
             :server-logfile "log/figwheel.log"}

  :profiles {:dev
             {:dependencies [[figwheel "0.5.8"]
                             [figwheel-sidecar "0.5.8"]
                             [org.clojure/tools.nrepl "0.2.12"]]

              :plugins [[lein-figwheel "0.5.8"]]}})
