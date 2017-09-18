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

(ns carter.services.orientdb
  (:require [carter.services.config :refer [config]]
            [clj-odbp.configure :as db-conf]
            [clj-odbp.core :as db]
            [mount.core :as mount]))

(defn set-db-host
  "Configure clj-odbp with the host from config.edn."
  []
  (let [host (get-in config [:orient :host])]
    (db-conf/configure-driver {:host host})))

(defn reset-db-host
  "Reset clj-odbp host to nil."
  []
  (db-conf/configure-driver {:host nil}))

(mount/defstate orientdb
  :start (set-db-host)
  :stop (reset-db-host))

(defn get-connection
  "Open a connection to OrientDB."
  []
  (let [db-name (get-in config [:orient :db-name])
        user (get-in config [:orient :properties :user])
        password (get-in config [:orient :properties :password])]
    (db/db-open db-name user password)))

(defn query!
  "Execute a sql query and return the results."
  ([query]
   (query! query {}))
  ([query params]
   (let [conn (get-connection)]
     (db/query-command conn query :params params))))

(defn update!
  "Execute a sql update and return the number of changed records."
  [sql-parameters])

(defn delete!
  "Delete the record identified by `rid`."
  [rid]
  (let [conn (get-connection)]
    (db/record-delete conn rid)))

(defn insert!
  "Insert a new record in the corresponding `class` with `params`."
  [class params]
  (let [conn (get-connection)
        record (assoc params :_class class)]
    (db/record-create conn record)))

(defn update!
  "Update the record of class `class` with `params`, identified by `rid.`"
  [class params rid]
  (let [conn (get-connection)
        record (assoc params :_class class)]
    (db/record-update conn rid record)))

(defn execute!
  "Execute the given `command` adding `params` if present."
  ([command]
   (execute! command {}))
  ([command params]
   (let [conn (get-connection)]
     (db/execute-command conn command :params params))))
