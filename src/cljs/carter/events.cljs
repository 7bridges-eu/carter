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

(ns carter.events
  (:require [carter.ajax :as ajax]
            [carter.db :as db]
            [day8.re-frame.http-fx]
            [re-frame.core :as rf]
            [reagent.cookies :as cookie]))

(rf/reg-event-db
 :initialize-db
 (fn [_ _]
   (-> db/default-db
       (assoc :user-id (cookie/get "user-id")))))

(rf/reg-event-db
 :set-active-panel
 (fn [db [_ active-panel]]
   (assoc db :active-panel active-panel)))

(rf/reg-event-fx
 :bad-response
 (fn [{db :db} [_ response]]
   (.log js/console response)
   {:dispatch [:show-error (get-in response [:response :error])]}))

(rf/reg-event-db
 :load-user
 (fn [db [_ value]]
   (assoc db :logged-user-screen-name (:screen-name value))))

(rf/reg-event-fx
 :get-logged-user
 (fn [{db :db} _]
   (ajax/get-request "/api/logged-user/data"
                     [:load-user]
                     [:bad-response])))
