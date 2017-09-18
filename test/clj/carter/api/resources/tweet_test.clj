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

(ns carter.api.resources.tweet-test
  (:require [carter.api.resources.tweet :as r]
            [carter.model.hashtag :as h]
            [carter.model.tweet :as m]
            [midje.sweet :refer :all]))

(facts "Test tweet API resource"
       (fact "get-by-hashtag accepts a string which is the hashtag name"
             (with-redefs [h/find-by-name (fn [s] {:_rid s})
                           m/find-by-hashtag (fn [params] params)]
               (let [hashtag "test"]
                 (r/get-by-hashtag hashtag) => "test")))
       (fact "save-tweet accepts a map which must have :id in it"
             (with-redefs [m/find-by-id (fn [s] nil)
                           m/create (fn [params] params)]
               (let [params {:id "1"}]
                 (r/save-tweet params) => {:id "1"})))
       (fact "save-tweets accepts a map which must have :tweet in it"
             (with-redefs [r/save-tweet (fn [params] params)]
               (let [params [{:tweet ["test1" "test2"]}]]
                 (r/save-tweets params) => [["test1" "test2"]]))))
