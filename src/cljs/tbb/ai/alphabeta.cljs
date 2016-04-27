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

(ns tbb.ai.alphabeta
  (:require [tbb.combatant :as combatant]
            [tbb.move :as move]
            [tbb.simulation :as simulation]))

(defn- score-combatant
  [cmbt]
  (let [bonus (if (combatant/alive cmbt) 50 0)
        score (max 0 (+ (combatant/hp cmbt) bonus))]
    (if (= (combatant/player cmbt) :ai)
      score
      (- score))))

(defn- score
  [sim]
  (transduce
    (map score-combatant)
    +
    (simulation/combatants sim)))

(defn- targets-for-move
  [sim mv]
  (condp = (move/type-of mv)
    :single-target
      (for [target (simulation/combatants sim)
            :when (combatant/alive target)]
        {:mv mv :target (combatant/id target)})
    :self-target
      (list {:mv mv})))

(defn- available-moves
  [sim]
  (if-let [cmbt (simulation/active-cmbt sim)]
    (for [mv (combatant/move-list cmbt)
          cmd (targets-for-move sim mv)]
      cmd)))

(def inf 256000.0)

(declare alphabeta)

(defn- alphabeta-maximizing
  [cmds sim depth a b v]
  (if-not (empty? cmds)
    (if-let [next-sim (simulation/simulate (first cmds) sim)]
      (let [next-v (max v (alphabeta next-sim (dec depth) a b))
            next-a (max a next-v)]
        (if (< b next-a)
          next-v
          (recur (rest cmds) sim depth next-a b next-v)))
      (recur (rest cmds) sim depth a b v))
    v))

(defn- alphabeta-minimizing
  [cmds sim depth a b v]
  (if-not (empty? cmds)
    (if-let [next-sim (simulation/simulate (first cmds) sim)]
      (let [next-v (min v (alphabeta next-sim (dec depth) a b))
            next-b (min b next-v)]
        (if (< next-b a)
          next-v
          (recur (rest cmds) sim depth a next-b next-v)))
      (recur (rest cmds) sim depth a b v))
    v))

(defn- alphabeta
  [sim depth a b]
  (if (or (= depth 0) (simulation/game-over sim))
    (score sim)
    (condp = (simulation/whos-turn sim)
      :ai
        (alphabeta-maximizing (available-moves sim) sim depth a b (- inf))
      :user
        (alphabeta-minimizing (available-moves sim) sim depth a b inf)
      nil
        (alphabeta (simulation/clock-tick sim) depth a b))))

(defn- evaluate-position
  [sim depth]
  (alphabeta sim depth (- inf) inf))

(defn- explore
  [sim cmd]
  (if-let [next-sim (simulation/simulate cmd sim)]
    (let [next-next-sim (simulation/clock-tick-until-turn next-sim)]
      [cmd next-next-sim (evaluate-position next-next-sim 4)])))

(defn play-ai
  [sim]
  (get (->>
         (available-moves sim)
         (map #(explore sim %))
         (filter some?)
         (sort-by #(get % 2))
         (first))
       1))

(comment
  (defn play-ai
    [sim]
    (let [explored (for [cmd (available-moves sim)]
                      (explore sim cmd))
          sorted (sort-by #(get % 2) explored)]
      ((first sorted) 1))))
