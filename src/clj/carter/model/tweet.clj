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

(ns carter.model.tweet
  (:require [carter.services.orientdb :as db]))

(def select-queries
  {:find-all "select from Tweet"
   :find-by-rid "select from Tweet where @rid = :rid"
   :find-by-id "select from Tweet where id = :id"
   :find-by-hashtag "select tweet.@rid as rid, tweet.id as id,
                            tweet.text as text, tweet.created_at as created_at
                     from (
                       MATCH {class: Hashtag, as: hashtag,
                              where: (@rid = :rid)}
                       .inE('Has'){as: has}
                       .bothV(){as: tweet,
                                where: ($matched.hashtag != $currentMatch)}
                       RETURN tweet)"
   :find-by-hashtag-user "select tweet.@rid as rid, tweet.id as id,
                          tweet.text as text, tweet.created_at as created_at,
                          tweeted.username as username
                          from (
                            MATCH {class: Hashtag, as: hashtag,
                                   where: (@rid = :rid)}
                            .inE('Has'){as: has}
                            .outV(){as: tweet,
                                    where: ($matched.hashtag != $currentMatch)}
                            .in('Tweeted'){as: tweeted}
                            .out(){as: user,
                                   where: ($matched.hashtag != $currentMatch)}
                            RETURN tweet, tweeted)"
   :find-by-user "select tweet.@rid as rid, tweet.id as id,
                  tweet.text as text, tweet.created_at as created_at
                  from (
                    MATCH {class: User, as: user,
                           where: (@rid = :rid)}
                   .outE('Tweeted'){as: tweeted}
                   .bothV(){as: tweet,
                            where: ($matched.user != $currentMatch)}
                   RETURN tweet)"})

(def update-queries
  {:update-by-rid "update :rid set id = :id, text = :text,
                   created_at = :created_at"})

(defn find-all
  "Retrieve all the Tweet vertexes."
  []
  (let [query (:find-all select-queries)]
    (db/query! query)))

(defn- find-by
  [find-key params]
  (let [query (get select-queries find-key)]
    (db/query! query params)))

(defn find-by-rid
  "Find the Tweet vertex by `rid`."
  [rid]
  (first (find-by :find-by-rid {:rid rid})))

(defn find-by-id
  "Find the Tweet vertex by `id`."
  [id]
  (first (find-by :find-by-id {:id id})))

(defn find-by-hashtag
  "Find Tweet vertexes by `hashtag`. `hashtag` is a rid."
  [hashtag]
  (find-by :find-by-hashtag {:rid hashtag}))

(defn find-by-hashtag-user
  "Find Tweet vertexes by `hashtag`, retrieving the username of the related
  user. `hashtag` is a rid."
  [hashtag]
  (find-by :find-by-hashtag-user {:rid hashtag}))

(defn find-by-user
  "Find Tweet vertexes by `user`. `user` is a rid."
  [user]
  (find-by :find-by-user {:rid user}))

(defn create
  "Create a Tweet vertex.
  `params` is a map with :id, :text and :created_at keys.
  :id and :text are strings, :created_at is a date."
  [params]
  (db/insert! "Tweet" params))

(defn update-by-rid
  "Update a Tweet vertex.
  `params` is a map with :rid, :id, :text and :created_at keys."
  [params]
  (let [query (:update-by-rid update-queries)]
    (db/execute! query params)))

(defn delete
  "Delete a Tweet by `rid`."
  [rid]
  (db/delete! rid))
