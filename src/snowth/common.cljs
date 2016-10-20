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
(s/def ::angle (s/double-in :min (- pi) :max pi :NaN? false))
(s/def ::positive-angle (s/double-in :min 0 :max (* 2 pi) :NaN? false))
(s/def ::half-angle (s/double-in :min (/ pi -2) :max (/ pi 2) :NaN? false))
(s/def ::positive-half-angle (s/double-in :min 0 :max pi :NaN? false))
(s/def ::eccentricity (s/and double? #(< 0 % 1) #(not (js/isNaN %))))
