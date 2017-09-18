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

(ns carter.api.resources.logged-user-test
  (:require [carter.api.resources.logged-user :as r]
            [carter.model.logged-user :as m]
            [midje.sweet :refer :all]))

(facts "Test logged-user API resource"
       (fact "get-data accepts a user-id"
             (with-redefs [m/find-by-id (fn [s] s)]
               (let [params {:id "1"}]
                 (r/get-data params) => (contains {:id "1"})))))
