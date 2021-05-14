(ns snowth.devcards.common
  (:require
   [clojure.core.async :refer [chan <! put! close!]]
   [reagent.core :as r]
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
  (let [s (r/atom {:now (js/Date.)
                   :lat default-lat
                   :long default-long})]
    (js/setInterval #(swap! s assoc-in [:now] (js/Date.)) 30000)
    s))

(defn analemma-card
  [sat & opt-args]
  (fn [state-atom _]
    (let [{:keys [lat long now]} @state-atom]
      (when (and lat long)
        (sab/html
         (apply analemma sat lat long now opt-args))))))

(defn date-hiccup
  [state]
  [:div {:style {:marginTop "20px"}}
   [:div [:strong "Analemmas generated at: "] (str (:now @state))]
   [:button
    {:onClick (fn [] (swap! state #(assoc % :now (js/Date.))))}
    "Reset time"]])

(defn place-hiccup
  [state]
  [:div
   [:div [:strong "Lat: "] (:lat @state)]
   [:div [:strong "Long: "] (:long @state)]
   (when-let [gl (.-geolocation js/navigator)]
     [:button
      {:onClick (fn []
                  (.getCurrentPosition gl
                                       #(swap! state (change-location (.-coords %)))
                                       #(.log js/console "location get failed " %)
                                       #js {:timeout 10000}))}
      "Use your location"])])

(defn place-card
  []
  (fn [state _]
    (sab/html
     (place-hiccup state))))

(defn place-and-date-card
  []
  (fn [state _]
    (sab/html
     [:div
      (place-hiccup state)
      (date-hiccup state)])))
