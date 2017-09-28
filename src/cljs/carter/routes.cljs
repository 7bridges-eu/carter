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

(ns carter.routes
  (:require [bidi.bidi :as bidi]
            [pushy.core :as pushy]
            [re-frame.core :as rf]))

(def routes
  ["/" {"" :user-home
        "sign-in" :twitter-home}])

(defn- parse-url []
  (fn [url]
    (bidi/match-route routes url)))

(defn- keywordize-route [route]
  (keyword (str route "-panel")))

(defn- dispatch-route [matched-route]
  (let [route (name (:handler matched-route))
        panel-name (keywordize-route route)]
    (rf/dispatch [:set-active-panel panel-name])))

(defn app-routes []
  (pushy/start! (pushy/pushy dispatch-route (parse-url))))
