(ns snowth.devcards.projections
  (:require
   [devcards.core :as dc :refer-macros [defcard]]
   [snowth.devcards.common :refer [analemma-card place-card state]]
   [snowth.projections :as proj]
   [snowth.render :as render :refer [racetrack]]
   [snowth.satellites :as sat]))

(defcard
  "# Renderers and Projections

  Tests each combination of the built-in renderers and projections on Earth's
  analemma. I'm calling the renderers \"dots\" and \"racetrack\", and the
  available projections are orthographic and stereographic. The red dot
  indicates the position of the sun in the analemma for your current time
  and location, and the display auto-updates every 30 seconds.")

(defcard
  (place-card)
  state)

(defcard dots-orthographic
  (analemma-card sat/earth)
  state)

(defcard racetrack-orthographic
  (analemma-card sat/earth racetrack)
  state)

(defcard dots-stereographic
  (analemma-card sat/earth proj/stereographic)
  state)

(defcard racetrack-stereographic
  (analemma-card sat/earth racetrack proj/stereographic)
  state)
