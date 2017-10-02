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
             [graph :as r.graph]
             [hashtag :as r.hashtag]
             [logged-user :as r.logged-user]
             [twitter :as r.twitter]]
            [carter.api.schemas
             [graph :as s.graph]
             [logged-user :as s.logged-user]
             [tweet :as s.tweet]
             [twitter :as s.twitter]]
            [carter.services.twitter :as twitter]
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
   "/api/twitter" []
   :tags ["operations"]
   (api/GET "/user-approval" []
            :return [s.twitter/approval-response]
            :summary "returns the Twitter user approval URI"
            (let [oauth-token (twitter/request-token)]
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
              (response/ok (r.logged-user/get-data logged-user-id)))))

  (api/context
   "/api/graphs" []
   :tags ["operations"]
   (api/GET "/data" req
            :return [s.graph/graph-data-response]
            :summary "returns the required data for the graphs"
            (let [logged-user-id (get-in req [:cookies "user-id" :value])]
              (let [hashtags (r.hashtag/find-top-10-hashtags logged-user-id)
                    {nodes :nodes links :links}
                    (r.graph/graph-data logged-user-id)]
                (response/ok
                 (hash-map :circles hashtags :nodes nodes :links links)))))
   (api/POST "/data" req
             :return [s.graph/graph-data-response]
             :body [body s.tweet/Body]
             :summary "saves user timeline tweets and returns data for graphs"
             (let [{:keys [tweet-count]} body
                   logged-user-id (get-in req [:cookies "user-id" :value])]
               (r.twitter/save-user-tweets logged-user-id tweet-count)
               (let [hashtags (r.hashtag/find-top-10-hashtags logged-user-id)
                     {nodes :nodes links :links}
                     (r.graph/graph-data logged-user-id)]
                 (response/ok
                  (hash-map :circles hashtags :nodes nodes :links links)))))))
