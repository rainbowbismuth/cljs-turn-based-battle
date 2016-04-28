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

(ns tbb.simulation
  (:require [tbb.combatant :as combatant]
            [tbb.move :as move]
            [tbb.command :as command]))

(defrecord Simulation
  [combatants active combatlog])

(defn combatants
  [sim]
  (.-combatants sim))

(defn combat-log
  [sim]
  (.-combatlog sim))

(defn- update-cmbt
  [sim cmbt f]
  (Simulation.
    (assoc (.-combatants sim) (combatant/id cmbt) (f cmbt))
    (.-active sim)
    (.-combatlog sim)))

(defn- set-active-cmbt
  [id sim]
  (Simulation.
    (.-combatants sim)
    id
    (.-combatlog sim)))

(defn- get-cmbt
  [cmbt sim]
  ((.-combatants sim) (combatant/id cmbt)))

(defn- get-cmbt-by-id
  [id sim]
  ((.-combatants sim) id))

(defn- party-for
  [player]
  (filter #(= player (combatant/player %))))

(defn- alive-units-for
  [player]
  (comp
    (party-for player)
    (filter combatant/alive)))

(defn party
  [player sim]
  (into [] (party-for player) (combatants sim)))

(defn lost
  [player sim]
  (empty?
    (sequence (alive-units-for player) (combatants sim))))

(defn game-over
  [sim]
  (or (lost :ai sim) (lost :user sim)))

(defn- active-cmbt
  ([sim]
   (if-let [id (.-active sim)]
     (get-cmbt-by-id id sim)
     (active-cmbt sim (count (.-combatants sim)) 0)))

  ([sim cnt i]
   (if (< i cnt)
    (let [cmbt ((.-combatants sim) i)]
      (if (combatant/can-i-have-active-turn cmbt)
       cmbt
       (recur sim cnt (inc i)))))))

(defn do-i-have-active-turn
  [cmbt sim]
  (= (combatant/id cmbt) (.-active sim)))

(defn- clock-tick
  [sim]
  (Simulation.
    (into [] (map combatant/clock-tick) (.-combatants sim))
    (.-active sim)
    (.-combatlog sim)))

(defn clock-tick-until-turn
  [sim]
  (if (.-active sim)
    sim
    (if (game-over sim)
      sim
      (if-let [cmbt (active-cmbt sim)]
        (set-active-cmbt (combatant/id cmbt)
          (update-cmbt sim cmbt combatant/increase-ap))
        (recur (clock-tick sim))))))

(defn whos-turn
  [sim]
  (if-let [cmbt (active-cmbt sim)]
    (combatant/player cmbt)))

(defn- drop-active-turn
  [sim]
  (when sim
    (if-let [cmbt (active-cmbt sim)]
      (clock-tick-until-turn
        (set-active-cmbt nil
          (update-cmbt sim cmbt combatant/pay-turn-ct))))))

(defn turn-order-list
  ([sim]
   (turn-order-list 12 sim []))
  ([i sim acc]
   (if (pos? i)
     (if-let [cmbt (active-cmbt sim)]
       (recur
         (dec i)
         (drop-active-turn sim)
         (conj acc cmbt))
       (recur
         (dec i)
         (clock-tick-until-turn sim)
         acc))
     acc)))

(defn exists-and-alive
  [id sim]
  (if-let [cmbt (get (combatants sim) id)]
    (if (combatant/alive cmbt)
      cmbt)))

(defn- target-reaction
  [user mv target]
  (condp = mv
    :attack
      (let [dmg (int (* (combatant/strength user)
                        20.0
                        (combatant/defense target)))
            msg (str (combatant/get-name user)
                     " deals " dmg " damage to "
                     (combatant/get-name target) ".")]
        [(update target :hp #(- % dmg))
         msg])
    :heal
      (let [heal 45.0
            msg (str (combatant/get-name user)
                     " heals " (combatant/get-name target)
                     " for " heal " hitpoints.")]
        [(update target :hp #(+ % heal))
         msg])))

(defn- self-reaction
  [user mv]
  (condp = mv
    :defend
      (let [updated (->> user
                        combatant/increase-ap
                        combatant/to-defend-state)]
        [updated
         (str (combatant/get-name updated) " has started defending.")])))

(defn- try-pay
  [sim mv cmbt]
  (if-let [payed (combatant/pay-ap (move/cost mv) cmbt)]
    (update-cmbt sim payed (constantly payed))))

(defn append-msg
  [sim msg]
  (Simulation.
    (.-combatants sim)
    (.-active sim)
    (conj (.-combatlog sim) msg)))

(defn- exec-single-target
  [user mv target sim]
  (if-let [next-sim (try-pay sim mv user)]
    (let [next-user (get-cmbt user next-sim)
          next-target (get-cmbt target next-sim)
          [next-next-target msg] (target-reaction next-user mv next-target)]
      (-> next-sim
        (update-cmbt next-next-target (constantly next-next-target))
        (append-msg msg)))))

(defn- exec-self-target
  [user mv sim]
  (if-let [next-sim (try-pay sim mv user)]
    (let [next-user (get-cmbt user next-sim)
          [next-next-user msg] (self-reaction next-user mv)]
      (-> next-sim
        (update-cmbt next-next-user (constantly next-next-user))
        (append-msg msg)))))

(defn simulate
  [cmd sim]
  (if-let [user (active-cmbt sim)]
    (let [next-sim (update-cmbt sim user combatant/to-default-state)
          next-user (get-cmbt user next-sim)]
      (drop-active-turn
        (if-let [target (get cmd :target)]
          (exec-single-target next-user
                              (command/move cmd)
                              (get-cmbt-by-id target next-sim) next-sim)
          (exec-self-target next-user
                            (command/move cmd)
                            next-sim))))))
