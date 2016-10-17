(ns snowth.devcards.selected-analemmas
  (:require
   [devcards.core :as dc :refer-macros [defcard]]
   [sablono.core :as sab :include-macros true]
   [snowth.core :as core :refer [analemma]]
   [snowth.devcards.common :refer [state]]
   [snowth.satellites :as sat]))

(defcard
  "# Selected Analemmas

  Here are some of Earth's analemmas for times and places chosen to show the
  range of what is possible. In each case, the June solstice is in red, so that
  it's easier to keep track of orientation.")

(def places
  [{:desc "North Pole at noon"
    :lat 90
    :long 0
    :time #inst "2016-01-01T12:00:00.000-00:00"}
   {:desc "North Pole at average sunset"
    :lat 90
    :long 0
    :time #inst "2016-01-01T18:00:00.000-00:00"}
   {:desc "North Pole at midnight"
    :lat 90
    :long 0
    :time #inst "2016-01-01T00:00:00.000-00:00"}
   #_{:desc
    :lat
    :long
    :time}
   #_{:desc
    :lat
    :long
    :time}])

(def june-solstice #inst "2016-06-21")

(defcard
  "## The Poles

  At the poles, the analemma for each time of day is pretty much the same,
  always oriented vertically relative to the horizon. It can be challenging
  to picture why this is so, but here is how I think about it - on the June
  solstice, the sun never sets on the North Pole. In fact, the \"day\" would
  start with the sun already high in the sky, and as time went on, it would
  appear to move horizontally, making a big circle around the sky centered on
  the point directly above your head, until it ended up just slightly lower in
  the sky from where it started, 24 hours later. The next day it would continue
  in the same way, always making a slow spiral.

  Since the sun's path through the sky is almost circular, and the center of
  that almost-circle is straight up, the only readily apparent thing that
  changes with time of day at the poles is the direction you'd have to face to
  look at the sun. The distance of the sun from the horizon does not change
  much. Because the sun is always approximately the same distance from the
  horizon during a given day, you end up with an analemma that is always
  vertically oriented, and always straddles the horizon.")

(defcard
  (sab/html
   [:div
    [:h3 "North Pole"]
    (analemma sat/earth 90 0 june-solstice)]))

(defcard
  "The South Pole is subject to the same dynamics as the North Pole, so that
  you always end up with a vertical analemma that straddles the horizon. The
  only difference is that it appears to be flipped end over end.")

(defcard
  (sab/html
   [:div
    [:h3 "South Pole"]
    (analemma sat/earth -90 0 june-solstice)]))

(defcard
  "## The Equator

  On the Equator, we have analemmas that are roughly the opposite of those at
  the poles. There, the sun rises and sets at almost the same time every day
  throughout the year, and so near sunrise and sunset, the analemma is
  horizontal relative to the horizon. As it moves through in the sky, its
  apparent orientation doesn't change.")

(defcard
  (sab/html
   [:div
    [:h3 "Equator at average sunrise"]
    (analemma sat/earth 0 0 #inst "2016-06-21T06:00:00.000-00:00")]))

(defcard
  "At noon it will be directly overhead and so the orientation is dependent on
  which direction you face - however, the small lobe of the analemma will
  always point north.")

(defcard
  (sab/html
   [:div
    [:h3 "Equator at noon"]
    (analemma sat/earth 0 0 #inst "2016-06-21T12:00:00.000-00:00")]))

(defcard
  "At sunset, the orientation appears to have flipped, but the only thing that
  has happened is that you're facing the opposite direction. The small lobe
  points north, and you have gone from looking east to looking west.")

(defcard
  (sab/html
   [:div
    [:h3 "Equator at average sunset"]
    (analemma sat/earth 0 0 #inst "2016-06-21T18:00:00.000-00:00")]))

(defcard
  "# Middle Latitudes

  The analemmas for middle latitudes change their orientation with time, with
  respect to the horizon. This is again a consequence of the small lobe of the
  analemma always pointing to the north celestial pole as the analemma crosses
  the sky, as it did in the other analemmas we've seen so far.")

(defcard
  (sab/html
   [:div
    [:h3 "45° North at average sunrise"]
    (analemma sat/earth 45 0 #inst "2016-06-21T06:00:00.000-00:00")]))

(defcard
  (sab/html
   [:div
    [:h3 "45° North at noon"]
    (analemma sat/earth 45 0 #inst "2016-06-21T12:00:00.000-00:00")]))

(defcard
  (sab/html
   [:div
    [:h3 "45° North at average sunset"]
    (analemma sat/earth 45 0 #inst "2016-06-21T18:00:00.000-00:00")]))

(defcard
  "To me, one of the strangest consequences of living on a sphere is that the
  sky on one side of the equator is \"upside down\" relative to the sky on the
  other side. You saw this above with the analemmas for the North and South
  Poles being flipped versions each other.")

(defcard
  (sab/html
   [:div
    [:h3 "45° South at average sunrise"]
    (analemma sat/earth -45 0 #inst "2016-06-21T06:00:00.000-00:00")]))

(defcard
  (sab/html
   [:div
    [:h3 "45° South at noon"]
    (analemma sat/earth -45 0 #inst "2016-06-21T12:00:00.000-00:00")]))

(defcard
  (sab/html
   [:div
    [:h3 "45° South at average sunset"]
    (analemma sat/earth -45 0 #inst "2016-06-21T18:00:00.000-00:00")]))
