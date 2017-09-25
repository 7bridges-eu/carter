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

(ns carter.api.resources.graph
  (:require [carter.model.graph :as model]))

(defn logged-user-node
  "Create LoggedUser node from `data`."
  [data]
  (let [{rid :loggeduser_rid screen-name :loggeduser_name} (first data)]
    {:id rid :name screen-name :label "LoggedUser"}))

(defn conj-if-absent
  "Conj `eleme` in `coll` only when absent."
  [coll elem]
  (if (some #(= elem %) coll)
    coll
    (conj coll elem)))

(defn user-nodes
  "Create User nodes from `data`."
  [data]
  (reduce
   (fn [res r]
     (let [{rid :user_rid screen-name :user_name} r
           entry {:id rid :name screen-name :label "User"}]
       (conj-if-absent res entry)))
   []
   data))

(defn tweet-nodes
  "Create Tweet nodes from `data`."
  [data]
  (reduce
   (fn [res r]
     (let [{rid :tweet_rid text :tweet_text} r
           entry {:id rid :name (subs text 0 15) :label "Tweet"}]
       (conj-if-absent res entry)))
   []
   data))

(defn hashtag-nodes
  "Create Hashtag nodes from `data`."
  [data]
  (reduce
   (fn [res r]
     (let [{rid :hashtag_rid hashtag :hashtag_name} r
           entry {:id rid :name hashtag :label "Hashtag"}]
       (conj-if-absent res entry)))
   []
   data))

(defn nodes
  "Create the necessary nodes from `data`."
  [data]
  (->> (concat (vector (logged-user-node data))
               (user-nodes data)
               (tweet-nodes data)
               (hashtag-nodes data))
       vec))

(defn follows-links
  "Create FOLLOW links (LoggedUser->User) from `data`."
  [data]
  (reduce
   (fn [res r]
     (let [{logged-user :loggeduser_rid user :user_rid} r
           entry {:source logged-user :target user :type "FOLLOWS"}]
       (conj-if-absent res entry)))
   []
   data))

(defn tweeted-links
  "Create TWEETED links (User->Tweet) from `data`."
  [data]
  (reduce
   (fn [res r]
     (let [{user :user_rid tweet :tweet_rid} r
           entry {:source user :target tweet :type "TWEETED"}]
       (conj-if-absent res entry)))
   []
   data))

(defn has-links
  "Create HAS links (Tweet->Hashtag) from `data`."
  [data]
  (reduce
   (fn [res r]
     (let [{tweet :tweet_rid hashtag :hashtag_rid} r
           entry {:source tweet :target hashtag :type "HAS"}]
       (conj-if-absent res entry)))
   []
   data))

(defn links
  "Create the necessary links from `data`."
  [data]
  (->> (concat (follows-links data)
               (tweeted-links data)
               (has-links data))
       vec))

(defn graph-data
  "Create a graph-suitable data structure from `data`.
  See: http://bl.ocks.org/fancellu/2c782394602a93921faff74e594d1bb1#graph.json"
  [logged-user-id]
  (let [data (model/graph-data logged-user-id)
        nodes (nodes data)
        links (links data)]
    (hash-map :nodes nodes :links links)))
