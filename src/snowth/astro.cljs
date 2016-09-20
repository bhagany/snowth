(ns snowth.astro
  "Astronomical calculations needed for computing an analemma"
  (:require
   [snowth.satellites :as sat]
   [clojure.pprint :refer [pprint]]
   [clojure.spec :as s]
   [clojure.string :as str]
   [clojure.test.check.generators :as gen]))

(def pi (.-PI js/Math))

(defn sin
  [n]
  (.sin js/Math n))

(defn cos
  [n]
  (.cos js/Math n))

(defn atan2
  [y x]
  (.atan2 js/Math y x))

(defn sqrt
  [n]
  (.sqrt js/Math n))

(defn abs
  [n]
  (.abs js/Math n))

(def min-datetime #inst "1900")
(def max-datetime #inst "2100")

(s/def ::valid-datetime (s/inst-in min-datetime max-datetime))
(s/def ::ref number?)
(s/def ::step (s/with-gen
                (s/and number? #(<= -1 % 1))
                (fn [] (gen/fmap #(/ % 100000000) (s/gen (s/int-in 0 1000))))))
(s/def ::not-nan (s/and number? #(not (js/isNaN %))))
(s/def ::eccentricity-step (s/with-gen
                             (s/and ::not-nan #(<= -1 % 1))
                             (fn [] (gen/fmap #(/ % 1000000000000000)
                                              (s/gen (s/int-in 0 1000))))))
(s/def ::angle (s/and ::not-nan #(<= (- pi) % pi)))
(s/def ::positive-angle (s/and ::not-nan #(<= 0 % (* 2 pi))))
(s/def ::half-angle (s/and ::not-nan #(<= (/ pi -2) % (/ pi 2))))
(s/def ::positive-half-angle (s/and ::not-nan #(<= 0 % pi)))
(s/def ::eccentricity (s/and ::not-nan #(< 0 % 1)))

(s/def ::root-satellite-args
  (s/cat :epoch ::valid-datetime
         :ms-per-d pos-int?
         :periapsis-start ::positive-angle
         :periapsis-step ::step
         :eccentricity-start ::eccentricity
         :mean-anomaly-start ::positive-angle
         :mean-anomaly-step (s/and ::positive-angle #(not= 0 %))
         :ecliptic-obliquity-start ::positive-angle
         :ecliptic-obliquity-step ::step
         :rotation-offset ::positive-angle))

(defn satellite-arg-gen
  "Generates random satellites"
  []
  (gen/bind
   (s/gen ::root-satellite-args)
   (fn [args]
     (let [{:keys [epoch ms-per-d periapsis-start periapsis-step
                   eccentricity-start mean-anomaly-start mean-anomaly-step
                   ecliptic-obliquity-start ecliptic-obliquity-step
                   rotation-offset]}
           (s/conform ::root-satellite-args args)
           epoch-ms (.getTime epoch)
           min-d (/ (- (.getTime min-datetime) epoch-ms) ms-per-d)
           max-d (/ (- (.getTime max-datetime) epoch-ms) ms-per-d)
           step-numerator (if (< eccentricity-start .5)
                            eccentricity-start
                            (- 1 eccentricity-start))
           step-denominator (if (< max-d (abs min-d))
                              (- min-d 10)
                              (+ max-d 10))
           eccentricity-step (/ step-numerator step-denominator)]
       (gen/return
        (reify sat/Satellite
          (-datetime->d [_ datetime]
            (/ (- (.getTime datetime) epoch-ms)
               ms-per-d))
          (-argument-of-periapsis [_ d]
            (+ periapsis-start (* periapsis-step d)))
          (-eccentricity [_ d]
            (+ eccentricity-start (* eccentricity-step d)))
          (-mean-anomaly [_ d]
            (+ mean-anomaly-start (* mean-anomaly-step d)))
          (-ecliptic-obliquity [_ d]
            (+ ecliptic-obliquity-start (* ecliptic-obliquity-step d)))
          (-rotation [_ d]
            (+ rotation-offset (* 2 pi (- d (int d)))))
          (-ds-per-orbit [_]
            (/ (* 2 pi) mean-anomaly-step))))))))

(s/def ::satellite (s/with-gen
                     #(satisfies? sat/Satellite %)
                     #(gen/one-of [(satellite-arg-gen)
                                   (s/gen #{sat/mercury sat/venus sat/earth
                                            sat/mars sat/jupiter sat/saturn
                                            sat/uranus sat/neptune})])))

(defn datetime->d
  "Converts a conventional datetime into a d number, specific to the satellite"
  [satellite datetime]
  (sat/-datetime->d satellite datetime))

(s/fdef datetime->d
        :args (s/cat :satellite ::satellite
                     :datetime ::valid-datetime)
        :ret ::not-nan)

(defn constrain-angle
  "Constrains an angle to be between 0 and the provided constraint, through
  rotating by the constraint"
  [angle constraint]
  (let [a (js-mod angle constraint)]
    (if (>= a 0)
      a
      (+ a constraint))))

(defn argument-of-periapsis
  "The angle between a satellite's ascending node and its periapsis"
  [satellite d]
  (constrain-angle (sat/-argument-of-periapsis satellite d) (* 2 pi)))

(defn satellite-d-gen
  "Generates a satellite/d pair, such that the d number is appropriate"
  []
  (gen/bind
   (s/gen ::satellite)
   (fn [sat]
     (gen/tuple
      (gen/return sat)
      (s/gen (s/and ::not-nan
                    #(>= % (datetime->d
                            sat
                            (js/Date. (+ min-datetime 1))))
                    #(<= % (datetime->d
                            sat
                            (js/Date. (- max-datetime 1))))))))))

(s/def ::satellite-d-args  (s/with-gen
                             (s/cat :satellite ::satellite
                                    :d ::not-nan)
                             satellite-d-gen))

(s/fdef argument-of-periapsis
        :args ::satellite-d-args
        :ret ::positive-angle)

(defn eccentricity
  "A measure of how round or elliptical an orbit is"
  [satellite d]
  (sat/-eccentricity satellite d))

(s/fdef eccentricity
        :args ::satellite-d-args
        :ret ::eccentricity)

(defn ecliptic-obliquity
  "The angle by which the satellite's equator is inclined to the plane
  of its orbit"
  [satellite d]
  (constrain-angle (sat/-ecliptic-obliquity satellite d) pi))

(s/fdef ecliptic-obliquity
        :args ::satellite-d-args
        :ret ::positive-half-angle)

(defn mean-anomaly
  "The angle between where the satellite would be and its periapsis,
  if the orbit were circular"
  [satellite d]
  (constrain-angle (sat/-mean-anomaly satellite d) (* 2 pi)))

(s/fdef mean-anomaly
        :args ::satellite-d-args
        :ret ::positive-angle)

(defn rotation
  "The angle by which the satellite has rotated since its last full rotation"
  [satellite d]
  (constrain-angle (sat/-rotation satellite d) (* 2 pi)))

(s/fdef rotation
        :args ::satellite-d-args
        :ret ::positive-angle)

(defn ds-per-orbit
  "The number of rotations per orbit, relative to the object the satellite
  is orbiting"
  [satellite]
  (sat/-ds-per-orbit satellite))

(s/fdef ds-per-orbit
        :args (s/cat :satellite ::satellite)
        :ret ::not-nan)

(defn eccentric-anomaly
  "It's hard to explain with words, but this is an orbital parameter related
  to a satellites eccentricity, and how far it has travelled in its orbit
  since the last periapsis"
  [mean-anomaly eccentricity]
  (loop [ecc-anomaly* (+ mean-anomaly
                         (* eccentricity
                            (sin mean-anomaly)
                            (+ 1
                               (* eccentricity (cos mean-anomaly)))))]
    (let [ecc-anomaly (- ecc-anomaly*
                         (/ (- ecc-anomaly*
                               (* eccentricity
                                  (sin ecc-anomaly*))
                               mean-anomaly)
                            (- 1
                               (* eccentricity
                                  (cos ecc-anomaly*)))))]
      (if (<= (abs (- ecc-anomaly* ecc-anomaly)) .00001)
        ecc-anomaly
        (recur ecc-anomaly)))))

(defn true-anomaly-components
  "Gives the true position of the satellite in the plane of its orbit
  reltaive to perihelion as a point, scaled to the unit circle"
  [e-anomaly eccentricity]
  (let [true-x (- (cos e-anomaly) eccentricity)
        true-y (* (sqrt (- 1 (* eccentricity eccentricity)))
                  (sin e-anomaly))]
    [true-x true-y]))

(defn horiz-coords
  "Gives the 3-dimensional cartesian coordinates relative to a viewer's
  horizon, scaled to a unit circle"
  [satellite latitude-radians longitude-radians d]
  (let [m-anomaly (mean-anomaly satellite d)
        ecc (eccentricity satellite d)
        rotn (rotation satellite d)
        e-anomaly (eccentric-anomaly m-anomaly ecc)
        [true-x true-y] (true-anomaly-components e-anomaly ecc)
        distance (sqrt (+ (* true-x true-x) (* true-y true-y)))
        t-anomaly (atan2 true-y true-x)
        arg-of-periapsis (argument-of-periapsis satellite d)
        solar-longitude (js-mod (+ t-anomaly arg-of-periapsis) (* 2 pi))
        mean-solar-longitude (js-mod (+ m-anomaly arg-of-periapsis) (* 2 pi))
        ecliptic-x (* distance (cos solar-longitude))
        ecliptic-y (* distance (sin solar-longitude))
        ecl-obliquity (ecliptic-obliquity satellite d)
        equatorial-x ecliptic-x
        equatorial-y (* ecliptic-y (cos ecl-obliquity))
        equatorial-z (* ecliptic-y (sin ecl-obliquity))
        right-ascension (atan2 equatorial-y equatorial-x)
        declination (atan2 equatorial-z
                           (sqrt (+ (* equatorial-x equatorial-x)
                                    (* equatorial-y equatorial-y))))
        mean-midnight-longitude (+ mean-solar-longitude pi)
        prime-mean-sidereal-time (+ mean-midnight-longitude rotn)
        sidereal-time (constrain-angle
                       (+ prime-mean-sidereal-time longitude-radians)
                       (* 2 pi))
        hour-angle (- sidereal-time right-ascension)
        rect-x (* (cos hour-angle) (cos declination))
        rect-y (* (sin hour-angle) (cos declination))
        rect-z (sin declination)
        horiz-x (- (* rect-x (sin latitude-radians))
                   (* rect-z (cos latitude-radians)))
        horiz-y rect-y
        horiz-z (+ (* rect-x (cos latitude-radians))
                   (* rect-z (sin latitude-radians)))]
    [horiz-x horiz-y horiz-z]))

(defn horiz-coords-args-gen
  "Generates random positions on random satellites on random days"
  []
  (gen/bind
   (satellite-d-gen)
   (fn [[sat d]]
     (gen/tuple
      (gen/return sat)
      (s/gen ::half-angle)
      (s/gen ::angle)
      (gen/return d)))))

(s/def ::horiz-coords-args (s/with-gen
                             (s/cat :satellite ::satellite
                                    :latitude-radians ::half-angle
                                    :longitude-radians ::angle
                                    :d ::not-nan)
                             horiz-coords-args-gen))
(s/def ::normal-coord (s/and ::not-nan #(<= -1 % 1)))
(s/def ::horiz-coords (s/cat :x ::normal-coord
                             :y ::normal-coord
                             :z ::normal-coord))

(s/fdef horiz-coords
        :args ::horiz-coords-args
        :ret ::horiz-coords)

(defn alt-az
  "Converts 3-dimensional horizontal coordinates to altitude and azimuth"
  [[horiz-x horiz-y horiz-z]]
  (let [altitude (.asin js/Math horiz-z)
        azimuth (+ pi (atan2 horiz-y horiz-x))]
    [altitude azimuth]))

(s/def ::alt-az (s/cat :altitude ::half-angle
                       :azimuth ::positive-angle))
(s/fdef alt-az
        :args (s/cat :coords (s/spec ::horiz-coords))
        :ret ::alt-az)

(defn analemma-coords
  "Generates a satellite's analemma in 3-dimensional horizontal coordinates"
  [satellite latitude longitude datetime]
  (let [d (datetime->d satellite datetime)
        dpo (ds-per-orbit satellite)
        latitude-rad (* latitude (/ pi 180))
        longitude-rad (* longitude (/ pi 180))
        d-step (.max js/Math 1 (int (/ dpo 120)))
        num-samples* (/ dpo d-step)
        num-samples (if (int? num-samples*)
                      num-samples*
                      (int (+ 1 num-samples*)))]
    (->> (range d (+ d dpo) d-step)
         (take num-samples)
         (map #(horiz-coords satellite latitude-rad longitude-rad %)))))

(s/def ::latitude (s/and ::not-nan #(<= -90 % 90)))
(s/def ::longitude (s/and ::not-nan #(<= -180 % 180)))
(s/def ::analemma-args (s/cat :satellite ::sat/satellite
                              :latitude ::latitude
                              :longitude ::longitude
                              :datetime ::valid-datetime))
(s/fdef analemma-coords
        :args ::analemma-args
        :ret (s/coll-of ::horiz-coords))
