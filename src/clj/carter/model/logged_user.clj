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

(ns carter.model.logged-user
  (:require [carter.services.orientdb :as db]))

(def select-queries
  {:find-all "select from LoggedUser"
   :find-by-rid "select from LoggedUser where @rid = :rid"
   :find-by-id "select from LoggedUser where id = :id"
   :find-by-username "select from LoggedUser where username = :username"})

(def update-queries
  {:update-by-rid "update LoggedUser set id = :id, username = :username,
                   screen_name = :screen_name, last_update = :last_update,
                   oauth_token = :oauth_token,
                   oauth_token_secret = :oauth_token_secret
                   where @rid = :rid"})

(defn find-all
  "Retrieve all the LoggedUser vertexes."
  []
  (let [query (:find-all select-queries)]
    (db/query! query)))

(defn- find-by
  [find-key params]
  (let [query (get select-queries find-key)]
    (db/query! query params)))

(defn find-by-rid
  "Find the LoggedUser vertex by `rid`."
  [rid]
  (first (find-by :find-by-rid {:rid rid})))

(defn find-by-id
  "Find the LoggedUser vertex by `id`."
  [id]
  (first (find-by :find-by-id {:id id})))

(defn find-by-username
  "Find the LoggedUser vertex by `username`."
  [username]
  (first (find-by :find-by-username {:username username})))

(defn create
  "Create a LoggedUser vertex.
  `params` is a map with :id, :username and :screen_name keys.
  All values are strings."
  [params]
  (db/insert! "LoggedUser" params))

(defn update-by-rid
  "Update a LoggedUser vertex.
  `params` is a map with :rid, :id, :username and :screen_name keys."
  [params]
  (let [query (:update-by-rid update-queries)]
    (db/execute! query params)))

(defn delete
  "Delete a LoggedUser by `rid`."
  [rid]
  (db/delete! rid))
