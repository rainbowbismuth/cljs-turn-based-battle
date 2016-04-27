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

(ns tbb.move)

(def cost
  {:attack 1
   :heal 2
   :defend 0})

(def name-of
  {:attack "Attack"
   :heal "Heal"
   :defend "Defend"})

(def tooltip
  {:attack "Attack a target, costs 1 AP"
   :heal "Heal a target for 45 HP, costs 2 AP"
   :defend "Take half damage until your next turn, and gain 1 AP"})

(def type-of
  {:attack :single-target
   :heal :single-target
   :defend :self-target})
