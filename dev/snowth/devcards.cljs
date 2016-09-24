(ns snowth.devcards
  (:require
   [clojure.core.async :refer [chan <! >! put! close! timeout]]
   [clojure.spec :as s]
   [clojure.spec.test :as test]
   [devcards.core :as dc :refer-macros [defcard]]
   [sablono.core :as sab :include-macros true]
   [snowth.astro :as astro]
   [snowth.core :as core :refer [analemma]]
   [snowth.projections :as proj]
   [snowth.render :as render :refer [dots racetrack]]
   [snowth.satellites :as sat])
  (:require-macros [cljs.core.async.macros :refer [go]]))

;; Hawthorne Fish House, Portland OR
(def default-lat 45.5121466)
(def default-long -122.6196392)

(defn change-location
  [coords]
  #(-> %
       (assoc-in [:lat] (or (.-latitude coords) default-lat))
       (assoc-in [:long] (or (.-longitude coords) default-long))))

(defonce state
  (let [a (atom {:now (js/Date.)})
        location-ch (chan)
        defaults #js {:latitude default-lat :longitude default-long}]
    (if-let [gl (.-geolocation js/navigator)]
      (.getCurrentPosition gl #(put! location-ch (.-coords %)))
      (put! location-ch defaults))
    (go
      (let [[val ch] (alts! [location-ch (timeout 10000)])
            coords (if (= ch location-ch)
                     val
                     defaults)]
        (swap! a (change-location coords)))
      (close! location-ch))
    (js/setInterval #(swap! state assoc-in [:now] (js/Date.)) 30000)
    a))

(defn analemma-card
  [sat & opt-args]
  (fn [state-atom _]
    (let [{:keys [lat long now]} @state-atom]
      (if (and lat long)
        (sab/html
         (apply analemma sat lat long now opt-args))))))

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

(defcard mars
  (analemma-card sat/mars)
  state)

(defcard mercury
  (analemma-card sat/mercury)
  state)

(defcard venus
  (analemma-card sat/venus)
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

(doseq [[args svg-data] (s/exercise-fn 'analemma)]
  (let [conformed (s/conform ::core/analemma-args args)]
    (defcard analemma
      (sab/html [:div
                 [:p (str (:latitude conformed) ", "
                          (:longitude conformed) " at "
                          (:datetime conformed))]
                 svg-data]))))

(dc/start-devcard-ui!)
