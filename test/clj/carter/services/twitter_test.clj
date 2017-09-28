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

(ns carter.services.twitter-test
  (:require [carter.model.logged-user :as lu]
            [carter.services.twitter :as t]
            [midje.sweet :refer :all]
            [twitter.api.restful :as r]))

(facts "Twitter API interrogation"
       (fact "home-tweets requires one argument 'tweet-count'"
             (with-redefs [lu/find-by-id (fn [id] [])
                           r/statuses-home-timeline
                           (fn [& args]
                             (let [credentials (second (rest (rest args)))
                                   tweet-count (:count credentials)]
                               tweet-count))]
               (let [tweet-count 1]
                 (t/home-tweets "test "tweet-count) => 1))))
