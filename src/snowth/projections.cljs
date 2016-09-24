(ns snowth.projections
  (:require
   [clojure.spec :as s]
   [snowth.astro :as astro]
   [snowth.common :as c :refer [sin cos sqrt]]))

(defn center-info
  [coords]
  (let [min-max (juxt #(apply min %) #(apply max %))
        [min-x max-x] (min-max (map first coords))
        [min-y max-y] (min-max (map second coords))
        [min-z max-z] (min-max (map #(get % 2) coords))
        width (- max-x min-x)
        height (- max-y min-y)
        depth (- max-z min-z)
        [alt az] (astro/alt-az [(+ min-x (/ width 2))
                                (+ min-y (/ height 2))
                                (+ min-z (/ depth 2))])]
    {::astro/alt-az [alt az]
     ::sin-center-alt (sin alt)
     ::cos-center-alt (cos alt)}))

(defn orthographic
  [center [alt az]]
  (let [{:keys [::astro/alt-az ::sin-center-alt ::cos-center-alt]} center
        [_ center-az] alt-az
        delta-az (- az center-az)
        x (* (cos alt) (sin delta-az))
        y (- (* (sin alt)
                cos-center-alt)
             (* (cos alt)
                (cos delta-az)
                sin-center-alt))]
    [x y]))

(defn stereographic
  [center [alt az :as pt-alt-az]]
  (let [{:keys [::astro/alt-az ::sin-center-alt ::cos-center-alt]} center
        [_ center-az] alt-az
        [x* y*] (orthographic center pt-alt-az)
        z* (+ (* (sin alt)
                 sin-center-alt)
              (* (cos alt)
                 (cos (- az center-az))
                 cos-center-alt))
        scale-factor (/ 2 (+ z* 1))
        x (* x* scale-factor)
        y (* y* scale-factor)]
    [x y]))

(s/def ::trig-range (s/and ::c/not-nan #(<= -1 % 1)))
(s/def ::sin-center-alt ::trig-range)
(s/def ::cos-center-alt ::trig-range)
(s/def ::center-info (s/keys :req [::astro/alt-az
                                   ::sin-center-alt
                                   ::cos-center-alt]))
(s/def ::point (s/tuple ::c/not-nan ::c/not-nan))

(s/def ::projection-fn
  (s/with-gen
    (s/fspec :args (s/cat :center ::center-info
                          :alt-az (s/spec ::astro/alt-az))
             :ret ::point)
    #(s/gen #{orthographic stereographic})))
