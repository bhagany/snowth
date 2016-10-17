(ns snowth.devcards.core
  (:require
   [clojure.spec.test :as test]
   [devcards.core :as dc]
   [snowth.devcards.colors]
   [snowth.devcards.generated]
   [snowth.devcards.planets]
   [snowth.devcards.projections]
   [snowth.devcards.selected-analemmas]
   [snowth.devcards.world-clock]))

(dc/start-devcard-ui!)
