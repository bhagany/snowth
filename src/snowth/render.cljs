(ns snowth.render
  (:require
   [clojure.spec :as s]
   [clojure.string :as str]
   [snowth.astro :as astro]
   [snowth.projections :as proj]))

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

(defn dots
  [projection horizon _ _ _]
  ;; svg coordinates are upside down
  (let [projection (map (fn [[x y]] [x (- y)]) projection)
        [x _ width _ :as viewbox-data] (view-box projection)
        horizon (->> horizon
                     (map (fn [[x y]] [x (- y)]))
                     (drop-while #(< (first %) x))
                     (take-while #(<= (first %) (+ x width))))
        horizon-d (->> (rest horizon)
                       (map #(str "L" (str/join " " %)))
                       (into [(str "M" (str/join " " (first horizon)))]))
        dot-and-horizon (list [:circle {:cx (ffirst projection)
                                        :cy (second (first projection))
                                        :r .004363323129985824
                                        :style {"fill" "red"}}]
                              [:path {:d (str/join " " horizon-d)
                                      :stroke "black" :stroke-width .0005
                                      :fill "none"}])]
    [:svg {:width "100%" :height 900 :viewBox (str/join "," viewbox-data)}
     (->> (rest projection)
          (map #(-> [:circle {:cx (first %)
                              :cy (second %)
                              :r .004363323129985824
                              :style {"fill" "#a6e3f7"}}]))
          (into dot-and-horizon))]))

(defn racetrack
  [projection horizon _ _ _]
  ;; svg coordinates are upside down
  (let [projection (map (fn [[x y]] [x (- y)]) projection)
        [x _ width _ :as viewbox-data] (view-box projection)
        horizon (->> horizon
                     (map (fn [[x y]] [x (- y)]))
                     (drop-while #(< (first %) x))
                     (take-while #(<= (first %) (+ x width))))
        first-date (first projection)
        path-d (conj (->> (rest projection)
                          (map #(str "L" (str/join " " %)))
                          (into [(str "M" (str/join " " first-date))]))
                     " Z")
        horizon-d (->> (rest horizon)
                       (map #(str "L" (str/join " " %)))
                       (into [(str "M" (str/join " " (first horizon)))]))]
    [:svg {:width "100%" :height 900 :viewBox (str/join "," viewbox-data)}
     [[:path {:d (str/join " " path-d) :stroke "#a6e3f7" :stroke-linejoin "round"
              :stroke-width .01 #_0.008726646259971648 :fill "none"}]
      [:circle {:cx (first first-date)
                :cy (second first-date)
                :r .004363323129985824
                :style {"fill" "red"}}]
      [:path {:d (str/join " " horizon-d) :stroke "black"
              :stroke-width .0005 :fill "none"}]]]))

(s/def ::render-fn
  (s/with-gen
    (s/fspec :args (s/cat :projection (s/coll-of ::proj/point)
                          :horizon (s/coll-of ::proj/point)
                          :center-horizon (s/spec ::proj/point)
                          :zenith (s/spec ::proj/point)
                          :nadir (s/spec ::proj/point)))
    #(s/gen #{dots racetrack})))
