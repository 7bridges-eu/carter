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

(ns carter.graphs
  (:require [cljsjs.d3]
            [re-frame.core :as rf]
            [reagent.core :as reagent]))

(def graph-width 850)
(def graph-height 850)

;;; Circles graph
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
  (-> (js/d3.select "#circles-graph svg")
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
  (-> (js/d3.select "#circles-graph svg")
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
  (-> (js/d3.select "#circles-graph svg")
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
    (-> (js/d3.select "#circles-graph svg")
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
  (-> (js/d3.select "#circles-graph svg")
      (.append "g")
      (.attr "class" "circles"))
  (circles-did-update))

(defn graph-render
  [graph-name]
  [:div
   {:id graph-name}

   [:svg
    {:width  graph-width
     :height graph-height}]])

(defn graph-did-mount
  []
  (circles-did-mount))

(defn graph-did-update
  []
  (circles-did-update))

(defn circles-graph
  []
  (reagent/create-class
   {:display-name         "circles-graph"
    :reagent-render       #(graph-render "circles-graph")
    :component-did-mount  #(graph-did-mount)
    :component-did-update #(graph-did-update)}))

;;; Nodes graph
(defn initialize-svg
  []
  (-> (js/d3.select "#nodes-graph svg")
      (.append "defs")
      (.append "marker")
      (.attr "id" "arrowhead")
      (.attr "viewBox" "-0 -5 10 10")
      (.attr "refX" 13)
      (.attr "refY" 0)
      (.attr "orient" "auto")
      (.attr "markerWidth" 13)
      (.attr "markerHeight" 13)
      (.attr "xoverflow" "visible")
      (.append "svg:path")
      (.attr "d" "M 0,-5 L 10 ,0 L 0,5")
      (.attr "fill" "#999")
      (.style "stroke" "none")))

(defn simulation
  []
  (-> (js/d3.forceSimulation)
      (.force "link" (-> (js/d3.forceLink)
                         (.id (fn [d] (.-id d)))
                         (.distance 300)))
      (.force "charge" (js/d3.forceManyBody))
      (.force "center"
              (js/d3.forceCenter (/ graph-width 2) (/ graph-height 2)))))

(defn init-links
  [links]
  (-> (js/d3.select "#nodes-graph svg")
      (.selectAll ".link")
      (.data links)
      .enter
      (.append "line")
      (.attr "class" "link")
      (.attr "marker-end" "url(#arrowhead)")))

(defn append-link-title
  [links]
  (-> links
      (.append "title")
      (.text (fn [d] (.-type d))))
  links)

(defn build-links
  [links]
  (-> links
      init-links
      append-link-title))

(defn build-edge-paths
  [links]
  (-> (js/d3.select "#nodes-graph svg")
      (.selectAll ".edgepath")
      (.data links)
      .enter
      (.append "path")
      (.attr "class" "edgepath")
      (.attr "fill-opacity" 0)
      (.attr "stroke-opacity" 0)
      (.attr "id" (fn [d i] (str "edgepath" i)))
      (.style "pointer-events" "none")))

(defn init-edge-labels
  [links]
  (-> (js/d3.select "#nodes-graph svg")
      (.selectAll ".edgelabel")
      (.data links)
      .enter
      (.append "text")
      (.style "pointer-events" "none")
      (.attr "class" "edgelabel")
      (.attr "id" (fn [d i] (str "edgelabel" i)))
      (.attr "font-size" 10)
      (.attr "fill" "#aaa")))

(defn add-edge-labels-text-path
  [edgelabels]
  (-> edgelabels
      (.append "textPath")
      (.attr "xlink:href" (fn [d i] (str "#edgepath" i)))
      (.style "text-anchor" "middle")
      (.style "pointer-events" "none")
      (.attr "startOffset" "50%")
      (.text (fn [d] (.-type d))))
  edgelabels)

(defn build-edge-labels
  [links]
  (-> links
      init-edge-labels
      add-edge-labels-text-path))

(defn init-nodes
  [nodes]
  (-> (js/d3.select "#nodes-graph svg")
      (.selectAll ".node")
      (.data nodes)
      .enter
      (.append "g")
      (.attr "class" "node")))

(defn append-circle
  [nodes]
  (let [nodes-idx {"LoggedUser" 5 "User" 10
                   "Tweet" 15 "Hashtag" 19}]
    (-> nodes
        (.append "circle")
        (.attr "r" 15)
        (.style "fill"
                (fn [d]
                  (nth (.-schemeCategory20c js/d3)
                       (get nodes-idx (.-label d)))))))
  nodes)

(defn append-title
  [nodes]
  (-> nodes
      (.append "title")
      (.text (fn [d] (.-id d))))
  nodes)

(defn append-text
  [nodes]
  (-> nodes
      (.append "text")
      (.attr "dy" -3)
      (.text (fn [d] (str (.-name d) ": " (.-label d)))))
  nodes)

(defn build-nodes
  [nodes]
  (-> nodes
      init-nodes
      append-circle
      append-title
      append-text))

(defn ticked
  []
  (let [svg (js/d3.select "#nodes-graph svg")
        links (.selectAll svg ".link")
        nodes (.selectAll svg ".node")
        edgepaths (.selectAll svg ".edgepath")
        edgelabels(.selectAll svg ".edgelabel")]
    (-> links
        (.attr "x1" (fn [d] (.-x (.-source d))))
        (.attr "y1" (fn [d] (.-y (.-source d))))
        (.attr "x2" (fn [d] (.-x (.-target d))))
        (.attr "y2" (fn [d] (.-y (.-target d)))))
    (-> nodes
        (.attr "transform" (fn [d]
                             (.log js/console d)
                             (str "translate(" (.-x d) "," (.-y d) ")"))))
    (-> edgepaths
        (.attr "d" (fn [d]
                     (str "M " (.-x (.-source d)) " " (.-y (.-source d))
                          " L " (.-x (.-target d)) " " (.-y (.-target d))))))
    (-> edgelabels
        (.attr "transform" (fn [d]
                             (this-as this
                               (if (< (.-x (.-target d)) (.-x (.-source d)))
                                 (let [bbox (.getBBox this)
                                       rx (/ (+ (.-x bbox) (.-width bbox)) 2)
                                       ry (/ (+ (.-y bbox) (.-height bbox)) 2)]
                                   (str "rotate(180 " rx " " ry ")"))
                                 "rotate(0)")))))))

(defn nodes-update
  []
  (let [nodes-links @(rf/subscribe [:nodes-links])]
    (when-not (empty? nodes-links)
      (let [{ns :nodes ls :links} nodes-links
            nodes-js (clj->js ns)
            links-js (clj->js ls)
            sim (-> (simulation)
                    (.nodes nodes-js)
                    (.on "tick" ticked))]
        (build-links links-js)
        (build-nodes nodes-js)
        (build-edge-paths links-js)
        (build-edge-labels links-js)

        (-> sim
            (.force "link")
            (.links links-js))))))

(defn nodes-enter
  []
  (initialize-svg)
  (nodes-update))

(defn nodes-exit
  []
  (let [nodes-links @(rf/subscribe [:nodes-links])]
    (when-not (empty? nodes-links)
      (let [{ns :nodes ls :links} nodes-links
            nodes (clj->js ns)
            links (clj->js ls)]
        (-> (js/d3.select "#nodes-graph svg")
            (.selectAll ".node")
            (.data nodes)
            .exit
            .remove)
        (-> (js/d3.select "#nodes-graph svg")
            (.selectAll ".link")
            (.data links)
            .exit
            .remove)))))

(defn nodes-did-update
  []
  (nodes-enter)
  (nodes-exit))

(defn nodes-did-mount
  []
  (-> (js/d3.select "#nodes-graph svg")
      (.append "g")
      (.attr "class" "nodes"))
  (nodes-did-update))

(defn nodes-graph-did-mount
  []
  (nodes-did-mount))

(defn nodes-graph-did-update
  []
  (nodes-did-update))

(defn nodes-graph
  []
  (reagent/create-class
   {:display-name         "nodes-graph"
    :reagent-render       #(graph-render "nodes-graph")
    :component-did-mount  #(nodes-graph-did-mount)
    :component-did-update #(nodes-graph-did-update)}))
