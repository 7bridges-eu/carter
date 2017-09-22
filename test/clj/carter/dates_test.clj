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
