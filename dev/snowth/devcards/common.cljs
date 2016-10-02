(ns snowth.devcards.common
  (:require
   [clojure.core.async :refer [chan <! put! close!]]
   [sablono.core :as sab :include-macros true]
   [snowth.core :as core :refer [analemma]])
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
      (.getCurrentPosition gl
                           #(put! location-ch (.-coords %))
                           #(put! location-ch defaults)
                           #js {:timeout 10000})
      (put! location-ch defaults))
    (go
      (swap! a (change-location (<! location-ch)))
      (close! location-ch))
    (js/setInterval #(swap! state assoc-in [:now] (js/Date.)) 30000)
    a))

(defn analemma-card
  [sat & opt-args]
  (fn [state-atom _]
    (let [{:keys [lat long now]} @state-atom]
      (when (and lat long)
        (sab/html
         (apply analemma sat lat long now opt-args))))))
