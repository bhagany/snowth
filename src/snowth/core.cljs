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

  ([satellite latitude longitude datetime render-or-proj-fn]
   (let [conformed (s/conform (s/or :projection-fn ::proj/projection-fn
                                    :render-fn ::render/render-fn)
                              render-or-proj-fn)]
     (if (= (first conformed) :projection-fn)
       (analemma satellite latitude longitude datetime
                 render/dots render-or-proj-fn)
       (analemma satellite latitude longitude datetime
                 render-or-proj-fn proj/orthographic))))

  ([satellite latitude longitude datetime render-fn projection-fn]
   (let [coords (astro/analemma-coords satellite latitude longitude datetime)
         {[_ center-az] ::astro/alt-az :as center} (proj/center-info coords)
         projection (->> coords
                         (map astro/alt-az)
                         (map #(projection-fn center %)))
         horizon (map #(projection-fn center [0 %])
                      (range (- center-az (/ pi 2))
                             (+ center-az (/ pi 2))
                             (/ pi 180)))
         center-horizon (projection-fn center [0 center-az])
         zenith (projection-fn center [(/ pi 2) 0])
         nadir (projection-fn center [(/ pi -2) 0])]
     (render-fn projection horizon center-horizon zenith nadir))))

(s/def ::analemma-args (s/cat :satellite ::sat/satellite
                              :latitude ::astro/latitude
                              :longitude ::astro/longitude
                              :datetime ::c/valid-datetime
                              :render-fn (s/? ::render/render-fn)
                              :projection-fn (s/? ::proj/projection-fn)))
(s/fdef analemma :args ::analemma-args)
