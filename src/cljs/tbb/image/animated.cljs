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

(ns tbb.image.animated)

(defn sprite
  [src x y w h]
  [:div
    {:style
      {:background-image src
       :background-repeat "no-repeat"
       :width (str w "px")
       :height (str h "px")
       :background-position (str "-" x "px -" y "px")}}])

(defn- bottom-sprite
 [src x y w h]
 [:div
   {:style
     {:position "absolute"
      :animation "tbb-flip-book-anim-bottom 1s infinite"
      :animation-timing-function "ease-in-out"
      :background-image src
      :background-repeat "no-repeat"
      :width (str w "px")
      :height (str h "px")
      :background-position (str "-" x "px -" y "px")}}])


(defn- top-sprite
  [src x y w h]
  [:div
    {:style
      {:position "absolute"
       :animation "tbb-flip-book-anim-top 1s infinite"
       :animation-timing-function "ease-in-out"
       :background-image src
       :background-repeat "no-repeat"
       :width (str w "px")
       :height (str h "px")
       :background-position (str "-" x "px -" y "px")}}])

(defn flip-book
  [src1 src2 x y w h]
  [:div
    {:style
      {:position "relative"}}
    (bottom-sprite src1 x y w h)
    (top-sprite src2 x y w h)])
