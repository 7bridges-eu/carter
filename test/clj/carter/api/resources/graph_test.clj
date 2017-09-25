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

(ns carter.api.resources.graph-test
  (:require [carter.api.resources.graph :as r]
            [carter.model.graph :as m]
            [midje.sweet :refer :all]))

(def data [{:loggeduser_rid "#18:3", :_rid "#-2:0", :loggeduser_name "m7bridges", :tweet_rid "#25:116", :hashtag_rid "#31:108", :hashtag_name "19settembre", :tweet_text "#Leggere è andare incontro a qualcosa che sta per essere e ancora nessuno sa cosa sarà - Italo Calvino\n\n#19settembre https://t.co/mnb907lSk4", :user_name "LaFeltrinelli", :user_rid "#22:17", :_version 0}{:loggeduser_rid "#18:3", :_rid "#-2:33", :loggeduser_name "m7bridges", :tweet_rid "#26:121", :hashtag_rid "#32:109", :hashtag_name "UltimOra", :tweet_text "#UltimOra, Gossip. #AlessandroDiBattista diventerà presto padre di un bellissimo complotto https://t.co/KWwoFgvUIY", :user_name "lercionotizie", :user_rid "#21:17", :_version 0}])

(facts "Test graph API resource"
       (fact "logged-user-node returns a map like {:id rid :name screen-name :label 'LoggedUser'}"
             (let [res {:id "#18:3" :name "m7bridges" :label "LoggedUser"}]
               (r/logged-user-node data) => res))
       (fact "conj-if-absent should add an element to a collection only when absent"
             (let [coll [{:id 1} {:id 2}]]
               (r/conj-if-absent coll {:id 1}) => coll)
             (let [coll [{:id 1} {:id 2}]]
               (r/conj-if-absent coll {:id 3}) => [{:id 1} {:id 2} {:id 3}]))
       (fact "user-nodes should return a vector of user nodes"
             (let [res [{:id "#22:17" :name "LaFeltrinelli" :label "User"}
                        {:id "#21:17" :name "lercionotizie" :label "User"}]]
               (r/user-nodes data) => res))
       (fact "tweet-nodes should return a vector of tweet nodes"
             (let [res [{:id "#25:116" :name "#Leggere è anda" :label "Tweet"}
                        {:id "#26:121" :name "#UltimOra, Goss" :label "Tweet"}]]
               (r/tweet-nodes data) => res))
       (fact "hashtag-nodes should return a vector of hashtag nodes"
             (let [res [{:id "#31:108" :name "19settembre" :label "Hashtag"}
                        {:id "#32:109" :name "UltimOra" :label "Hashtag"}]]
               (r/hashtag-nodes data) => res))
       (fact "nodes should return a map with :nodes structure"
             (let [res [{:id "#18:3" :name "m7bridges" :label "LoggedUser"}
                        {:id "#22:17" :name "LaFeltrinelli" :label "User"}
                        {:id "#21:17" :name "lercionotizie" :label "User"}
                        {:id "#25:116" :name "#Leggere è anda" :label "Tweet"}
                        {:id "#26:121" :name "#UltimOra, Goss" :label "Tweet"}
                        {:id "#31:108" :name "19settembre" :label "Hashtag"}
                        {:id "#32:109" :name "UltimOra" :label "Hashtag"}]]
               (r/nodes data) => res))
       (fact "follow-links should return a vector of follow links"
             (let [res [{:source "#18:3" :target "#22:17" :type "FOLLOWS"}
                        {:source "#18:3" :target "#21:17" :type "FOLLOWS"}]]
               (r/follows-links data) => res))
       (fact "tweeted-links should return a vector of tweeted links"
             (let [res [{:source "#22:17" :target "#25:116" :type "TWEETED"}
                        {:source "#21:17" :target "#26:121" :type "TWEETED"}]]
               (r/tweeted-links data) => res))
       (fact "has-links should return a vector of has links"
             (let [res [{:source "#25:116" :target "#31:108" :type "HAS"}
                        {:source "#26:121" :target "#32:109" :type "HAS"}]]
               (r/has-links data) => res))
       (fact "links should return a map with :links structure"
             (let [res [{:source "#18:3" :target "#22:17" :type "FOLLOWS"}
                        {:source "#18:3" :target "#21:17" :type "FOLLOWS"}
                        {:source "#22:17" :target "#25:116" :type "TWEETED"}
                        {:source "#21:17" :target "#26:121" :type "TWEETED"}
                        {:source "#25:116" :target "#31:108" :type "HAS"}
                        {:source "#26:121" :target "#32:109" :type "HAS"}]]
               (r/links data) => res))
       (fact "graph-data should return a map with :nodes and :links structures"
             (with-redefs [m/graph-data (fn [id] data)]
               (let [res
                     {:nodes
                      [{:id "#18:3" :name "m7bridges" :label "LoggedUser"}
                       {:id "#22:17" :name "LaFeltrinelli" :label "User"}
                       {:id "#21:17" :name "lercionotizie" :label "User"}
                       {:id "#25:116" :name "#Leggere è anda" :label "Tweet"}
                       {:id "#26:121" :name "#UltimOra, Goss" :label "Tweet"}
                       {:id "#31:108" :name "19settembre" :label "Hashtag"}
                       {:id "#32:109" :name "UltimOra" :label "Hashtag"}]
                      :links
                      [{:source "#18:3" :target "#22:17" :type "FOLLOWS"}
                       {:source "#18:3" :target "#21:17" :type "FOLLOWS"}
                       {:source "#22:17" :target "#25:116" :type "TWEETED"}
                       {:source "#21:17" :target "#26:121" :type "TWEETED"}
                       {:source "#25:116" :target "#31:108" :type "HAS"}
                       {:source "#26:121" :target "#32:109" :type "HAS"}]}]
                 (r/graph-data data) => res))))
