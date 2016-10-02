(ns snowth.devcards.projections
  (:require
   [devcards.core :as dc :refer-macros [defcard]]
   [snowth.devcards.common :refer [analemma-card state]]
   [snowth.projections :as proj]
   [snowth.render :as render :refer [racetrack]]
   [snowth.satellites :as sat]))

(defcard dots-ortho
  (analemma-card sat/earth)
  state)

(defcard racetrack-ortho
  (analemma-card sat/earth racetrack)
  state)

(defcard dots-stereo
  (analemma-card sat/earth proj/stereographic)
  state)

(defcard racetrack-stereo
  (analemma-card sat/earth racetrack proj/stereographic)
  state)
