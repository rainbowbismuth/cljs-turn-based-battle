; cljs-turn-based-battle, a small browser game.
; Copyright (C) 2016  Emily A. Bellows
;
; This program is free software: you can redistribute it and/or modify
; it under the terms of the GNU General Public License as published by
; the Free Software Foundation, either version 3 of the License, or
; (at your option) any later version.
;
; This program is distributed in the hope that it will be useful,
; but WITHOUT ANY WARRANTY; without even the implied warranty of
; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
; GNU General Public License for more details.
;
; You should have received a copy of the GNU General Public License
; along with this program.  If not, see <http://www.gnu.org/licenses/>.

(ns tbb.core
  (:require [reagent.core :as r]))

(defonce model-atom (r/atom nil))

(def party-to-css-class {:ai "ai-party", :user "user-party"})

(defn view-party
  [party model]
  [:div
    {:class (party-to-css-class party)}
    "no party atm!"])

(defn view-combat-log-line
  [idx line]
  (let [op (- 1.0 (* idx 0.08))]
    ^{:key idx}
    [:div
      {:class "combat-log-line"
       :style {:opacity op}}
      line]))

(defn view-combat-log
  [model]
  [:div
    {:class "combat-log"}
    (map-indexed view-combat-log-line ["just" "a" "test" "really" "for reals" "yup"])])

(defn view-ct-bar
  [model]
  [:div
    {:class "ct-bar"}
    "still just a test"])

(defn view
  [model]
  [:div
    {:class "game"}
    [:div
      {:class "main"}
      (view-party :ai @model-atom)
      (view-party :user @model-atom)
      (view-combat-log @model-atom)]
    (view-ct-bar @model-atom)])

(r/render-component [view]
  (.getElementById js/document "app"))
