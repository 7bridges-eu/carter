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

(ns carter.home.views
  (:require [carter.common :as common]
            [carter.graphs :as graphs]
            [carter.validation :as v]
            [clojure.string :as s]
            [re-frame.core :as rf]))

(defn home-panel
  []
  (fn []
    (let [logged-user (rf/subscribe [:logged-user])
          last-update (s/split (:last-update @logged-user) #" ")
          date (second last-update)
          hours (first last-update)
          data (rf/subscribe [:data])
          loading? (rf/subscribe [:loading])
          show-relations-graph? (rf/subscribe [:show-relations-graph])]
      (if @loading?
        [:div.ui.active.dimmer
         [:div.ui.loader]]
        [:div
         [:div.ui.container {:style {:margin-top "1em"}}
          [common/page-title "carter"]
          [:div
           {:style {:padding-top "2em" :text-align "center"}}
           [:div.ui.labeled.mini.input
            {:style {:padding-right ".5em"}}
            [:div.ui.basic.segment
             [:p
              "Hello " [:strong (:screen-name @logged-user)] "! This is a graph
              of the 10 most used hashtags in your timeline."]
             [:p
              "It was updated on " date ", at " hours ". Would you like to "
              [:a {:style {:cursor "pointer"}
                   :on-click #(rf/dispatch [:get-tweets])}
               "refresh it"] "?"]]]]
          [:div
           {:style {:text-align "center"}}
           [:button.ui.button
            {:on-click #(rf/dispatch [:show-relations-graph
                                      @show-relations-graph?])}
            (if @show-relations-graph?
              "Display hashtags graph"
              "Display relations graph")]]
          [:div
           {:style {:text-align "center"}}
           (if (and (false? @show-relations-graph?)
                    (not (empty? @data)))
             [graphs/circles-graph]
             [graphs/nodes-graph])]]]))))
