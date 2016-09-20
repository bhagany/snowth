(ns snowth.common
  (:require
   [clojure.spec :as s]))

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
(s/def ::not-nan (s/and number? #(not (js/isNaN %))))
(s/def ::angle (s/and ::not-nan #(<= (- pi) % pi)))
(s/def ::positive-angle (s/and ::not-nan #(<= 0 % (* 2 pi))))
(s/def ::half-angle (s/and ::not-nan #(<= (/ pi -2) % (/ pi 2))))
(s/def ::positive-half-angle (s/and ::not-nan #(<= 0 % pi)))
(s/def ::eccentricity (s/and ::not-nan #(< 0 % 1)))
