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

(ns carter.api.resources.hashtag
  (:require [carter.model.hashtag :as model]))

(defn save-hashtag
  "Create Hashtag vertex with `params`.
  The vertex is created only if a Hashtag with the same :name is not present."
  [params]
  (let [name (:name params)
        hashtag (model/find-by-name name)]
    (if (nil? hashtag)
      (model/create params)
      hashtag)))

(defn save-hashtags
  "Save the hashtags identified by :hashtags in `hashtags`."
  [hashtags]
  (let [names (map #(hash-map :name %) hashtags)]
    (doall (map save-hashtag names))))

(defn hashtags-with-tweets-count
  "For `tweet-count` tweets, return a sequence of maps with the number of tweets
  for each hashtag from `logged-user-id` home timeline."
  [logged-user-id tweet-count]
  (let [result (model/find-tweet-count tweet-count logged-user-id)]
    (if (empty? result)
      []
      (map #(select-keys % [:hashtag :tweets]) result))))
