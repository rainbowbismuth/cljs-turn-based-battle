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

(ns tbb.simulation-test
  (:require [cljs.test :refer-macros [deftest is run-tests]]
            [tbb.class :as class]
            [tbb.combatant :as combatant]
            [tbb.simulation :as simulation]
            [tbb.ai.alphabeta :as alphabeta]))

(def start-p1 (combatant/mk-combatant 0 :ai "Alpha" :warrior))

(def start-p2 (combatant/mk-combatant 1 :user "Beta" :warrior))

(def sim
  (simulation/clock-tick-until-turn
    (simulation/Simulation. [start-p1 start-p2] [])))

(def p1 (simulation/exists-and-alive 0 sim))

(def p2 (simulation/exists-and-alive 1 sim))

(deftest p1-should-have-1-ap
  (is (= 1 (combatant/ap p1)))
  (is (= 0 (combatant/ap p2))))

(deftest can-pay-ap-on-p1
  (is (= 0 (combatant/ap (combatant/pay-ap 1 p1)))))

(deftest cant-pay-ap-on-p2
  (is (= nil (combatant/pay-ap 1 p2))))

(deftest can-update-cmbt
  (is (not= nil (simulation/update-cmbt sim p1 combatant/to-defend-state))))

(deftest can-target-reaction
  (is (not= nil (simulation/target-reaction p1 :attack p2))))

(deftest can-self-reaction
  (is (not= nil (simulation/self-reaction p1 :defend))))

(deftest can-exec-single-target
  (is (not= nil (simulation/exec-single-target p1 :attack p2 sim))))

(deftest can-try-to-pay-in-sim
  (is (not= nil (simulation/try-pay sim :attack p1))))

(deftest can-attack-with-p1
  (is (not= nil (simulation/simulate {:mv :attack :target 1} sim))))

(deftest can-play-ai
  (is (not= sim (alphabeta/play-ai sim))))

(enable-console-print!)
(run-tests)
