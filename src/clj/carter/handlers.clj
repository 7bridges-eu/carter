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

(ns carter.handlers
  (:require [carter.api.resources.twitter :as r.twitter]
            [carter.model.logged-user :as logged-user]
            [carter.services.twitter :as s.twitter]
            [carter.templates :refer [index-html]]
            [ring.util.http-response :as response]))

(defn sign-in
  "Direct to the sign in page."
  []
  (response/ok (index-html "carter - Sign in")))

(defn callback
  "Callback for Twitter API.

  If the user granted permission to carter:
  - save the first 150 tweets of her timeline
  - store her twitter id in the cookies as \"user-id\"
  - direct her to the homepage

  Otherwise, redirect her to the \"Sign in\" page."
  [request]
  (let [query-params (:params request)
        {denied :denied token :oauth_token
         verifier :oauth_verifier} query-params]
    (if (nil? denied)
      (let [logged-user-id (s.twitter/authorize-app token verifier)]
        (r.twitter/save-first-150-tweets logged-user-id)
        (-> (response/found "/")
            (assoc-in [:cookies "user-id"] {:value logged-user-id})))
      (response/found "/denied"))))

(defn permission-revoked?
  ""
  [logged-user-id]
  (nil? (try
          (s.twitter/home-tweets logged-user-id 1)
          (catch Exception e nil))))

(defn existing-user?
  "Check if a user with id equal to `logged-user-id` exists."
  [logged-user-id]
  (not (nil? (logged-user/find-by-id logged-user-id))))

(defn index-page
  "Direct to \"/home\" only when:

  - \"user-id\" cookie is present
  - user data are already stored on the database

  Otherwise, redirect to \"Sign in\"."
  [request]
  (let [logged-user-id (get-in request [:cookies "user-id" :value])]
    (if (and (not (nil? logged-user-id))
             (not (permission-revoked? logged-user-id))
             (existing-user? logged-user-id))
      (response/ok (index-html "carter - Home"))
      (response/found "/sign-in"))))
