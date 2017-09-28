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
                         (.distance 100)))
      (.force "charge" (js/d3.forceManyBody))
      (.force "center"
              (js/d3.forceCenter (/ graph-width 2) (/ graph-height 2)))))

(defn build-links
  [links]
  (let [elements (-> (js/d3.select "#nodes-graph svg")
                     (.append "g")
                     (.attr "class" "link")
                     (.selectAll "line")
                     (.data links)
                     (.enter)
                     (.append "line")
                     (.attr "stroke-width" (fn [d] 1))
                     (.attr "stroke" (fn [d] "#000")))]
    (-> elements
        (.append "title")
        (.text (fn [d] (.-type d))))
    elements))

(defn build-nodes
  [nodes drag-start dragged drag-end]
  (let [nodes-idx {"LoggedUser" 5 "User" 10
                   "Tweet" 15 "Hashtag" 19}]
    (-> (js/d3.select "#nodes-graph svg")
        (.append "g")
        (.attr "class" "nodes")
        (.selectAll "circle")
        (.data nodes (fn [d] (.-id d)))
        (.enter)
        (.append "circle")
        (.attr "r" 25)
        (.style "fill"
                (fn [d]
                  (nth (.-schemeCategory20c js/d3)
                       (get nodes-idx (.-label d)))))
        (.call (-> (.drag js/d3)
                   (.on "start" drag-start)
                   (.on "drag" dragged)
                   (.on "end" drag-end))))))

(defn build-texts
  [nodes]
  (-> (js/d3.select "#nodes-graph svg")
      (.append "g")
      (.attr "class" "texts")
      (.selectAll "text")
      (.data nodes (fn [d] (.-id d)))
      (.enter)
      (.append "text")
      (.text (fn [d] (str (.-name d) ": " (.-label d))))
      (.attr "dx" 28)
      (.attr "dy" 10)))

(defn ticked-fn
  [nodes links texts]
  (fn [d]
    (-> links
        (.attr "x1" (fn [d] (aget d "source" "x")))
        (.attr "y1" (fn [d] (aget d "source" "y")))
        (.attr "x2" (fn [d] (aget d "target" "x")))
        (.attr "y2" (fn [d] (aget d "target" "y"))))
    (-> nodes
        (.attr "transform"
               (fn [d]
                 (str "translate(" (.-x d) "," (.-y d) ")"))))
    (-> texts
        (.attr "x" (fn [d] (.-x d)))
        (.attr "y" (fn [d] (.-y d))))))

(defn nodes-update
  []
  (let [nodes-links @(rf/subscribe [:nodes-links])]
    (when-not (empty? nodes-links)
      (let [{ns :nodes ls :links} nodes-links
            nodes-js (clj->js ns)
            links-js (clj->js ls)
            link (build-links links-js)
            text (build-texts nodes-js)
            sim (simulation)
            drag-started (fn [d]
                           (let [active (.-active (.-event js/d3))
                                 x (.-x (.-event js/d3))
                                 y (.-y (.-event js/d3))]
                             (-> sim
                                 (.alphaTarget 0.3)
                                 (.restart))
                             (aset d "fx" (.-x d))
                             (aset d "fy" (.-y d))))
            dragged (fn [d]
                      (let [active (.-active (.-event js/d3))
                            x (.-x (.-event js/d3))
                            y (.-y (.-event js/d3))]
                        (aset d "fx" x)
                        (aset d "x" x)
                        (aset d "fy" y)))
            drag-end (fn [d]
                       (-> sim
                           (.alphaTarget 0))
                       (aset d "fx" nil)
                       (aset d "fy" nil))
            node (build-nodes nodes-js drag-started dragged drag-end)
            run (fn [nodes links]
                  (-> sim
                      (.nodes nodes)
                      (.on "tick" (ticked-fn node link text))
                      (.force "link")
                      (.links links)))]
        (run nodes-js links-js)))))

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
