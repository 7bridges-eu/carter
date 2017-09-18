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
            [carter.services.twitter :as s.twitter]
            [carter.templates :refer [index-html]]
            [ring.util.http-response :as response]))

(defn sign-in
  "Return the index page for the Twitter sign in.
  If the user has already signed in, redirect to /home."
  [request]
  (let [logged-user-id (get-in request [:cookies "user-id" :value])]
    (if (nil? logged-user-id)
      (response/ok (index-html "carter - Sign in"))
      (response/found "/home"))))

(defn prepare-homepage
  "Prepare the homepage for the logged user who authorized Carter:

  - complete app authorization
  - save the first 100 tweets of the logged user timeline
  - redirect to the homepage"
  [token verifier]
  (s.twitter/authorize-app token verifier)
  (r.twitter/save-first-100-tweets)
  (let [user-id (s.twitter/logged-user-id)]
    (response/content-type
     (-> (response/ok (index-html "carter - Home"))
         (assoc-in [:cookies "user-id"] {:value user-id}))
     "text/html; charset=utf-8")))
