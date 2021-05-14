(ns snowth.devcards.colors
  (:require
   [cljsjs.chroma :as chroma]
   [cljsjs.d3 :as d3]
   [clojure.core.async :refer [chan <! put! close! timeout]]
   [clojure.spec.test.alpha :as s]
   [clojure.string :as str]
   [devcards.core :as dc :refer-macros [defcard]]
   [reagent.core :as r]
   [sablono.core :as sab]
   [snowth.core :refer [analemma]]
   [snowth.devcards.common :refer [analemma-card place-and-date-card default-lat default-long]]
   [snowth.projections :as proj]
   [snowth.render :as render]
   [snowth.satellites :as sat])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(defcard
  "# Animations and Color Scales

  Experiments with varying the color of the sun and sky with altitude, and a
  proving ground for making sure that `snowth` is flexible enough to be used
  for animations.

  These analemmas do not auto-update as time passes, because time updates
  during animations wouldn't make for a pleasant viewing experience. Instead,
  they reflect your time and location when the page was loaded. However, you
  can manually reset the time with the button below.")

(defonce state (r/atom {:now (js/Date.)
                        :lat default-lat
                        :long default-long}))

(defcard
  (place-and-date-card)
  state)

(defcard
  "## D3 examples
  These examples use D3 to animate the analemmas, over the course of a day, and
  at the same time every day for a year, respectively. Click to start the
  animation")

(defn d3-chroma-dots
  [projection horizon [_ center-horizon-y] [_ zenith-y] [_ nadir-y]]
  ;; svg coordinates are upside down
  (let [projection (map (fn [[x y]] [x (- y)]) projection)
        [x y width height :as viewbox-data] (render/view-box projection)
        horizon (->> horizon
                     (map (fn [[x y]] [x (- y)]))
                     (drop-while #(< (first %) x))
                     (take-while #(<= (first %) (+ x width))))
        horizon-d (->> (rest horizon)
                       (map #(str "L" (str/join " " %)))
                       (into [(str "M" (str/join " " (first horizon)))]))
        center-horizon-y (- center-horizon-y)
        zenith-y (- zenith-y)
        nadir-y (- nadir-y)
        horizon-pct (int (* 100 (/ (- center-horizon-y y) height)))]
    [projection center-horizon-y zenith-y nadir-y]))

(defn make-day-calc
  [global-state local-state]
  (fn [_]
    (let [{:keys [lat long now]} @global-state]
      (when (and lat long)
        (let [start-ms (.getTime now)
              end-ms (inc (+ start-ms (* 1000 60 60 24)))
              data
              (->>
               (range start-ms end-ms 900000)
               (map #(analemma
                      sat/earth lat long (js/Date. %) d3-chroma-dots
                      proj/orthographic))
               (map (fn [[projection horz-y zenith-y nadir-y]]
                      (let [above-horz (- horz-y
                                          (* 0.05 (- nadir-y zenith-y)))
                            below-horz (+ horz-y
                                          (* 0.05 (- nadir-y zenith-y)))
                            sun-scale (-> js/chroma
                                          (.scale
                                           #js ["#fdffe8" "#f9ff40" "#ff6000"
                                                "#e31e00" "#691048"])
                                          (.domain
                                           #js [zenith-y above-horz horz-y
                                                below-horz nadir-y])
                                          (.mode "lab"))]
                        [projection sun-scale]))))
              viewbox (render/view-box (mapcat first data))]
          (swap! local-state #(-> %
                                  (assoc :data data)
                                  (assoc :viewbox viewbox))))))))

(defn d3-day-component
  [global-state]
  (let [local-state (r/atom {})
        calc (make-day-calc global-state local-state)
        render
        (fn [_]
          (let [[[projection1 sun-scale1] & rest-data1] (:data @local-state)
                viewbox (:viewbox @local-state)]
            (when viewbox
              (-> js/d3
                  (.select "svg#d3-day")
                  (.attr "viewBox" (str/join "," viewbox))
                  (.on "click"
                       (fn []
                         (loop [[[projection sun-scale] & rest-data] rest-data1
                                t-parent (-> js/d3
                                             (.select "svg#d3-day"))]
                           (let [transition (-> t-parent
                                                .transition
                                                (.duration 125)
                                                (.ease (.-easeLinear js/d3)))]
                             (-> js/d3
                                 (.select "svg#d3-day")
                                 (.selectAll "circle.later")
                                 (.data (clj->js (rest projection)))
                                 (.transition transition)
                                 (.attr "cx" #(aget % 0))
                                 (.attr "cy" #(aget % 1))
                                 (.style "fill" #(.hex (sun-scale (aget % 1)))))
                             (-> js/d3
                                 (.select "svg#d3-day")
                                 (.selectAll "circle.first")
                                 (.data (clj->js (take 1 projection)))
                                 (.transition transition)
                                 (.attr "cx" #(aget % 0))
                                 (.attr "cy" #(aget % 1)))
                             (when-not (empty? rest-data)
                               (recur rest-data transition)))))))
              (-> js/d3
                  (.select "svg#d3-day")
                  (.selectAll "circle.later")
                  (.data (clj->js (rest projection1)))
                  (.attr "cx" #(aget % 0))
                  (.attr "cy" #(aget % 1))
                  .enter
                  (.append "circle")
                  (.attr "class" "later")
                  (.attr "cx" #(aget % 0))
                  (.attr "cy" #(aget % 1))
                  (.attr "r" .004363323129985824)
                  (.style "fill" #(.hex (sun-scale1 (aget % 1)))))
              (-> js/d3
                  (.select "svg#d3-day")
                  (.selectAll "circle.first")
                  (.data (clj->js (take 1 projection1)))
                  (.attr "cx" #(aget % 0))
                  (.attr "cy" #(aget % 1))
                  .enter
                  (.append "circle")
                  (.attr "class" "first")
                  (.attr "cx" #(aget % 0))
                  (.attr "cy" #(aget % 1))
                  (.attr "r" .004363323129985824)
                  (.style "fill" "#42f47a")))))]
    (r/create-class
     {:component-will-mount calc
      :component-did-mount render
      :component-will-update calc
      :component-did-update render

      :reagent-render
      (fn []
        (let [{:keys [lat long now viewbox]} @global-state]
          (when (and lat long)
            [:svg {:id "d3-day" :width "100%" :height 900
                   :data-time (str now)}])))})))

(defcard chroma-d3-day
  (dc/reagent d3-day-component)
  state)

(defn make-year-calc
  [global-state local-state]
  (fn [_]
    (let [{:keys [lat long now]} @global-state]
      (when (and lat long)
        (let [start-ms (.getTime now)
              start-year (.getFullYear now)
              end-ms (.setFullYear (js/Date. start-ms) (inc start-year))
              data
              (->>
               (range start-ms (inc end-ms) (* 1000 60 60 24))
               (map #(analemma
                      sat/earth lat long (js/Date. %) d3-chroma-dots
                      proj/orthographic))
               (map (fn [[projection horz-y zenith-y nadir-y]]
                      (let [above-horz (- horz-y
                                          (* 0.05 (- nadir-y zenith-y)))
                            below-horz (+ horz-y
                                          (* 0.05 (- nadir-y zenith-y)))
                            sun-scale (-> js/chroma
                                          (.scale
                                           #js ["#fdffe8" "#f9ff40" "#ff6000"
                                                "#e31e00" "#691048"])
                                          (.domain
                                           #js [zenith-y above-horz horz-y
                                                below-horz nadir-y])
                                          (.mode "lab"))]
                        [projection sun-scale]))))
              viewbox (render/view-box (mapcat first data))]
          (swap! local-state #(-> %
                                  (assoc :data data)
                                  (assoc :viewbox viewbox))))))))

(defn d3-year-component
  [global-state]
  (let [local-state (r/atom {})
        calc (make-year-calc global-state local-state)
        render
        (fn [_]
          (let [[[projection1 sun-scale1] & rest-data1] (:data @local-state)
                viewbox (:viewbox @local-state)]
            (when viewbox
              (-> js/d3
                  (.select "svg#d3-year")
                  (.attr "viewBox" (str/join "," viewbox))
                  (.on "click"
                       (fn []
                         (loop [[[projection sun-scale] & rest-data] rest-data1
                                t-parent (-> js/d3
                                             (.select "svg#d3-year"))]
                           (let [transition (-> t-parent
                                                .transition
                                                (.duration 125)
                                                (.ease (.-easeLinear js/d3)))]
                             (-> js/d3
                                 (.select "svg#d3-year")
                                 (.selectAll "circle.later")
                                 (.data (clj->js (rest projection)))
                                 (.transition transition)
                                 (.attr "cx" #(aget % 0))
                                 (.attr "cy" #(aget % 1))
                                 (.style "fill" #(.hex (sun-scale (aget % 1)))))
                             (-> js/d3
                                 (.select "svg#d3-year")
                                 (.selectAll "circle.first")
                                 (.data (clj->js (take 1 projection)))
                                 (.transition transition)
                                 (.attr "cx" #(aget % 0))
                                 (.attr "cy" #(aget % 1)))
                             (when-not (empty? rest-data)
                               (recur rest-data transition)))))))
              (-> js/d3
                  (.select "svg#d3-year")
                  (.selectAll "circle.later")
                  (.data (clj->js (rest projection1)))
                  .enter
                  (.append "circle")
                  (.attr "class" "later")
                  (.attr "cx" #(aget % 0))
                  (.attr "cy" #(aget % 1))
                  (.attr "r" .004363323129985824)
                  (.style "fill" #(.hex (sun-scale1 (aget % 1)))))
              (-> js/d3
                  (.select "svg#d3-year")
                  (.selectAll "circle.first")
                  (.data (clj->js (take 1 projection1)))
                  .enter
                  (.append "circle")
                  (.attr "class" "first")
                  (.attr "cx" #(aget % 0))
                  (.attr "cy" #(aget % 1))
                  (.attr "r" .004363323129985824)
                  (.style "fill" "#42f47a")))))]
    (r/create-class
     {:component-will-mount calc
      :component-did-mount render
      :component-will-update calc
      :component-did-update render

      :reagent-render
      (fn []
        (let [{:keys [lat long now viewbox]} @global-state]
          (when (and lat long)
            [:svg {:id "d3-year" :width "100%" :height 900
                   :data-time (str now)}])))})))

(defcard chroma-d3-year
  (dc/reagent d3-year-component)
  state)

(defcard
  "## Color gradients

  This idea is still a bit half-baked, but what I'm trying to do here is vary
  the background sky and the sun color based on altitude, to approximate kind
  of a real-life composite of what you would actually see over the course of a
  year. That is, if you could see through the earth, and also that it produced
  a more pronounced reddening effect than the atmosphere does.")

(defn chroma-dots
  [projection horizon [_ center-horizon-y] [_ zenith-y] [_ nadir-y]]
  ;; svg coordinates are upside down
  (let [projection (map (fn [[x y]] [x (- y)]) projection)
        [x y width height :as viewbox-data] (render/view-box projection)
        horizon (->> horizon
                     (map (fn [[x y]] [x (- y)]))
                     (drop-while #(< (first %) x))
                     (take-while #(<= (first %) (+ x width))))
        horizon-d (->> (rest horizon)
                       (map #(str "L" (str/join " " %)))
                       (into [(str "M" (str/join " " (first horizon)))]))
        center-horizon-y (- center-horizon-y)
        zenith-y (- zenith-y)
        nadir-y (- nadir-y)
        above-horizon-stop (- center-horizon-y
                              (* 0.05
                                 (- nadir-y zenith-y)))
        below-horizon-stop (+ center-horizon-y
                              (* 0.05
                                 (- nadir-y zenith-y)))
        horizon-pct (int (* 100 (/ (- center-horizon-y y)
                                   (- nadir-y zenith-y))))
        sun-scale (-> js/chroma
                      (.scale #js ["#fdffe8" "#f9ff40" "#ff6000"
                                   "#e31e00" "#691048"])
                      (.mode "lab")
                      (.domain #js [zenith-y
                                    above-horizon-stop
                                    center-horizon-y
                                    below-horizon-stop
                                    nadir-y]))]
    [:svg {:width "100%" :height 900 :viewBox (str/join "," viewbox-data)}
     (concat
      [[:defs
        [[:linearGradient {:id "sky-gradient" :x1 "0" :x2 "0" :y1 "0" :y2 "1"}
          [[:stop {:offset "0%" :stop-color "#a6e3f7"}]
           [:stop {:offset (str horizon-pct "%") :stop-color "#2f3e7a"}]
           [:stop {:offset "100%" :stop-color "#162047"}]]]]]
       [:rect {:x x :y zenith-y :width width
               :height (- nadir-y zenith-y) :fill "url(#sky-gradient)"}]]
      (map #(-> [:circle {:cx (first %)
                          :cy (second %)
                          :r .004363323129985824
                          :style {"fill" (.hex (sun-scale (second %)))}}])
           (rest projection))
      [[:circle {:cx (ffirst projection)
                 :cy (second (first projection))
                 :r .004363323129985824
                 :style {"fill" "#72f276"}}]])]))

(defcard chroma
  (analemma-card sat/earth chroma-dots)
  state)
