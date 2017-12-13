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

(ns carter.tasks.init
  (:require [carter.services.config :refer [config]]
            [carter.services.orientdb :as db]
            [clj-odbp.core :as clj-odbp]
            [mount.core :as mount]))

(defn- create-db-if-not-present!
  "Create OrientDB database if it is not already present."
  []
  (println "Creating db, if not present")
  (let [user (get-in config [:orient :properties :user])
        pass (get-in config [:orient :properties :password])
        db-name (get-in config [:orient :db-name])
        conn (clj-odbp/connect-server user pass)]
    (when-not (:result (clj-odbp/db-exist conn db-name))
      (clj-odbp/db-create conn db-name))))

(defn- create-logged-user-vertex!
  "Create LoggedUser vertex."
  []
  (println "Creating LoggedUser vertex")
  (db/execute! "create class LoggedUser extends V")
  (db/execute! "create property LoggedUser.id string")
  (db/execute! "create property LoggedUser.username string")
  (db/execute! "create property LoggedUser.screen_name string")
  (db/execute! "create property LoggedUser.last_update datetime")
  (db/execute! "create property LoggedUser.oauth_token string")
  (db/execute! "create property LoggedUser.oauth_token_secret string"))

(defn- create-user!
  "Create User vertex."
  []
  (println "Creating User vertex")
  (db/execute! "create class User extends V")
  (db/execute! "create property User.id string")
  (db/execute! "create property User.username string")
  (db/execute! "create property User.screen_name string"))

(defn- create-tweet!
  "Create Tweet vertex."
  []
  (println "Creating Tweet vertex")
  (db/execute! "create class Tweet extends V")
  (db/execute! "create property Tweet.id string")
  (db/execute! "create property Tweet.text string")
  (db/execute! "create property Tweet.created_at datetime"))

(defn- create-hashtag!
  "Create Hashtag vertex."
  []
  (println "Creating Hashtag vertex")
  (db/execute! "create class Hashtag extends V")
  (db/execute! "create property Hashtag.name string"))

(defn- create-sees!
  "Create Tweeted edge."
  []
  (println "Creating Sees edge")
  (db/execute! "create class Sees extends E"))

(defn- create-tweeted!
  "Create Tweeted edge."
  []
  (println "Creating Tweeted edge")
  (db/execute! "create class Tweeted extends E")
  (db/execute! "create property Tweeted.logged_user_id string"))

(defn- create-contains!
  "Create Contains edge."
  []
  (println "Creating Has edge")
  (db/execute! "create class Has extends E")
  (db/execute! "create property Has.logged_user_id string"))

(defn -main []
  (mount/start #'carter.services.config/config)
  (create-db-if-not-present!)
  (println "Creating Vertexes")
  (create-logged-user-vertex!)
  (create-user!)
  (create-tweet!)
  (create-hashtag!)
  (println "Creating Edges")
  (create-sees!)
  (create-tweeted!)
  (create-contains!)
  (println "init-db done"))
