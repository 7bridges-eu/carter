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

(ns carter.api.resources.twitter-test
  (:require [carter.api.resources.twitter :as t]
            [carter.api.resources
             [hashtag :as hashtag]
             [tweet :as tweet]
             [user :as user]]
            [carter.model
             [has :as has]
             [logged-user :as logged-user]
             [sees :as sees]
             [tweeted :as tweeted]]
            [midje.sweet :refer :all]
            [carter.services.twitter :as twitter]))

(def twitter-date "Thu Aug 31 05:00:09 +0000 2017")
(def orient-date "2017-08-31 07:00:09")

(def tweet
  {:id "1"
   :text "test tweet"
   :created_at twitter-date
   :entities {:hashtags [{:text "test1"} {:text "test2"}]}
   :user {:id_str "1" :date twitter-date :age 42
          :name "test" :screen_name "Test"}})

(def processed-tweet
  {:user
   {:id_str "1", :username "test", :screen_name "Test"}
   :tweet
   {:id "1", :text "test tweet", :created_at orient-date}
   :hashtags ["test1" "test2"]})

(def tweets [tweet tweet tweet])

(def processed-tweets [processed-tweet processed-tweet processed-tweet])

(facts "Test twitter API resource"
       (fact "twitter-date->orient-date should convert 'Thu Aug 31 05:00:09 +0000 2017' to '2017-08-31 07:00:09'"
             (t/twitter-date->orient-date twitter-date) => orient-date)
       (fact "has-hashtags? should return true if tweet has hashtags"
             (t/has-hashtags? tweet))
       (fact "get-user-info should return a map like {:id 'id' :username 'username' :screen_name 'screen_name'}"
             (t/get-user-info tweet) =>
             {:id "1" :username "test" :screen_name "Test"})
       (fact "get-tweet-info should return a map like {:id 'id' :text 'text' :created_at created_at}"
             (t/get-tweet-info tweet) =>
             {:id "1" :text "test tweet" :created_at orient-date})
       (fact "get-hashtags should return a vector like ['test1' 'test2']"
             (t/get-hashtags tweet) => ["test1" "test2"])
       (fact "process-tweet should return a map with {:user user-info :tweet tweet-info :hashtags hashtags}"
             (with-redefs [t/get-user-info (fn [t] t)
                           t/get-tweet-info (fn [t] t)
                           t/get-hashtags (fn [t] t)]
               (t/process-tweet tweet) =>
               {:user tweet :tweet tweet :hashtags tweet}))
       (fact "save-tweet should accept an id and a processed tweet"
             (with-redefs [hashtag/save-hashtags (fn [t] t)
                           user/save-user (fn [t] t)
                           tweet/save-tweet (fn [t] t)
                           logged-user/find-by-id (fn [t] t)
                           sees/create (fn [t] t)
                           tweeted/create (fn [t] t)
                           has/create (fn [t] t)]
               (t/save-tweet "test" processed-tweet) =>
               (contains {:from anything :to anything})))
       (fact "save-user-tweets should accept and id and tweet-count"
             (with-redefs [twitter/get-home-tweets (fn [tweet-count]
                                                     tweets)
                           t/has-hashtags? (fn [ts] ts)
                           t/process-tweet (fn [t] t)
                           t/save-tweet (fn [id t] t)]
               (t/save-user-tweets "test" 10) => tweets))
       (fact "save-first-150-tweets should accept no parameters"
             (with-redefs [twitter/logged-user-id (fn [])
                           t/save-user-tweets (fn [id n])]
               (t/save-first-150-tweets) => nil)))
