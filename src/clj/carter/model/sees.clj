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

(ns carter.model.sees
  (:require [carter.services.orientdb :as db]))

(def select-queries
  {:find-by-rid "select from Sees where @rid = :rid"
   :find-by-from-and-to "select from Sees
                         where out = :from and in = :to"})

(def insert-queries
  {:create "create edge Sees from :from to :to"})

(def delete-queries
  {:delete "delete edge from :from to :to"})

(defn find-by-rid [rid]
  (let [query (:find-by-rid select-queries)]
    (db/query! query {:rid rid})))

(defn create
  "Create a Sees edge from two vertexes if not existing.
  `params` is a map with :from and :to keys. :from and :to are Rids."
  [params]
  (let [find-query (:find-by-from-and-to select-queries)
        {from :from to :to} params
        edge (db/execute! find-query {:from from :to to})]
    (when (empty? edge)
      (let [create-query (:create insert-queries)]
        (db/execute! create-query params)))))

(defn delete
  "Delete a Sees edge.
  `params` is a map with :from and :to keys. :from and :to are Rids."
  [params]
  (let [query (:delete delete-queries)]
    (db/execute! query params)))
