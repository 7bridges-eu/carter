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
  "select loggeduser.screen_name, user.screen_name, tweet.text, hashtag.name
   from (
    MATCH {class: Hashtag, as: hashtag,
           where: (@rid in
            (select hashtag.@rid from
              (select hashtag, tweets from (
                           select hashtag as hashtag,
                                  count(tweet) as tweets,
                                  tweet.created_at as dt from (
                             MATCH {class: Hashtag, as: hashtag}
                             .inE('Has'){as: has, where: (logged_user_id = %s)}
                             .outV('Tweet'){as: tweet}
                             RETURN hashtag, tweet)
                           group by hashtag
                           order by dt desc)
                          order by tweets desc
                          limit 10)))}
  .inE('Has'){as: has, where: (logged_user_id = %s)}
  .outV('Tweet'){as: tweet}
  .inE('Tweeted'){as: tweeted, where: (logged_user_id = %s)}
  .outV('User'){as: user}
  .outE('Tweeted'){where: (logged_user_id = %s)}
  .inV('Tweet')
  .inE('Sees')
  .outV('LoggedUser'){as: loggeduser}
  return hashtag, tweet, user, loggeduser)
  where loggeduser.id != user.id")

(defn graph-data
  "Get the data necessary to create the relations graph for `logged-user-id`."
  [logged-user-id]
  (let [formatted-query (format graph-query logged-user-id logged-user-id
                                logged-user-id logged-user-id)]
    (db/query! formatted-query)))
