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

(ns carter.common)

(defn page-title [title]
  (fn []
    [:div {:style {:text-align "center"}}
     [:a {:href "https://7bridges.eu" :title "7bridges.eu s.r.l."}
      [:img {:src "https://7bridges.eu/img/logo-inline.png"
             :alt "7bridges clj-odbp"
             :width "500px"
             :height "122px"}]]
     [:div.ui.section.divider]
     [:h1.ui.blue.header
      {:style {:text-align "center"}}
      title]]))
