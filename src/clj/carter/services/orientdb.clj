(ns carter.services.orientdb
  (:require [carter.services.config :refer [config]]
            [clj-odbp.constants :as db-const]
            [clj-odbp.core :as db]
            [clojure.string :as string]))

(defn get-session
  []
  (let [host (get-in config [:orient :host])
        port (get-in config [:orient :port])
        db-name (get-in config [:orient :db-name])
        user (get-in config [:orient :properties :user])
        password (get-in config [:orient :properties :password])]
    (db/db-open {:host host :port port} db-name user password)))

(defn split-on-space [word]
  (string/split word #"\s"))

(defn inline-query
  "Remove new lines and white spaces from `query`."
  [query]
  (->> query
       split-on-space
       (filter #(not (string/blank? %)))
       (string/join " ")))

(defn query!
  "Execute a sql query and return the results."
  ([query]
   (query! query {}))
  ([query params]
   (let [q (inline-query query)]
     (with-open [session (get-session)]
       (db/query-command session q :params params)))))

(defn delete!
  "Delete the record identified by `rid`."
  [rid]
  (with-open [session (get-session)]
    (db/record-delete session rid)))

(defn insert!
  "Insert a new record in the corresponding `class` with `params`."
  [class params]
  (let [record (assoc params :_class class)]
    (with-open [session (get-session)]
      (db/record-create session record))))

(defn update!
  "Update the record of class `class` with `params`, identified by `rid.`"
  [class params rid]
  (let [record (assoc params :_class class)]
    (with-open [session (get-session)]
      (db/record-update session rid record))))

(defn execute!
  ([command]
   (execute! command {}))
  ([command params]
   (let [c (inline-query command)]
     (with-open [session (get-session)]
       (db/execute-command session c :params params)))))

(defn with-return
  [s return]
  (str s "\nRETURN " return))

(defn script!
  "From `commands` and optionally `returns`, return a string of operations.
  The result will be the input of `execute-script!`."
  ([commands]
   (str "BEGIN\n"
        (->> (map inline-query commands)
             (interpose "\n")
             (apply str))
        "\nCOMMIT"))
  ([commands return]
   (-> (script! commands)
       (with-return return))))

(defn execute-script!
  [command params]
  (with-open [session (get-session)]
    (db/execute-script session command db-const/language-sql :params params)))
