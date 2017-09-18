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

(ns carter.api.resources.user-test
  (:require [carter.api.resources.user :as r]
            [carter.model.user :as m]
            [midje.sweet :refer :all]))

(facts "Test user API resource"
       (fact "save-user accepts a map which must have :id in it"
             (with-redefs [m/find-by-id (fn [s] nil)
                           m/create (fn [params] params)]
               (let [params {:id "1"}]
                 (r/save-user params) => {:id "1"})))
       (fact "save-users accepts a map which must have :user in it"
             (with-redefs [r/save-user (fn [params] params)]
               (let [params [{:user ["test1" "test2"]}]]
                 (r/save-users params) => [["test1" "test2"]]))))
