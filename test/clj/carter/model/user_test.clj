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

(ns carter.model.user-test
  (:require [carter.model.user :as model]
            [carter.services.orientdb :as db]
            [midje.sweet :refer :all]))

(facts "Testing User model"
       (fact "find-all requires no parameters"
             (with-redefs [db/query! (fn [query] [])]
               (model/find-all) => []))
       (fact "find-by-rid requires rid as a parameter"
             (with-redefs [db/query! (fn [query params] [params])]
               (let [rid "#21:0"]
                 (model/find-by-rid rid) => {:rid "#21:0"})))
       (fact "find-by-id requires id as a parameter"
             (with-redefs [db/query! (fn [query params] [params])]
               (let [id "1"]
                 (model/find-by-id id) => {:id "1"})))
       (fact "find-by-username requires username as a parameter"
             (with-redefs [db/query! (fn [query params] [params])]
               (let [username "test"]
                 (model/find-by-username username) => {:username "test"})))
       (fact "find-by-hashtag requires rid as a parameter"
             (with-redefs [db/query! (fn [query params] [params])]
               (let [rid "#21:0"]
                 (model/find-by-hashtag rid) => [{:rid "#21:0"}])))
       (fact "create requires a map (e.g.: {:id 1 :username 'test' :screen_name 'Test'}) as a parameter"
             (with-redefs [db/insert! (fn [class params] params)]
               (let [params {:id 1 :username "test" :screen_name "Test"}]
                 (model/create params) => {:id 1
                                           :username "test"
                                           :screen_name "Test"})))
       (fact "update-by-rid requires a map (e.g.: {:rid '#21:0' :id 1 :username 'test' :screen_name 'Test'}) as parameter"
             (with-redefs [db/execute! (fn [query params] params)]
               (let [params {:rid "#21:0" :id 1
                             :username "test" :screen_name "Test"}]
                 (model/update-by-rid params) =>
                 {:rid "#21:0" :id 1 :username "test" :screen_name "Test"})))
       (fact "delete requires rid as a parameter"
             (with-redefs [db/delete! (fn [params] params)]
               (let [rid "#21:0"]
                 (model/delete rid) => "#21:0"))))
