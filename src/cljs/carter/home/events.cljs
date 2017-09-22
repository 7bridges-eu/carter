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

(ns carter.home.events
  (:require [carter.ajax :as ajax]
            [day8.re-frame.http-fx]
            [re-frame.core :as rf]))

(rf/reg-event-db
 :tweet-count-change
 (fn [db [_ value]]
   (assoc db :tweet-count value)))

(rf/reg-event-fx
 :get-tweets
 (fn [{db :db} _]
   (let [tweet-count (get db :tweet-count 0)]
     (assoc
      (ajax/post-request "/api/tweet/user"
                         {:tweet-count tweet-count}
                         [:get-hashtags-tweets]
                         [:bad-response])
      :db (-> db
              (assoc :data [])
              (assoc :loading true))))))

(rf/reg-event-db
 :load-data
 (fn [db [_ value]]
   (assoc db :data value)))

(rf/reg-event-fx
 :get-hashtags-tweets
 (fn [{db :db} _]
   (assoc
    (ajax/get-request "/api/hashtag/tweet"
                      [:load-data]
                      [:bad-response])
    :db (assoc db :loading false))))
