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

(ns carter.model.hashtag-test
  (:require [carter.model.hashtag :as model]
            [carter.services.orientdb :as db]
            [midje.sweet :refer :all]))

(facts "Testing Hashtag model"
       (fact "find-all requires no parameters"
             (with-redefs [db/query! (fn [query] [])]
               (model/find-all) => []))
       (fact "find-by-rid requires rid as a parameter"
             (with-redefs [db/query! (fn [query params] [params])]
               (let [rid "#21:0"]
                 (model/find-by-rid rid) => {:rid "#21:0"})))
       (fact "find-by-name requires name as a parameter"
             (with-redefs [db/query! (fn [query params] [params])]
               (let [name "test"]
                 (model/find-by-name name) => {:name "test"})))
       (fact "find-by-user requires rid as a parameter"
             (with-redefs [db/query! (fn [query params] [params])]
               (let [rid "#21:0"]
                 (model/find-by-user rid) => [{:rid "#21:0"}])))
       (fact "find-tweet-count requires tweet-count and logged-user-id as parameters"
             (with-redefs [db/query! (fn [query] query)]
               (model/find-tweet-count 1 "test") => truthy))
       (fact "create requires a map (e.g.: {:name '#test'}) as a parameter"
             (with-redefs [db/insert! (fn [class params] params)]
               (let [params {:name "#test"}]
                 (model/create params) => {:name "#test"})))
       (fact "update-by-rid requires a map (e.g.: {:rid '#21:0' :name '#test'}) as parameter"
             (with-redefs [db/execute! (fn [query params] params)]
               (let [params {:rid "#21:0" :name "#test"}]
                 (model/update-by-rid params) => {:rid "#21:0" :name "#test"})))
       (fact "delete requires rid as a parameter"
             (with-redefs [db/delete! (fn [params] params)]
               (let [rid "#21:0"]
                 (model/delete rid) => "#21:0"))))
