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

(ns carter.core
  (:require [carter.events]
            [carter.home.events]
            [carter.routes :as routes]
            [carter.subs]
            [carter.twitter.events]
            [carter.views :as views]
            [re-frame.core :as rf]
            [reagent.core :as reagent]))

(defn main-panel []
  (let [active-panel (rf/subscribe [:active-panel])]
    (fn []
      [views/show-panel @active-panel])))

(defn mount-root []
  (rf/clear-subscription-cache!)
  (reagent/render [main-panel]
                  (.getElementById js/document "app")))

(defn ^export init []
  (routes/app-routes)
  (rf/dispatch-sync [:initialize-db])
  (mount-root))
