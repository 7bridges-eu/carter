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

(ns carter.api.resources.hashtag-test
  (:require [carter.api.resources.hashtag :as r]
            [carter.model.hashtag :as m]
            [midje.sweet :refer :all]))

(facts "Test hashtag API resource"
       (fact "save-hashtag accepts a map which must have :name in it"
             (with-redefs [m/find-by-name (fn [s] nil)
                           m/create (fn [params] params)]
               (let [params {:name "test"}]
                 (r/save-hashtag params) => {:name "test"})))
       (fact "save-hashtags accepts a sequence of hashtags"
             (with-redefs [r/save-hashtag (fn [params] params)]
               (let [params ["test1" "test2"]]
                 (r/save-hashtags params) =>
                 [{:name "test1"} {:name "test2"}])))
       (fact "find-top-10-hashtags requires logged-user-id as parameter"
             (with-redefs [m/find-top-10-hashtags (fn [id] [])]
               (r/find-top-10-hashtags "test") => [])))
