(ns tbb.command)

(defrecord SingleTarget [mv target])

(defrecord SelfTarget [mv])

(defn move
  [cmd]
  (.-mv cmd))
