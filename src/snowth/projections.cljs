(ns snowth.projections
  (:require
   [clojure.spec :as s]
   [snowth.astro :as astro]
   [snowth.common :as c :refer [sin cos sqrt]]))

(defprotocol Project
  (project* [self center alt-az]))

(defn project
  [projector center alt-az]
  (project* projector center alt-az))

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

(def orthographic
  (reify Project
    (project* [_ center [alt az]]
      (let [{:keys [::astro/alt-az ::sin-center-alt ::cos-center-alt]} center
            [_ center-az] alt-az
            delta-az (- az center-az)
            x (* (cos alt) (sin delta-az))
            y (- (* (sin alt)
                    cos-center-alt)
                 (* (cos alt)
                    (cos delta-az)
                    sin-center-alt))]
        [x y]))))

(def stereographic
  (reify Project
    (project* [_ center [alt az :as pt-alt-az]]
      (let [{:keys [::astro/alt-az ::sin-center-alt ::cos-center-alt]} center
            [_ center-az] alt-az
            [x* y*] (project orthographic center pt-alt-az)
            z* (+ (* (sin alt)
                     sin-center-alt)
                  (* (cos alt)
                     (cos (- az center-az))
                     cos-center-alt))
            scale-factor (/ 2 (+ z* 1))
            x (if (= scale-factor js/Infinity)
                js/Number.MAX_VALUE
                (* x* scale-factor))
            y (if (= scale-factor js/Infinity)
                js/Number.MAX_VALUE
                (* y* scale-factor))]
        [x y]))))

(s/def ::projector (s/with-gen
                     #(satisfies? Project %)
                     #(s/gen #{orthographic stereographic})))
(s/def ::trig-range (s/and ::c/not-nan #(<= -1 % 1)))
(s/def ::sin-center-alt ::trig-range)
(s/def ::cos-center-alt ::trig-range)
(s/def ::center-info (s/keys :req [::astro/alt-az
                                   ::sin-center-alt
                                   ::cos-center-alt]))
(s/def ::point (s/tuple (s/and ::c/not-nan #(not= % js/Infinity))
                        (s/and ::c/not-nan #(not= % js/Infinity))))
(s/fdef project
        :args (s/cat :projector ::projector
                     :center ::center-info
                     :alt-az (s/spec ::astro/alt-az))
        :ret ::point)
