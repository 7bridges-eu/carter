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

(ns carter.model.graph
  (:require [carter.services.orientdb :as db]))

(def graph-query
  "SELECT loggeduser.@rid as loggeduser_rid,
       loggeduser.screen_name as loggeduser_name,
       user.@rid as user_rid, user.screen_name as user_name,
       tweet.@rid as tweet_rid, tweet.text as tweet_text,
       hashtag.@rid as hashtag_rid, hashtag.name as hashtag_name
   FROM (
    MATCH {class: Hashtag, as: hashtag,
           where: (@rid in
            (SELECT H.@rid as hashtag,
               list(T).size() as tweets FROM (
                MATCH {class: Hashtag, as: H}
                 .inE('Has'){where: (logged_user_id = %s)}
                 .outV('Tweet'){as: T}
                RETURN H, T)
                GROUP BY H.@rid
                ORDER BY T.created_at desc, tweets desc
                LIMIT 10))}
    .inE('Has'){as: has, where: (logged_user_id = %s)}
    .outV('Tweet'){as: tweet}
    .inE('Tweeted'){as: tweeted, where: (logged_user_id = %s)}
    .outV('User'){as: user}
    .outE('Tweeted'){where: (logged_user_id = %s)}
    .inV('Tweet')
    .inE('Sees')
    .outV('LoggedUser'){as: loggeduser}
    RETURN hashtag, tweet, user, loggeduser)
  WHERE loggeduser.id != user.id")

(defn graph-data
  "Get the data necessary to create the relations graph for `logged-user-id`."
  [logged-user-id]
  (let [formatted-query (format graph-query logged-user-id logged-user-id
                                logged-user-id logged-user-id)]
    (db/query! formatted-query)))
