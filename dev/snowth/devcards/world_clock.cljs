(ns snowth.devcards.world-clock
  (:require
   [devcards.core :as dc :refer-macros [defcard]]
   [sablono.core :as sab :include-macros true]
   [snowth.core :as core :refer [analemma]]
   [snowth.devcards.common :refer [state]]
   [snowth.satellites :as sat]))

(defcard
  "# World Clock

  An analemma's orientation changes based on the latitude and time of day,
  which means that an analemma can be used as sort of a fuzzy clock. In fact,
  the shadow of a sundial will create the same analemma pattern over the
  course of a year as directly imaging the sun would.")

(def cities
  [{:city "Chicago"
    :lat 41.8781
    :long -87.6298}
   {:city "Vancouver"
    :lat 49.2827
    :long -123.1207}
   {:city "Auckland"
    :lat -36.8485
    :long 174.7633}
   {:city "Sydney"
    :lat -33.8688
    :long 151.2093}
   {:city "Beijing"
    :lat 39.9042
    :long 116.4074}
   {:city "Jakarta"
    :lat -6.1745
    :long 106.8227}
   {:city "New Delhi"
    :lat 28.6139
    :long 77.2090}
   {:city "Moscow"
    :lat 55.7558
    :long 37.6173}
   {:city "Nairobi"
    :lat -1.2921
    :long 36.8219}
   {:city "Cairo"
    :lat 30.0444
    :long 31.2357}
   {:city "Istanbul"
    :lat 41.0082
    :long 28.9784}
   {:city "Stockholm"
    :lat 59.3293
    :long 18.0686}
   {:city "Rome"
    :lat 41.9028
    :long 12.4964}
   {:city "Lagos"
    :lat 6.5244
    :long 3.3792}
   {:city "Rio de Janeiro"
    :lat -22.9068
    :long -43.1729}
   {:city "Lima"
    :lat -12.0464
    :long -77.0428}])

(run! #(let [{:keys [city lat long]} %]
         (defcard
           (fn [state-atom _]
             (sab/html
              [:div
               [:h2 city]
               (analemma sat/earth lat long (:now @state-atom))]))
           state))
      cities)
