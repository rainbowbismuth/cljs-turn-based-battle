(ns tbb.core
  (:require [reagent.core :as r]))

(defonce model-atom (r/atom nil))

(def party-to-css-class {:ai "ai-party", :user "user-party"})

(defn view-party
  [party model]
  [:div
    {:class (party-to-css-class party)}
    "no party atm!"])

(defn view-combat-log-line
  [idx line]
  (let [op (- 1.0 (* idx 0.08))]
    ^{:key idx}
    [:div
      {:class "combat-log-line"
       :style {:opacity op}}
      line]))

(defn view-combat-log
  [model]
  [:div
    {:class "combat-log"}
    (map-indexed view-combat-log-line ["just" "a" "test" "really" "for reals" "yup"])])

(defn view-ct-bar
  [model]
  [:div
    {:class "ct-bar"}
    "still just a test"])

(defn view
  [model]
  [:div
    {:class "game"}
    [:div
      {:class "main"}
      (view-party :ai @model-atom)
      (view-party :user @model-atom)
      (view-combat-log @model-atom)]
    (view-ct-bar @model-atom)])

(r/render-component [view]
  (.getElementById js/document "app"))
