(ns snowth.satellites)

(defprotocol Satellite
  (-datetime->d [self datetime]
    "Takes a datetime, returns an epoch offset in planet-days that we use for
     calculations")
  (-argument-of-periapsis [self d]
    "The angle from the satellite's ascending node to its periapsis, at d")
  (-eccentricity [self d]
    "A measure of how oblong the orbit is, at d; 0 is circular, 1 is a
     parabola")
  (-mean-anomaly [self d]
    "The distance from periapsis in radians, if the orbit were circular")
  (-ecliptic-obliquity [self d]
    "The angle between the satellite's equator and its ecliptic")
  (-rotation [self d]
    "How far the planet has rotated since the beginning of the last sidereal
     day")
  (-ds-per-orbit [self]
    "Number of rotations per revolution"))

(def pi (.-PI js/Math))

(def j2000 (js/Date. (js/Date.UTC 2000 0 1)))
(def j2000-ms (.getTime j2000))

(def mercury
  (let [mean-anomaly-step 12.566582521]
    (reify Satellite
      (-datetime->d [_ datetime]
        (/ (- (.getTime datetime) j2000-ms)
           15201360000))
      (-argument-of-periapsis [_ d]
        (+ 0.508311 (* 3.1151e-5 d)))
      (-eccentricity [_ d]
        (+ 0.20563069 (* 1.217e-7 d)))
      (-ecliptic-obliquity [_ d]
        0.00059)
      (-mean-anomaly [_ d]
        (+ 2.943606 (* mean-anomaly-step d)))
      (-rotation [_ d]
        (* 2 pi (- d (int d))))
      (-ds-per-orbit [_]
        (/ (* 2 pi) mean-anomaly-step)))))

(def venus
  (let [mean-anomaly-step 3.2646157411]
    (reify Satellite
      (-datetime->d [_ datetime]
        (/ (- (.getTime datetime) j2000-ms)
           10087200000))
      (-argument-of-periapsis [_ d]
        (+ 0.958029 (* 2.81960e-5 d)))
      (-eccentricity [_ d]
        (+ 0.00677323 (* -2.378e-7 d)))
      (-ecliptic-obliquity [_ d]
        3.096)
      (-mean-anomaly [_ d]
        (+ 0.837848 (* mean-anomaly-step d)))
      (-rotation [_ d]
        (* 2 pi (- d (int d))))
      (-ds-per-orbit [_]
        (/ (* 2 pi) mean-anomaly-step)))))

(def earth
  (let [mean-anomaly-step 0.0172019696]
    (reify Satellite
      (-datetime->d [_ datetime]
        (/ (- (.getTime datetime) j2000-ms)
           86400000))
      (-argument-of-periapsis [_ d]
        (+ 4.938242 (* 8.21937e-7 d)))
      (-eccentricity [_ d]
        (+ 0.016709 (* -1.151e-9 d)))
      (-ecliptic-obliquity [_ d]
        (+ 0.409093 (* -6.219e-9 d)))
      (-mean-anomaly [_ d]
        (+ 6.214192 (* mean-anomaly-step d)))
      (-rotation [_ d]
        (* 2 pi (- d (int d))))
      (-ds-per-orbit [_]
        (/ (* 2 pi) mean-anomaly-step)))))

(def mars
  (let [mean-anomaly-step 0.00939732]
    (reify Satellite
      (-datetime->d [_ datetime]
        (/ (- (.getTime datetime) j2000-ms)
           88775244))
      (-argument-of-periapsis [_ d]
        (+ 5.000396 (* 5.25370e-7 d)))
      (-eccentricity [_ d]
        (+ .09341233 (* 3.34817272e-9 d)))
      (-ecliptic-obliquity [_ d]
        0.4396)
      (-mean-anomaly [_ d]
        (+ 0.338369 (* mean-anomaly-step d)))
      (-rotation [_ d]
        (+ 6.277137 (* 2 pi (- d (int d)))))
      (-ds-per-orbit [_]
        (/ (* 2 pi) mean-anomaly-step)))))

(def jupiter
  (let [mean-anomaly-step .0005997361319]
    (reify Satellite
      (-datetime->d [_ datetime]
        (/ (- (.getTime datetime) j2000-ms)
           35733240))
      (-argument-of-periapsis [_ d]
        (+ 4.780068 (* 1.18745e-7 d)))
      (-eccentricity [_ d]
        (+ 0.04839266 (* -1.4584e-9 d)))
      (-ecliptic-obliquity [_ d]
        0.0546)
      (-mean-anomaly [_ d]
        (+ 0.3472 (* mean-anomaly-step d)))
      (-rotation [_ d]
        (* 2 pi (- d (int d))))
      (-ds-per-orbit [_]
        (/ (* 2 pi) mean-anomaly-step)))))

(def saturn
  (let [mean-anomaly-step 0.000259168083]
    (reify Satellite
      (-datetime->d [_ datetime]
        (/ (- (.getTime datetime) j2000-ms)
           38361600))
      (-argument-of-periapsis [_ d]
        (+ 5.923541 (* 2.30665e-7 d)))
      (-eccentricity [_ d]
        (+ 0.05415060 (* -4.4688e-9 d)))
      (-ecliptic-obliquity [_ d]
        0.4665)
      (-mean-anomaly [_ d]
        (+ 5.532118 (* mean-anomaly-step d)))
      (-rotation [_ d]
        (* 2 pi (- d (int d))))
      (-ds-per-orbit [_]
        (/ (* 2 pi) mean-anomaly-step)))))

(def uranus
  (let [mean-anomaly-step 0.00014700973]
    (reify Satellite
      (-datetime->d [_ datetime]
        (/ (- (.getTime datetime) j2000-ms)
           62064000))
      (-argument-of-periapsis [_ d]
        (+ 1.68706 (* 3.8320e-7 d)))
      (-eccentricity [_ d]
        (+ 0.04716771 (* -3.7662e-9 d)))
      (-ecliptic-obliquity [_ d]
        1.706)
      (-mean-anomaly [_ d]
        (+ 2.488674 (* mean-anomaly-step d)))
      (-rotation [_ d]
        (* 2 pi (- d (int d))))
      (-ds-per-orbit [_]
        (/ (* 2 pi) mean-anomaly-step)))))

(def neptune
  (let [mean-anomaly-step 7.023628e-5]
    (reify Satellite
      (-datetime->d [_ datetime]
        (/ (- (.getTime datetime) j2000-ms)
           57996000))
      (-argument-of-periapsis [_ d]
        (+ 4.762063 (* -7.06e-8 d)))
      (-eccentricity [_ d]
        (+ 0.00858587 (* 4.61e-10 d)))
      (-ecliptic-obliquity [_ d]
        0.4943)
      (-mean-anomaly [_ d]
        (+ 4.542169 (* mean-anomaly-step d)))
      (-rotation [_ d]
        (* 2 pi (- d (int d))))
      (-ds-per-orbit [_]
        (/ (* 2 pi) mean-anomaly-step)))))
