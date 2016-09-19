(ns snowth.render
  (:require
   [snowth.astro :as astro]
   [snowth.projections :as proj]
   [clojure.string :as str]))

(defprotocol Render
  (-render [self projection]))

(defn view-box
  [projection]
  (let [min-max (juxt #(apply min %) #(apply max %))
        [min-x max-x] (min-max (map first projection))
        [min-y max-y] (min-max (map second projection))
        width (- max-x min-x)
        height (- max-y min-y)
        x-margin (if (= width 0)
                   .01
                   (/ width 10))
        y-margin (if (= height 0)
                   .01
                   (/ height 10))]
    [(- min-x x-margin) (- min-y y-margin)
     (+ width (* x-margin 2)) (+ height (* y-margin 2))]))

(def dots
  (reify Render
    (-render [_ projection]
      ;; svg coordinates are upside down
      (let [projection (map (fn [[x y]] [x (- y)]) projection)
            viewbox-data (view-box projection)]
        [:svg {:width 908 :height 908 :viewBox (str/join "," viewbox-data)}
         (into `([:circle {:cx ~(ffirst projection)
                           :cy ~(second (first projection))
                           :r .004363323129985824
                           :style {"fill" "red"}}])
               (map #(-> [:circle {:cx (first %)
                                   :cy (second %)
                                   :r .004363323129985824
                                   :style {"fill" "#a6e3f7"}}])
                    (drop 1 projection)))]))))

(def racetrack
  (reify Render
    (-render [_ projection]
      ;; svg coordinates are upside down
      (let [projection (map (fn [[x y]] [x (- y)]) projection)
            viewbox-data (view-box projection)
            first-date (first projection)
            path-d (conj (->> projection
                              (map #(str "L" (str/join " " %)))
                              (into [(str "M" (str/join " " first-date))]))
                         " Z")]
        [:svg {:width 908 :height 908 :viewBox (str/join "," viewbox-data)}
         [:path {:d (str/join " " path-d) :stroke "#a6e3f7" :stroke-linejoin "round"
                 :stroke-width .01 #_0.008726646259971648 :fill "none"}]
         [:circle {:cx (first first-date)
                   :cy (second first-date)
                   :r .004363323129985824
                   :style {"fill" "red"}}]]))))
