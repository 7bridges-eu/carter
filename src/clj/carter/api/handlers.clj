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

(ns carter.api.handlers
  (:require [carter.api.resources
             [hashtag :as r.hashtag]
             [logged-user :as r.logged-user]
             [tweet :as r.tweet]
             [twitter :as r.twitter]]
            [carter.api.schemas
             [hashtag :as s.hashtag]
             [logged-user :as s.logged-user]
             [tweet :as s.tweet]
             [twitter :as s.twitter]]
            [carter.services.twitter :as twitter]
            [carter.templates :refer [index-html]]
            [compojure.api.sweet :as api]
            [ring.util.http-response :as response]))

(api/defapi apis
  {:coercion nil
   :swagger
   {:ui "/swagger-ui"
    :spec "/swagger.json"
    :data
    {:info
     {:version "0.1.0"
      :title "carter APIs"
      :description "These are all the carter APIs"}
     :tags [{:name "operations"}]}}}

  (api/context
   "/api/hashtag" []
   :tags ["operations"]
   (api/GET "/tweet" req
            :return [s.hashtag/tweet-count-response]
            :summary "returns the last tweet-count tweets"
            (let [logged-user-id (get-in req [:cookies "user-id" :value])]
              (response/ok
               (r.hashtag/find-top-10-hashtags logged-user-id)))))

  (api/context
   "/api/tweet" [hashtag]
   :tags ["operations"]
   (api/GET "/hashtag/:hashtag" [hashtag]
            :return [s.tweet/tweet-response]
            :summary "returns all the tweets for a given hashtag"
            (response/ok (r.tweet/get-by-hashtag hashtag)))
   (api/POST "/user" req
             :return [s.tweet/tweet-user-response]
             :body [body s.tweet/Body]
             :summary "save user timeline tweets"
             (let [{:keys [tweet-count]} body
                   logged-user-id (get-in req [:cookies "user-id" :value])]
               (response/ok
                (r.twitter/save-user-tweets logged-user-id tweet-count)))))

  (api/context
   "/api/twitter" []
   :tags ["operations"]
   (api/GET "/user-approval" []
            :return [s.twitter/approval-response]
            :summary "returns the Twitter user approval URI"
            (let [oauth-token (twitter/get-request-token)]
              (response/found
               (str "https://api.twitter.com/oauth/authenticate?oauth_token="
                    oauth-token)))))

  (api/context
   "/api/logged-user" []
   :tags ["operations"]
   (api/GET "/data" req
            :return [s.logged-user/user-response]
            :summary "returns logged user data"
            (let [logged-user-id (get-in req [:cookies "user-id" :value])]
              (response/ok (r.logged-user/get-data logged-user-id))))))
