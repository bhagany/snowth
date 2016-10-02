(ns snowth.devcards.planets
  (:require
   [devcards.core :as dc :refer-macros [defcard]]
   [snowth.devcards.common :refer [analemma-card state]]
   [snowth.satellites :as sat]))

(defcard earth
  (analemma-card sat/earth)
  state)

(defcard mars
  (analemma-card sat/mars)
  state)

(defcard jupiter
  (analemma-card sat/jupiter)
  state)

(defcard saturn
  (analemma-card sat/saturn)
  state)

(defcard uranus
  (analemma-card sat/uranus)
  state)

(defcard neptune
  (analemma-card sat/neptune)
  state)

;; These are pretty uninteresting, including for completeness

(defcard mercury
  (analemma-card sat/mercury)
  state)

(defcard venus
  (analemma-card sat/venus)
  state)
