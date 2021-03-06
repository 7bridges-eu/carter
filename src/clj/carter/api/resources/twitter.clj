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

(ns carter.api.resources.twitter
  (:require [carter.api.resources
             [hashtag :as hashtag]
             [tweet :as tweet]
             [user :as user]]
            [carter.dates :as d]
            [carter.model
             [has :as has]
             [logged-user :as logged-user]
             [sees :as sees]
             [tweeted :as tweeted]]
            [carter.services.twitter :as twitter]))

(defn has-hashtags?
  "Check if the `tweet` has hashtags."
  [tweet]
  (let [hashtags (get-in tweet [:entities :hashtags] [])]
    (> (count hashtags) 0)))

(defn get-user-info
  "Extract user id, username and screen_name from `tweet`."
  [tweet]
  (let [id (get-in tweet [:user :id_str])
        username (get-in tweet [:user :name])
        screen_name (get-in tweet [:user :screen_name])]
    {:id id :username username :screen_name screen_name}))

(defn get-tweet-info
  "Extract tweet id, text and created_at from `tweet`."
  [tweet]
  (let [{:keys [id text created_at]} tweet
        created_at (d/twitter-date->orient-date created_at)]
    {:id id :text text :created_at created_at}))

(defn get-hashtags
  "Extract the hashtags form `tweet`."
  [tweet]
  (->> (get-in tweet [:entities :hashtags])
       (mapv :text)))

(defn process-tweet
  "Process `tweet` to obtain user and tweet information and hashtags."
  [tweet]
  {:user (get-user-info tweet)
   :tweet (get-tweet-info tweet)
   :hashtags (get-hashtags tweet)})

(defn create-tweeted
  "Create Tweeted edge from `user` to `tweet`. Also set `logged-user-id`
  on the edge."
  [user tweet logged-user-id]
  (tweeted/create {:from           (:_rid user)
                   :to             (:_rid tweet)
                   :logged_user_id logged-user-id}))

(defn create-has
  "Create Has edge from `tweet` to `hashtag`. Also set `logged-user-id`
  on the edge."
  [tweet hashtag logged-user-id]
  (has/create {:from           (:_rid tweet)
               :to             (:_rid hashtag)
               :logged_user_id logged-user-id}))

(defn create-sees
  "Create Sees edge from `logged-user` to `tweet`."
  [logged-user tweet]
  (sees/create {:from (:_rid logged-user) :to (:_rid tweet)}))

(defn save-tweet
  "Save `tweet` creating the necessary vertexes and edges.
  Add `logged-user-id` to the edges."
  [logged-user-id tweet]
  (let [hashtags    (hashtag/save-hashtags (:hashtags tweet))
        user        (user/save-user (:user tweet))
        tweet       (tweet/save-tweet (:tweet tweet))
        logged-user (logged-user/find-by-id logged-user-id)]
    (create-tweeted user tweet logged-user-id)
    (doall (map #(create-has tweet % logged-user-id) hashtags))
    (create-sees logged-user tweet)))

(defn update-last-update
  "Update the last_update property of the LoggedUser vertex identified
  by `logged-user-id`."
  [logged-user-id]
  (let [user (logged-user/find-by-id logged-user-id)
        params {:rid (:_rid user)
                :id (:id user)
                :username (:username user)
                :screen_name (:screen_name user)
                :last_update (d/java-date->orient-date (java.util.Date.))
                :oauth_token (:oauth_token user)
                :oauth_token_secret (:oauth_token_secret user)}]
    (logged-user/update-by-rid params)))

(defn save-user-tweets
  "Get `user` tweets up to `tweet-count` and save them."
  [logged-user-id tweet-count]
  (let [tweets (twitter/home-tweets logged-user-id tweet-count)
        save-fn #(save-tweet logged-user-id %)]
    (->> tweets
         (filter has-hashtags?)
         (map process-tweet)
         (map save-fn)
         doall)
    (update-last-update logged-user-id)))

(defn save-first-150-tweets
  "Save the first 150 tweets in the timeline of the logged user identified
  by `logged-user-id`."
  [logged-user-id]
  (save-user-tweets logged-user-id 150))
