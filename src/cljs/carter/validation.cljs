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

(ns carter.validation
  (:require [re-frame.core :as rf]))

(defn or-empty-string
  "Return the original value or an empty string when the value is nil."
  [value]
  (or value ""))

(defn or-zero
  "Return the original value or 0 when the value is nil."
  [value]
  (or value "0"))

(defn or-empty-vec
  "Return the original value or an emtpy vector when the value is nil."
  [value]
  (or value []))

(defn check-nil-then-predicate
  "Check if the value is nil, then apply the predicate.
   This is useful only for mandatory fields."
  [value predicate]
  (if (nil? value)
    false
    (predicate value)))

(defn validate-input
  "Validate an input field against a list of requirements.
   Keep throwing error until all requirements are met."
  [value requirements]
  (->> requirements
       (filter (fn [req] (not ((:check-fn req) value))))
       (doall)
       (map (fn [req] ^{:key req} (:message req)))))

(defn validation-msg-box []
  (fn []
    (when-let [show @(rf/subscribe [:show-validation])]
      (let [validation-message (rf/subscribe [:validation-msg])]
        [:div
         {:style {:padding-top ".5em"
                  :padding-left ".5em"
                  :color "#FF5555"}}
         [:ul
          (for [m @validation-message]
            [:li {:key (random-uuid)} m])]]))))
