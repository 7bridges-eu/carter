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
            [carter.validation :as v]
            [clojure.string :as s]
            [cljsjs.d3]
            [re-frame.core :as rf]
            [reagent.core :as reagent]))

(def graph-width 850)
(def graph-height 850)

(def pack
  (-> js/d3 .pack (.size #js [graph-width graph-height]) (.padding 1.5)))

(defn get-data
  "Retrieve data subscription. Add :value for d3.hierarchy."
  []
  (let [data @(rf/subscribe [:data])]
    (mapv (fn [d] (assoc d :value (:tweets d))) data)))

(defn data-clj->data-js
  "Transform `data` in a JS object suitable for d3.hierachy."
  [data]
  (clj->js {:name "hashtags" :children (get-data)}))

(defn pack-hierarchy
  "Generate a d3 hierarchy from `data-js` and pack it."
  [data-js]
  (-> js/d3
      (.hierarchy data-js)
      (.sum (fn [d] (.-value d)))
      pack))

(defn create-circles
  "Create the node circles starting from `root` node."
  [root]
  (-> (js/d3.select "svg")
      (.selectAll ".node")
      (.data (.leaves root))
      .enter
      (.append "g")
      (.attr "class" "node")
      (.attr "transform" (fn [d]
                           (str "translate(" (.-x d) "," (.-y d) ")")))
      (.append "circle")
      (.attr "r" (fn [d] (.-r d)))
      (.style "fill" (fn [_]
                       (nth (.-schemeCategory20c js/d3) (rand-int 20))))))

(defn create-texts
  "Add hashtags to the circles."
  []
  (-> (js/d3.select "svg")
      (.selectAll ".node")
      (.append "text")
      (.attr "dy" ".3em")
      (.style "text-anchor" "middle")
      (.text (fn [d]
               (let [text (get (js->clj (.-data d)) "hashtag")
                     radius (.-r d)]
                 (if (< (/ radius 2) (+ 25 (count text)))
                   (str (subs text 0 5) "…")
                   text))))))

(defn create-tooltips
  "Add hashtags as tooltips to every circle."
  []
  (-> (js/d3.select "svg")
      (.selectAll ".node")
      (.append "title")
      (.text (fn [d]
               (str (get (js->clj (.-data d)) "hashtag") "\n"
                    "tweets: " (get (js->clj (.-data d)) "tweets"))))))

(defn circles-enter
  []
  (let [data (get-data)
        data-js (data-clj->data-js data)
        root (pack-hierarchy data-js)]
    (create-circles root)
    (create-texts)
    (create-tooltips)))

(defn circles-exit
  []
  (let [data (get-data)]
    (-> (js/d3.select "#graph svg .container .circles")
        (.selectAll "circle")
        (.data (clj->js data))
        .exit
        .remove)))

(defn circles-did-update
  []
  (circles-enter)
  (circles-exit))

(defn circles-did-mount
  []
  (-> (js/d3.select "#graph svg .container")
      (.append "g")
      (.attr "class" "circles"))
  (circles-did-update))

(defn graph-render
  []
  [:div
   {:id "graph"}

   [:svg
    {:width  graph-width
     :height graph-height}]])

(defn graph-did-mount
  []
  (circles-did-mount))

(defn graph-did-update
  []
  (circles-did-update))

(defn graph
  []
  (reagent/create-class
   {:reagent-render       #(graph-render)
    :component-did-mount  #(graph-did-mount)
    :component-did-update #(graph-did-update)}))

(defn home-panel
  []
  (fn []
    (let [logged-user (rf/subscribe [:logged-user])
          last-update (s/split (:last-update @logged-user) #" ")
          date (second last-update)
          hours (first last-update)
          data (rf/subscribe [:data])
          loading? (rf/subscribe [:loading])]
      (if @loading?
        [:div.ui.active.dimmer
         [:div.ui.loader]]
        [:div
         [:div.ui.container {:style {:margin-top "1em"}}
          [common/page-title "carter"]
          [:div
           {:style {:padding-top "2em" :text-align "center"}}
           [:div {:style {:padding-bottom "1em" :text-align "center"}}
            [:div.ui.compact.blue.message
             [:h3.ui.blue.header "Hello " (:screen-name @logged-user) "!"]]]
           [:div.ui.labeled.mini.input
            {:style {:padding-right ".5em"}}
            [:div.ui.basic.segment
             [:p
              "This graph was updated on " date ", at " hours
              ". Would you like to "
              [:a {:style {:cursor "pointer"}
                   :on-click #(rf/dispatch [:get-tweets])}
               "refresh it"] "?"]]]]
          (when-not (empty? @data)
            [:div
             {:style {:padding "1em" :text-align "center"}}
             [graph]])]]))))
