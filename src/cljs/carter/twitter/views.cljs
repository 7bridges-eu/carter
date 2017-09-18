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

(ns carter.twitter.views
  (:require [carter.common :as common]
            [re-frame.core :as rf]))

(defn home-panel
  []
  (fn []
    [:div.ui.container {:style {:margin-top "1em"}}
     [common/page-title "carter"]
     [:div.ui.basic.segment
      [:p
       [:strong "carter"] " is a little application that provides data analysis
       based on your Twitter timeline. It inspects the tweets of your timeline
       and tells you how many tweets per hashtag there are."]
      [:p
       [:strong "carter"] " uses "
       [:a {:href "http://orientdb.com/orientdb/"} "OrientDB"]
       " through "
       [:a {:href "https://7bridges.eu"} "7bridges"]
       " own "
       [:a {:href "https://github.com/7bridges-eu/clj-odbp"} "Clojure driver"]
       " for its binary protocol."]]
     [:div {:style {:text-align "center"}}
      [:a
       {:href "/api/twitter/user-approval"}
       [:img {:src "img/sign-in-with-twitter.png"}]]]
     [:div.ui.section.divider]
     [:div.ui.one.column.centered.grid
      [:div.row
       [:div.ui.basic.segment
        [:img.ui.medium.circular.image
         {:src "img/carter.jpg"}]]]
      [:div.row
       [:p [:strong "carter"] " owns its name to the famous archeologist "
        [:a {:href "https://en.wikipedia.org/wiki/Howard_Carter"}
         "Howard Carter"] "."]]]]))
