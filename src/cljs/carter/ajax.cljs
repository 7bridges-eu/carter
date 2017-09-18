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

(ns carter.ajax
  (:require [ajax.core :as ajax]))

(defn get-request
  [uri on-success on-error]
  {:http-xhrio {:method          :get
                :uri             uri
                :format          (ajax/json-request-format)
                :response-format (ajax/json-response-format {:keywords? true})
                :on-success      on-success
                :on-failure      on-error}})

(defn put-request
  [uri params on-success on-error]
  {:http-xhrio {:method          :put
                :uri             uri
                :params          params
                :format          (ajax/json-request-format)
                :response-format (ajax/json-response-format {:keywords? true})
                :on-success      on-success
                :on-failure      on-error}})

(defn post-request
  [uri params on-success on-error]
  {:http-xhrio {:method          :post
                :uri             uri
                :params          params
                :format          (ajax/json-request-format)
                :response-format (ajax/json-response-format {:keywords? true})
                :on-success      on-success
                :on-failure      on-error}})

(defn delete-request
  [uri on-success on-error]
  {:http-xhrio {:method          :delete
                :uri             uri
                :format          (ajax/json-request-format)
                :response-format (ajax/json-response-format {:keywords? true})
                :on-success      on-success
                :on-failure      on-error}})
