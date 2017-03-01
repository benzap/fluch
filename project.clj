(defproject fluch "0.2.0-SNAPSHOT"
  :description "CLJS Frontend CLI"
  :url "http://github.com/benzap/fluch"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.494"]
                 [org.clojure/core.async "0.2.395"]
                 [com.rpl/specter "0.13.2"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [ring "1.5.0"]
                 [ring/ring-defaults "0.2.1"]
                 [bk/ring-gzip "0.1.1"]
                 [ring.middleware.logger "0.5.0"]
                 [compojure "1.5.1"]
                 [environ "1.1.0"]]

  :plugins [[lein-cljsbuild "1.1.5"]
            [lein-environ "1.1.0"]]

  :min-lein-version "2.6.1"

  :source-paths ["src/clj" "src/cljs" "dev/clj" "dev/cljs"]

  :test-paths ["test"]

  :clean-targets ^{:protect false} [:target-path :compile-path "resources/public/js"]

  :uberjar-name "fluch.jar"

  :repl-options {:init-ns user}

  :cljsbuild {:builds
              {:dev
               {:source-paths ["src/cljs" "src/clj" "dev/clj" "dev/cljs"]

                :figwheel {:devcards true}
                ;; Alternatively, you can configure a function to run every time figwheel reloads.
                ;; :figwheel {:on-jsload "fluch.core/on-figwheel-reload"}

                :compiler {:main fluch.site
                           :asset-path "js/compiled/out"
                           :output-to "resources/public/js/compiled/fluch.js"
                           :output-dir "resources/public/js/compiled/out"
                           :source-map-timestamp true}}

               :prod-demo
               {:source-paths ["src/cljs"]
                :compiler {:main fluch.core
                           :asset-path "js/compiled/out"
                           :output-to "dist/js/compiled/fluch.js"
                           :output-dir "dist/js/compiled/out"
                           :optimizations :simple
                           }}}}

  :figwheel {:server-ip "localhost"              ;; default
             :css-dirs ["resources/public/css"]  ;; watch and update CSS
             :ring-handler user/http-handler
             :server-logfile "log/figwheel.log"}

  :profiles {:dev
             {:dependencies [[figwheel "0.5.8"]
                             [figwheel-sidecar "0.5.8"]
                             [com.cemerick/piggieback "0.2.1"]
                             [org.clojure/tools.nrepl "0.2.12"]
                             [devcards "0.2.2"]
                             [sablono "0.7.7"]
                             [org.clojure/test.check "0.9.0"]]

              :plugins [[lein-figwheel "0.5.8"]]}

             :uberjar
             {:source-paths ^:replace ["src/clj"]
              :hooks [leiningen.cljsbuild]
              :omit-source true
              :aot :all
              :cljsbuild {:builds
                          {:app
                           {:source-paths ^:replace ["src/cljs"]
                            :compiler
                            {:optimizations :simple
                             :pretty-print false}}}}}}
  )
