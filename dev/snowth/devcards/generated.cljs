(ns snowth.devcards.generated
  (:require
   [clojure.spec :as s]
   [devcards.core :as dc :refer-macros [defcard]]
   [sablono.core :as sab :include-macros true]
   [snowth.astro :as astro]
   [snowth.common :refer [pi]]
   [snowth.core :as core :refer [analemma]]))

(doseq [[args svg-data] (s/exercise-fn 'analemma)]
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
                  [:li [:strong "Eccentricity: "] eccentricity]
                  [:li [:strong "Ecliptic obliquity: "] (str obliquity "π radians ("
                                                             (* obliquity (/ 180 pi)) " degrees)")]
                  [:li [:strong "D's per orbit: "] ds-per-orbit]]
                 svg-data]))))
