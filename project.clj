(defproject carter "0.1.0-SNAPSHOT"
  :description "carter: tweets analysis via graph database"
  :url "http://lab.7bridges.eu/7b/carter"
  :license {:name "Apache License 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0.html"}

  :dependencies
  [;; Clojure
   [clj-time "0.14.0"]
   [com.taoensso/timbre "4.10.0"]
   [eu.7bridges/clj-odbp "0.2.1"]
   [hiccup "1.0.5"]
   [http-kit "2.2.0"]
   [metosin/compojure-api "1.1.10"]
   [metosin/ring-http-response "0.8.2"]
   [midje "1.8.3"]
   [mount "0.1.11"]
   [org.clojure/clojure "1.8.0"]
   [ring "1.6.0"]
   [ring/ring-defaults "0.2.3"]
   [ring/ring-json "0.4.0"]
   [ring/ring-mock "0.3.0"]
   [ring-middleware-format "0.7.2"]
   [twitter-api "1.8.0"]

   ;; ClojureScript
   [bidi "2.0.16"]
   [cljs-ajax "0.5.9"]
   [cljsjs/d3 "4.3.0-5"]
   [cljsjs/semantic-ui-react "0.68.4-0"]
   [day8.re-frame/http-fx "0.1.3"]
   [kibu/pushy "0.3.7"]
   [org.clojure/clojurescript "1.9.521"]
   [re-frame "0.9.2"]
   [reagent "0.6.1"]
   [reagent-utils "0.2.1"]]

  :plugins [[lein-cljsbuild "1.1.5"]
            [lein-cloverage "1.0.9"]
            [lein-midje "3.1.3"]]

  :aliases {"coverage" ["cloverage" "--runner" ":midje"]
            "init-db" ["run" "-m" "carter.tasks.init"]
            "reset-db" ["run" "-m" "carter.tasks.reset"]}

  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

  :figwheel {:css-dirs ["resources/public/css"]}

  :source-paths ["src/clj"]
  :test-paths ["test/clj" "test/cljs"]
  :resource-paths ["resources"]
  :target-path "target/%s"

  :clean-targets ^{:protect false} [:target-path "resources/public/js"]

  :main ^:skip-aot carter.core

  :profiles
  {:dev
   {:dependencies [[binaryage/devtools "0.9.4"]
                   [com.cemerick/piggieback "0.2.1"]
                   [figwheel-sidecar "0.5.10"]]
    :plugins [[lein-figwheel "0.5.10"]]}}

  :cljsbuild
  {:builds
   [{:id "dev"
     :source-paths ["src/cljs"]
     :figwheel true
     :compiler {:main carter.core
                :asset-path "js/out"
                :output-to "resources/public/js/main.js"
                :output-dir "resources/public/js/out"
                :preloads [devtools.preload]
                :external-config {:devtools/config
                                  {:features-to-install :all}}}}]})
