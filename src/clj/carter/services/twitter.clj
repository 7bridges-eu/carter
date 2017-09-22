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

(ns carter.services.twitter
  (:require [carter.dates :as d]
            [carter.model.logged-user :as logged-user]
            [carter.services.config :refer [config]]
            [oauth.client :as oauth]
            [twitter.api.restful :refer :all]
            [twitter.callbacks.handlers :refer :all]
            [twitter.oauth :refer :all])
  (:import twitter.callbacks.protocols.SyncSingleCallback))

(def oauth-data (atom {}))

(defn logged-user-id
  []
  (get @oauth-data :logged-user-id))

(defn get-consumer
  "OAuth consumer to interact with Twitter APIs."
  []
  (let [consumer-key (get-in config [:twitter :consumer-key])
        consumer-secret (get-in config [:twitter :consumer-secret])]
    (oauth/make-consumer consumer-key
                         consumer-secret
                         "https://twitter.com/oauth/request_token"
                         "https://twitter.com/oauth/access_token"
                         "https://twitter.com/oauth/authorize"
                         :hmac-sha1)))

(defn get-oauth-data
  "Fill `oauth-data` with the required data for OAuth operations."
  []
  (let [consumer (get-consumer)
        oauth-response (oauth/request-token consumer "")
        {:keys [oauth_token oauth_token_secret oauth_callback_confirmed]}
        oauth-response]
    (when (= oauth_callback_confirmed "true")
      (swap! oauth-data assoc :oauth-token oauth_token)
      (swap! oauth-data assoc :oauth-token-secret oauth_token_secret)
      (swap! oauth-data assoc :oauth-consumer consumer))))

(defn get-request-token
  "Get the OAuth request token."
  []
  (get-oauth-data)
  (:oauth-token @oauth-data))

(defn user-approval-uri
  "Return the URL needed to redirect the user to Twitter approval page."
  []
  (get-oauth-data)
  (let [consumer(:oauth-consumer @oauth-data)
        request-token (:oauth-token @oauth-data)]
    (oauth/user-approval-uri consumer request-token)))

(defn oauth-token->access-token
  "Transform an OAuth token in an Access token."
  [oauth-token oauth-verifier]
  (let [consumer (:oauth-consumer @oauth-data)
        token {:oauth_token (:oauth-token @oauth-data)
               :oauth_token_secret (:oauth-token-secret @oauth-data)}]
    (when (= oauth-token (:oauth-token @oauth-data))
      (let [access-token (oauth/access-token consumer token oauth-verifier)
            {:keys [oauth_token oauth_token_secret]} access-token]
        (swap! oauth-data assoc :oauth-token oauth_token)
        (swap! oauth-data assoc :oauth-token-secret oauth_token_secret)))))

(defn get-credentials
  "Set up the necessary credentials to interact with Twitter API."
  []
  (let [consumer-key (get-in config [:twitter :consumer-key])
        consumer-secret (get-in config [:twitter :consumer-secret])
        access-token (:oauth-token @oauth-data)
        access-token-secret (:oauth-token-secret @oauth-data)]
    (make-oauth-creds consumer-key consumer-secret
                      access-token access-token-secret)))

(defn verify-credentials
  "Verify authentication credentials."
  []
  (account-verify-credentials
   :oauth-creds (get-credentials)
   :callbacks (SyncSingleCallback. response-return-body
                                   response-throw-error
                                   exception-rethrow)))

(defn save-logged-user
  "Save logged user data.
  Update `oauth-data` atom with logged user id and save the logged user
  in the database if not already present."
  []
  (let [response (verify-credentials)
        {id :id_str username :name screen_name :screen_name} response
        logged-user (logged-user/find-by-id id)]
    (swap! oauth-data assoc :logged-user-id id)
    (when (nil? logged-user)
      (logged-user/create
       {:id id
        :username username
        :screen_name screen_name
        :last_update (d/java-date->orient-date (java.util.Date.))}))))

(defn authorize-app
  "Complete app authorization.
  OAuth `token` is converted to access token, and the id of the logged
  user is saved in `oauth-data`."
  [token verifier]
  (oauth-token->access-token token verifier)
  (save-logged-user))

(defn get-home-tweets
  "Return the last `tweet-count` tweets in the home of the authenticated user."
  [tweet-count]
  (statuses-home-timeline
   :oauth-creds (get-credentials)
   :params {:count tweet-count}
   :callbacks (SyncSingleCallback. response-return-body
                                   response-throw-error
                                   exception-rethrow)))
