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

(ns carter.api.handlers-test
  (:require [carter.api.resources
             [graph :as g]
             [hashtag :as h]
             [logged-user :as lu]
             [tweet :as t]
             [twitter :as tw]]
            [carter.services
             [http :as http]
             [twitter :as twitter]]
            [cheshire.core :as json]
            [midje.sweet :refer :all]
            [ring.mock.request :as mock]))

(facts "Test API endpoints"
       (fact "Testing find top 10 hashtags"
             (with-redefs [twitter/logged-user-id (fn [] [])
                           h/find-top-10-hashtags (fn [id] id)]
               (let [request (mock/request :get "/api/hashtag/tweet")
                     response (http/app request)]
                 (:status response) => 200)))
       (fact "Testing find tweet by hashtag endpoint"
             (with-redefs [t/get-by-hashtag (fn [s] s)]
               (let [request (mock/request :get "/api/tweet/hashtag/test")
                     response (http/app request)]
                 (:status response) => 200)))
       (fact "Testing save user timeline tweets"
             (with-redefs [twitter/logged-user-id (fn [] [])
                           tw/save-user-tweets (fn [id n] n)]
               (let [body (json/generate-string {:tweet-count 1})
                     request (-> (mock/request :post "/api/tweet/user" body)
                                 (mock/content-type "application/json"))
                     response (http/app request)]
                 (:status response) => 200)))
       (fact "Testing get Twitter user approval URI endpoint"
             (with-redefs [twitter/get-request-token (fn [])]
               (let [request (mock/request :get "/api/twitter/user-approval")
                     response (http/app request)]
                 (:status response) => 302)))
       (fact "Testing get logged-user data endpoint"
             (with-redefs [twitter/logged-user-id (fn [] [])
                           lu/get-data (fn [id] id)]
               (let [request (mock/request :get "/api/logged-user/data")
                     response (http/app request)]
                 (:status response) => 200)))
       (fact "Testing get graph data endpoint"
             (with-redefs [twitter/logged-user-id (fn [] [])
                           g/graph-data (fn [id] id)]
               (let [request (mock/request :get "/api/graph/data")
                     response (http/app request)]
                 (:status response) => 200))))
