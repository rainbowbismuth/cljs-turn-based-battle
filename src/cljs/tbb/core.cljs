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
  (:require [tbb.combatant :as combatant]
            [tbb.simulation :as simulation]
            [tbb.move :as move]
            [tbb.class :as class]
            [tbb.command :as command]
            [tbb.ai.alphabeta :as alphabeta]
            [reagent.core :as r]))

(def initial-sim
  (simulation/clock-tick-until-turn
    (simulation/Simulation.
      [(combatant/mk-combatant 0 :user "Alpha" :warrior)
       (combatant/mk-combatant 1 :user "Beta" :thief)
       (combatant/mk-combatant 2 :user "Gamma" :cleric)
       (combatant/mk-combatant 3 :ai "Delta" :warrior)
       (combatant/mk-combatant 4 :ai "Epsilon" :thief)
       (combatant/mk-combatant 5 :ai "Zeta" :cleric)]
      nil
      [])))

(defn ai-if-necessary
  [sim]
  (if (simulation/game-over sim)
    sim
    (let [next-sim (simulation/clock-tick-until-turn sim)]
      (condp = (simulation/whos-turn next-sim)
        :ai (recur (alphabeta/play-ai next-sim))
        :user next-sim
        nil next-sim))))

(defonce model-atom (r/atom {:sim initial-sim :mov nil}))

(defn reset-model!
  []
  (swap! model-atom
    (constantly {:sim initial-sim :mov nil})))

(defn- swap-sim
  [model next-sim]
  (assoc model :mov nil :sim
    (simulation/clock-tick-until-turn (ai-if-necessary next-sim))))

(defn select-move!
  [mv]
  (condp = (move/type-of mv)
    :single-target
      (swap! model-atom #(assoc % :mov mv))
    :self-target
      (if-let [next-sim (simulation/simulate (command/SelfTarget. mv) (:sim @model-atom))]
        (swap!
          model-atom
          #(swap-sim % next-sim)))))

(defn select-target!
  [target]
  (let [cmd (command/SingleTarget. (:mov @model-atom) target)]
    (if-let [next-sim (simulation/simulate cmd (:sim @model-atom))]
      (swap!
        model-atom
        #(swap-sim % next-sim)))))

(defn cancel-selection!
  []
  (swap! model-atom #(assoc % :mov nil)))

(def player-to-css-class {:ai "party ai-party", :user "party user-party"})

(defn tooltip
  [txt]
  [:div
    {:class "tooltip"}
    txt])

(defn view-combatant-ap
  [cmbt]
  [:div
    {:class "combatant-ap tooltip-container"}
    [:span
      {:class "combatant-ap-label"}
      "AP"]
    [:span
      {:class "combatant-ap-filled"}
      (apply str (repeat (combatant/ap cmbt) "•"))]
    [:span
      {:class "combatant-ap-empty"}
      (apply str (repeat (- 5 (combatant/ap cmbt)) "•"))]
    (tooltip (str "This unit has " (combatant/ap cmbt) " AP to spend on moves."))])

(defn view-combatant-hp
  [cmbt]
  [:div
    {:class "combatant-hp tooltip-container"}
    [:span
      {:class "combatant-hp-label"}
      "HP"]
    (combatant/hp cmbt)
    (tooltip "Health")])

(defn view-combatant-ct
  [cmbt]
  [:div
    {:class "combatant-ct tooltip-container"}
    [:span
      {:class "combatant-ct-label"}
      "CT"]
    (combatant/ct cmbt)
    (tooltip "Charge time, unit takes a turn when at least 100")])

(defn view-combatant-class
  [cmbt]
  [:span
    {:class "combatant-class tooltip-container"}
    (str (class/name-of (:class cmbt)))
    (tooltip "The unit's class")])

(defn view-combatant-name
  [cmbt]
  [:span
    {:class "combatant-name tooltip-container"}
    (combatant/get-name cmbt)
    (tooltip "The unit's name")])

(defn view-target
  [cmbt]
  (let [attrs (if (combatant/alive cmbt)
                {:class "combatant-target combatant-target-alive"
                 :on-click #(select-target! (combatant/id cmbt))}
                {:class "combatant-target combatant-target-dead"})]
    ^{:key (combatant/id cmbt)}
    [:button attrs (combatant/get-name cmbt)]))

(defn view-targets
  [player model]
  [:div
    {:class "combatant-target-list"}
    [:div
      {:class "combatant-target-party"}
      (->> (simulation/combatants (:sim model))
        (filter (partial combatant/foes-of player))
        (map view-target))]
    [:div
      {:class "combatant-target-party"}
      (->> (simulation/combatants (:sim model))
        (filter (partial combatant/friends-of player))
        (map view-target))]
    [:button
      {:class "combatant-target-cancel"
       :on-click cancel-selection!}
      "Cancel"]])

(defn view-move
  [cmbt mv]
  ^{:key mv}
  [:button
    (if (>= (combatant/ap cmbt) (move/cost mv))
      {:class "combatant-move tooltip-container"
       :on-click #(select-move! mv)}
      {:class "combatant-move tooltip-container combatant-move-unusable"})
    (str (move/name-of mv) " " (apply str (repeat (move/cost mv) "•")))
    (tooltip (move/tooltip mv))])

(defn view-moves
  [cmbt]
  [:div
    {:class "combatant-move-list"}
    (map (partial view-move cmbt) (combatant/move-list cmbt))])

(defn view-combatant
  [player model cmbt]
  ^{:key (combatant/id cmbt)}
  [:div
    {:class (if (combatant/alive cmbt)
              "combatant combatant-alive"
              "combatant combatant-dead")}
    [:div
      {:class "combatant-status-bar"}
      (view-combatant-name cmbt)
      (view-combatant-class cmbt)
      (view-combatant-hp cmbt)
      (view-combatant-ap cmbt)
      (view-combatant-ct cmbt)]
    (if (and (= :user (combatant/player cmbt))
             (simulation/do-i-have-active-turn cmbt (:sim model)))
      (if (:mov model)
        (view-targets player model)
        (view-moves cmbt)))])

(defn view-party
  [player model]
  (let [units (simulation/party player (:sim model))]
    [:div
      {:class (player-to-css-class player)}
      (map (partial view-combatant player model) units)]))

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
    (map-indexed
      view-combat-log-line
      (reverse (simulation/combat-log (:sim model))))])

(defn view-ct-bar-unit
  [n cmbt]
  ^{:key n}
  [:div
    {:class "ct-bar-unit"}
    [:span
      {:class "ct-bar-unit-num"}
      (inc n)]
    (combatant/get-name cmbt)])

(defn view-ct-bar
  [model]
  (let [order (simulation/turn-order-list (:sim model))]
    (into
      [:div
        {:class "ct-bar"}
        "Turn Order"]
      (map-indexed view-ct-bar-unit order))))

(defn view
  [model]
  [:div
    {:class "game"}
    [:div
      {:class "main"}
      (view-party :ai @model-atom)
      (view-party :user @model-atom)
      (view-combat-log @model-atom)]
    (view-ct-bar @model-atom)
    [:button
      {:on-click reset-model!}
      "Reset!"]])

(r/render-component [view]
  (.getElementById js/document "app"))
