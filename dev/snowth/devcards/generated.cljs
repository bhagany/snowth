(ns snowth.devcards.generated
  (:require
   [clojure.spec :as s]
   [devcards.core :as dc :refer-macros [defcard]]
   [sablono.core :as sab :include-macros true]
   [snowth.core :as core :refer [analemma]]))

(doseq [[args svg-data] (s/exercise-fn 'analemma)]
  (let [conformed (s/conform ::core/analemma-args args)]
    (defcard analemma
      (sab/html [:div
                 [:p (str (:latitude conformed) ", "
                          (:longitude conformed) " at "
                          (:datetime conformed))]
                 svg-data]))))
