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

(ns carter.model.hashtag
  (:require [carter.services.orientdb :as db]))

(def select-queries
  {:find-all "select from Hashtag"
   :find-by-rid "select from Hashtag where @rid = :rid"
   :find-by-name "select from Hashtag where name = :name"
   :find-by-user "select hashtag.@rid as rid, hashtag.name as name
                  from (
                    MATCH {class: User, as: user,
                           where: (@rid = :rid)}
                     .out('Tweeted'){as: tweeted}
                     .outE('Has'){as: has}
                     .inV(){as: hashtag,
                            where: ($matched.user != $currentMatch)}
                    RETURN hashtag)"
   :find-tweet-count "select hashtag.name as hashtag, count(tweet) as tweets,
                      tweet.created_at as dt from (
                        MATCH {class: Hashtag, as: hashtag}
                         .inE('Has'){as: has, where: (logged_user_id = %s)}
                         .outV('Tweet'){as: tweet}
                        RETURN hashtag, tweet)
                      group by hashtag
                      order by dt desc
                      limit %s"})

(def update-queries
  {:update-by-rid "update :rid set name = :name"})

(defn find-all
  "Retrieve all the Hashtag vertexes."
  []
  (let [query (:find-all select-queries)]
    (db/query! query)))

(defn- find-by
  [find-key params]
  (let [query (get select-queries find-key)]
    (db/query! query params)))

(defn find-by-rid
  "Find the Hashtag vertex by `rid`."
  [rid]
  (first (find-by :find-by-rid {:rid rid})))

(defn find-by-name
  "Find the Hashtag vertex by `name`."
  [name]
  (first (find-by :find-by-name {:name name})))

(defn find-by-user
  "Find the Hashtag vertexes used by `user`. `user` is a rid."
  [user]
  (find-by :find-by-user {:rid user}))

(defn find-tweet-count
  "For the home timeline of `logged-user-id`, return `tweet-count` tweets
  for every hashtag."
  [tweet-count logged-user-id]
  (let [query (:find-tweet-count select-queries)
        formatted-query (format query logged-user-id tweet-count)]
    (db/query! formatted-query)))

(defn create
  "Create a Hashtag vertex.
  `params` is a map with :name key. :name is a string."
  [params]
  (db/insert! "Hashtag" params))

(defn update-by-rid
  "Update a Hashtag vertex.
  `params` is a map with :rid and :name key."
  [params]
  (let [query (:update-by-rid update-queries)]
    (db/execute! query params)))

(defn delete
  "Delete a Hashtag by `rid`."
  [rid]
  (db/delete! rid))
