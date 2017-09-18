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

(ns carter.twitter.events
  (:require [carter.ajax :as ajax]
            [day8.re-frame.http-fx]
            [re-frame.core :as rf]))

(rf/reg-event-db
 :save-uri
 (fn [db [_ result]]
   (let [uri (get result :uri "")]
     (.log js/console (str "uri: " uri))
     (assoc db :approval-uri uri))))

(rf/reg-event-fx
 :sign-in
 (fn [{db :db} _]
   (ajax/get-request "/api/twitter/user-approval"
                     [:save-uri]
                     [:bad-response])))
