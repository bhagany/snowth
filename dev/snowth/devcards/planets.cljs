(ns snowth.devcards.planets
  (:require
   [devcards.core :as dc :refer-macros [defcard]]
   [snowth.devcards.common :refer [analemma-card state]]
   [snowth.satellites :as sat]))

(defcard
  "# Planets
  Any body that orbits another will result in _some_ analemma pattern,
  including the other planets in our solar system. Below, you'll find
  solar analemmas for each planet as they appear right now, as though
  your latitude and longitude on each planet were the same as your latitude
  and longitude on Earth.

  The red dot is the sun's position right now. If you see a black line,
  that is the horizon - the sun would not be visible below it, but I'm showing
  more than you could actually see with your eyes here.

  There's no need to refresh the page to get current analemmas - they
  update automatically as time passes.")

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

(defcard pluto
  (analemma-card sat/pluto)
  state)

(defcard
  "I've included Mercury and Venus below for completeness, but they
  don't rotate fast enough relative to their orbital period to make
  very interesting analemmas")

(defcard mercury
  (analemma-card sat/mercury)
  state)

(defcard venus
  (analemma-card sat/venus)
  state)
