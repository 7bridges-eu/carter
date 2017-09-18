;; Copyright 2017 7bridges s.r.l.
;;
;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at
;;
;; http://www.apache.org/licenses/LICENSE-2.0
;;
;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.

(ns carter.services.http
  (:require [carter.api.routes :as api]
            [carter.handlers :as handlers]
            [carter.services.config :as config]
            [compojure
             [core :as compojure]
             [route :as route]]
            [mount.core :as mount]
            [org.httpkit.server :as server]
            [ring.middleware
             [cookies :as cookies]
             [defaults :refer [api-defaults wrap-defaults]]
             [reload :as reload]]))

(compojure/defroutes app
  (-> (compojure/routes
       (route/resources "/")
       (compojure/GET "/" request (handlers/sign-in request))
       (-> (compojure/GET
            "/home" [oauth_token oauth_verifier]
            (handlers/prepare-homepage oauth_token oauth_verifier))
           (wrap-defaults api-defaults))
       api/routes)
      cookies/wrap-cookies
      reload/wrap-reload))

(defonce server (atom nil))

(defn stop-server!
  []
  (when-not (nil? @server)
    (@server :timeout 100)
    (reset! server nil)))

(defn start-server!
  []
  (let [port (get-in config/config [:http :port])]
    (reset! server (server/run-server app {:port port}))))

(mount/defstate http-server
  :start (start-server!)
  :stop (stop-server!))
