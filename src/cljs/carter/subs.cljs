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

(ns carter.subs
  (:require-macros [reagent.ratom :as r])
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 :active-panel
 (fn [db _]
   (:active-panel db)))

(rf/reg-sub
 :tweet-count
 (fn [db _]
   (:tweet-count db)))

(rf/reg-sub
 :message
 (fn [db _]
   (:message db)))

(rf/reg-sub-raw
 :data
 (fn [db _]
   (rf/dispatch [:get-hashtags-tweets])
   (r/reaction (get @db :data))))

(rf/reg-sub
 :loading
 (fn [db _]
   (:loading db)))

(rf/reg-sub-raw
 :logged-user-screen-name
 (fn [db _]
   (rf/dispatch [:get-logged-user])
   (r/reaction (get @db :logged-user-screen-name))))
