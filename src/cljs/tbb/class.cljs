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

(ns tbb.class)

(def strength
  {:warrior 1.0,
   :thief 1.2,
   :cleric 0.8})

(def speed
  {:warrior 8,
   :thief 11,
   :cleric 7})

(def defense
  {:warrior 1.0,
   :thief 1.4,
   :cleric 1.2})

(def move-list
  {:warrior [:attack, :defend]
   :thief   [:attack, :defend]
   :cleric  [:attack, :defend, :heal]})
