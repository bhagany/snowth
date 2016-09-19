(ns snowth.projections
  (:require
   [snowth.astro :as astro]))

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
    {:center-alt alt
     :center-az az
     :sin-center-alt (astro/sin alt)
     :cos-center-alt (astro/cos alt)}))

(defn orthographic
  [center [alt az]]
  (let [{:keys [:center-az :sin-center-alt :cos-center-alt]} center
        delta-az (- az center-az)
        x (* (astro/cos alt) (astro/sin delta-az))
        y (- (* (astro/sin alt)
                cos-center-alt)
             (* (astro/cos alt)
                (astro/cos delta-az)
                sin-center-alt))]
    [x y]))

(defn stereographic
  [center [alt az :as alt-az]]
  (let [{:keys [:center-az :sin-center-alt :cos-center-alt]} center
        [x* y*] (orthographic center alt-az)
        z* (+ (* (astro/sin alt)
                 sin-center-alt)
              (* (astro/cos alt)
                 (- az center-az)
                 cos-center-alt))
        scale-factor (/ 2 (+ z* 1))
        x (* x* scale-factor)
        y (* y* scale-factor)]
    [x y]))
