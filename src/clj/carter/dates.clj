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

(ns carter.dates
  (:require [clj-time
             [coerce :as c]
             [format :as f]])
  (:import java.text.SimpleDateFormat
           java.util.Locale))

(defn twitter-date->orient-date
  "Convert `date-str` from Twitter API date format to OrientDB date format."
  [date-str]
  (let [orient-date-format (SimpleDateFormat. "yyyy-MM-dd HH:mm:ss")
        formatter (f/with-locale
                    (f/formatter "EEE MMM dd HH:mm:ss Z yyyy")
                    Locale/ENGLISH)
        date (c/to-date (f/parse formatter date-str))]
    (.format orient-date-format date)))

(defn java-date->orient-date
  "Format a java.util.Date `java-date` to a string with a format suitable for
  OrientDB datetime properties."
  [java-date]
  (let [orient-date-format (SimpleDateFormat. "yyyy-MM-dd HH:mm:ss")]
    (.format orient-date-format java-date)))

(defn orient-date->ui-date
  "Format `orient-date` from OrientDB to a string to be displayed client-side."
  [orient-date]
  (let [ui-date-format (SimpleDateFormat. "HH:mm:ss dd-MM-yyyy")]
    (.format ui-date-format orient-date)))
