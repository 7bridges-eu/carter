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

(ns carter.dates-test
  (:require [carter.dates :as d]
            [clj-time.core :as t]
            [midje.sweet :refer :all]))

(def twitter-date "Thu Aug 31 05:00:09 +0000 2017")
(def java-date (.toDate (t/date-time 2017 8 31 5 0 9)))
(def orient-date "2017-08-31 07:00:09")
(def ui-date "07:00:09 31-08-2017")

(facts "Test dates utility functions"
       (fact "twitter-date->orient-date should convert 'Thu Aug 31 05:00:09 +0000 2017' to '2017-08-31 07:00:09'"
             (d/twitter-date->orient-date twitter-date) => orient-date)
       (fact "java-date->orient-date should convert '2017-08-31T05:00:09.000-00:00' to '2017-08-31 07:00:09'"
             (d/java-date->orient-date java-date) => orient-date)
       (fact "orient-date->ui-date should convert '2017-08-31T05:00:09.000-00:00' to '07:00:09 31-08-2017'"
             (d/orient-date->ui-date java-date) => ui-date))
