(ns snowth.core
  (:require
   [clojure.spec :as s]
   [snowth.astro :as astro]
   [snowth.common :as c :refer [pi]]
   [snowth.projections :as proj]
   [snowth.render :as render]
   [snowth.satellites :as sat]))

(defn analemma
  "Returns data sufficient for rendering a satellite's analemma

  The format of the render data can be controlled by passing a render function
  that takes a projection as a list of [x y] points, and returns whatever you'd
  like. Uses an svg-hiccup renderer by default.

  The projection can similarly being customized by passing a function that
  takes a map that conforms to :snowth.projections/center-info and a single
  point in [altitude azimuth], and returns the projected point."
  ([satellite latitude longitude datetime]
   (analemma satellite latitude longitude datetime
             render/dots proj/orthographic))

  ([satellite latitude longitude datetime render-fn-or-projector]
   (if (satisfies? proj/Project render-fn-or-projector)
     (analemma satellite latitude longitude datetime
               render/dots render-fn-or-projector)
     (analemma satellite latitude longitude datetime
               render-fn-or-projector proj/orthographic)))

  ([satellite latitude longitude datetime render-fn projector]
   (let [coords (astro/analemma-coords satellite latitude longitude datetime)
         {[_ center-az] ::astro/alt-az :as center} (proj/center-info coords)
         projection (->> coords
                         (map astro/alt-az)
                         (map #(proj/project projector center %)))
         horizon (map #(proj/project projector center [0 %])
                      (range (- center-az (/ pi 2))
                             (+ center-az (/ pi 2))
                             (/ pi 180)))
         center-horizon (proj/project projector center [0 center-az])
         zenith (proj/project projector center [(/ pi 2) 0])
         nadir (proj/project projector center [(/ pi -2) 0])]
     (render-fn projection horizon center-horizon zenith nadir))))

(s/def ::analemma-args (s/cat :satellite ::sat/satellite
                              :latitude ::astro/latitude
                              :longitude ::astro/longitude
                              :datetime ::c/valid-datetime
                              :render-fn (s/? (s/with-gen
                                                ::render/render-fn
                                                #(s/gen #{render/dots})))
                              :projector (s/? ::proj/projector)))
(s/fdef analemma :args ::analemma-args)
