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
