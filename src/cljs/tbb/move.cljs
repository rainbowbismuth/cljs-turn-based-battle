(ns tbb.move)

(def cost
  {:attack 1
   :heal 2
   :defend 0})

(def tooltip
  {:attack "Attack a target, costs 1 AP"
   :heal "Heal a target for 45 HP, costs 2 AP"
   :defend "Take half damage until your next turn, and gain 1 AP"})

(def type-of
  {:attack :single-target
   :heal :single-target
   :defend :self-target})
