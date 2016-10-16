(ns snowth.devcards.generated
  (:require
   [clojure.spec :as s]
   [devcards.core :as dc :refer-macros [defcard]]
   [sablono.core :as sab :include-macros true]
   [snowth.astro :as astro]
   [snowth.common :refer [pi]]
   [snowth.core :as core :refer [analemma]]))

(defcard
  "# Randomly generated satellites

  Now this is pretty cool. Thanks to the magic of `clojure.spec`, the code
  specification for a satellite in `snowth` can be used to randomly generate
  satellites that conform to itself. This can be used to test that the code for
  handling satellites doesn't break, and also to show you a bunch of cool
  analemmas for planets that don't necessarily exist.

  These analemmas will be unique for every page load, and I can't guarantee
  that each one will be displayed optimally, but they are usually pretty good.
  Enjoy!")

(doseq [[args svg-data] (s/exercise-fn 'analemma 20)]
  (let [{:keys [satellite latitude longitude datetime]}
        (s/conform ::core/analemma-args args)
        start-d (astro/datetime->d satellite datetime)
        eccentricity (astro/eccentricity satellite start-d)
        obliquity (/ (astro/ecliptic-obliquity satellite start-d) pi)
        ds-per-orbit (astro/ds-per-orbit satellite)]
    (defcard analemma
      (sab/html [:div
                 [:ul
                  [:li [:strong "Lat, Long: "] (str latitude ", " longitude)]
                  [:li [:strong "Orbital Eccentricity (0=circle, 1=parabola): "] eccentricity]
                  [:li [:strong "Ecliptic obliquity: "] (str obliquity "π radians ("
                                                             (* obliquity (/ 180 pi)) " degrees)")]
                  [:li [:strong "D's per orbit: "] ds-per-orbit]]
                 svg-data]))))
