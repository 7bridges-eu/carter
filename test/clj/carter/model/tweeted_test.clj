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

(ns carter.model.tweeted-test
  (:require [carter.model.tweeted :as model]
            [carter.services.orientdb :as db]
            [midje.sweet :refer :all]))

(facts "Testing Tweeted model"
       (fact "find-by-rid requires rid as a parameter"
             (with-redefs [db/query! (fn [query params] params)]
               (let [rid "#21:0"]
                 (model/find-by-rid rid) => {:rid "#21:0"})))
       (fact "create requires a map (e.g.: {:from '#21:0' :to '#22:0' :logged_user_id 'test'}) as a parameter"
             (with-redefs [db/execute! (fn [query params] [])]
               (let [params {:from "#21:0" :to "#22:0" :logged_user_id "test"}]
                 (model/create params) => [])))
       (fact "delete requires a map (e.g.: {:from '#21:0' :to '#22:0'}) as a parameter"
             (with-redefs [db/execute! (fn [query params] params)]
               (let [params {:from "#21:0" :to "#22:0"}]
                 (model/delete params) => {:from "#21:0" :to "#22:0"}))))
