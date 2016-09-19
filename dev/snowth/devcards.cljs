(ns snowth.devcards
  (:require
   [snowth.astro :as astro]
   [snowth.core :refer [analemma]]
   [snowth.projections :as proj]
   [snowth.render :as render :refer [dots racetrack]]
   [snowth.satellites :as sat]
   [clojure.spec :as s]
   [clojure.spec.test :as test]
   [devcards.core :refer-macros [defcard]]
   [sablono.core :as sab :include-macros true]))

(def now (js/Date.))

(defcard dots-ortho
  (sab/html
   (analemma sat/earth 44.217 -88.344 now)))

(defcard racetrack-ortho
  (sab/html
   (analemma sat/earth 44.217 -88.344 now racetrack)))

(defcard dots-stereo
  (sab/html
   (analemma sat/earth 44.217 -88.344 now dots proj/stereographic)))

(defcard racetrack-stereo
  (sab/html
   (analemma sat/earth 44.217 -88.344 now racetrack proj/stereographic)))

(defcard mars
  (sab/html
   (analemma sat/mars 44.217 -88.344 now)))

(defcard mercury
  (sab/html
   (analemma sat/mercury 44.217 -88.344 now)))

(defcard venus
  (sab/html
   (analemma sat/venus 44.217 -88.344 now)))

(defcard jupiter
  (sab/html
   (analemma sat/jupiter 44.217 -88.344 now)))

(defcard saturn
  (sab/html
   (analemma sat/saturn 44.217 -88.344 now)))

(defcard uranus
  (sab/html
   (analemma sat/uranus 44.217 -88.344 now)))

(defcard neptune
  (sab/html
   (analemma sat/neptune 44.217 -88.344 now)))

(doseq [[args coords] (s/exercise-fn 'astro/analemma-coords)]
  (let [conformed (s/conform ::astro/analemma-args args)
        center (proj/center-info coords)
        svg-data (->> coords
                      (map astro/alt-az)
                      (map #(proj/orthographic center %))
                      (render/-render dots))]
    (defcard analemma
      (sab/html [:div
                 [:p (str (:latitude conformed) ", "
                          (:longitude conformed) " at "
                          (:datetime conformed))]
                 svg-data]))))
