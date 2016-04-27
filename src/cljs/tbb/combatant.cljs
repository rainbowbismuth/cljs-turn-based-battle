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

(ns tbb.combatant
  (:require [tbb.class :as class]))

(defrecord Combatant
  [id player name class hp ap ct state])

(def starting-hp
  {:warrior 100, :thief 70, :cleric 80})

(defn id
  [cmbt]
  (.-id cmbt))

(defn player
  [cmbt]
  (.-player cmbt))

(defn get-name
  [cmbt]
  (.-name cmbt))

(defn ap
  [cmbt]
  (.-ap cmbt))

(defn ct
  [cmbt]
  (.-ct cmbt))

(defn hp
  [cmbt]
  (.-hp cmbt))

(defn mk-combatant
  [id player name class]
  (Combatant.
    id player name class (starting-hp class) 0 0 :default))

(defn strength
  [cmbt]
  (class/strength (:class cmbt)))

(defn defense
  [cmbt]
  (class/defense (:class cmbt)))

(defn defenseBonus
  [cmbt]
  (if (= (:state cmbt) :defending)
    0.5
    1.0))

(defn speed
  [cmbt]
  (class/speed (:class cmbt)))

(defn move-list
  [cmbt]
  (class/move-list (:class cmbt)))

(defn move-available
  [move cmbt]
  (some #{move} (move-list cmbt)))

(defn alive
  [cmbt]
  (> (:hp cmbt) 0))

(defn dead
  [cmbt]
  (not (alive cmbt)))

(defn can-i-have-active-turn
  [cmbt]
  (and (alive cmbt) (>= (:ct cmbt) 100)))

(defn clock-tick
  [cmbt]
  (if (alive cmbt)
    (update cmbt :ct #(+ % (speed cmbt)))
    cmbt))

(defn increase-ap
  [cmbt]
  (update cmbt :ap #(min 5 (inc %))))

(defn pay-ap
  [amount cmbt]
  (if (>= (ap cmbt) amount)
    (update cmbt :ap #(- % amount))))

(defn pay-turn-ct
  [cmbt]
  (update cmbt :ct #(- % 100)))

(defn to-default-state
  [cmbt]
  (assoc cmbt :state :default))

(defn to-defend-state
  [cmbt]
  (assoc cmbt :state :defending))

(defn friends
  [cmbt-a cmbt-b]
  (= (player cmbt-a) (player cmbt-b)))

(defn foes
  [cmbt-a cmbt-b]
  (not= (player cmbt-a) (player cmbt-b)))

(defn friends-of
  [plyr cmbt]
  (= plyr (player cmbt)))

(defn foes-of
  [plyr cmbt]
  (not= plyr (player cmbt)))

(defn should-use-on
  [user mv target]
  (condp = mv
    :attack (and (foes user target) (alive target))
    :heal (and (friends user target) (alive target))))
