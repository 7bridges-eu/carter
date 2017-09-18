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

(ns carter.api.resources.tweet
  (:require [carter.model.hashtag :as h]
            [carter.model.tweet :as model]))

(defn get-by-hashtag
  "Find tweets containing `hashtag`."
  [hashtag]
  (let [hashtag (h/find-by-name hashtag)
        hashtag-rid (:_rid hashtag)]
    (model/find-by-hashtag hashtag-rid)))

(defn save-tweet
  "Create Tweet vertex with `params`.
  The vertex is created only if a Tweet with the same :id is not present."
  [params]
  (let [id (:id params)
        tweet (model/find-by-id id)]
    (if (nil? tweet)
      (model/create params)
      tweet)))

(defn save-tweets
  "Save the tweets identified by :tweet in `data`."
  [data]
  (let [tweets (map :tweet data)]
    (doall (map save-tweet tweets))))
