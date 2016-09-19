(ns snowth.core
  (:require
   [snowth.astro :as astro]
   [snowth.projections :as proj]
   [snowth.render :as render]))

(defn analemma
  "Returns data sufficient for rendering a satellite's analemma

  The format of the render data can be controlled by passing a `renderer` that
  implements snowth.render/Render. Uses an svg-hiccup renderer by default"
  ([satellite latitude longitude datetime]
   (analemma satellite latitude longitude datetime
             render/dots proj/orthographic))

  ([satellite latitude longitude datetime renderer-or-proj-fn]
   (if (satisfies? render/Render renderer-or-proj-fn)
     (analemma satellite latitude longitude datetime
               renderer-or-proj-fn proj/orthographic)
     (analemma satellite latitude longitude datetime
               render/dots renderer-or-proj-fn)))

  ([satellite latitude longitude datetime renderer projection-fn]
   (let [coords (astro/analemma-coords satellite latitude longitude datetime)
         center (proj/center-info coords)
         projection (->> coords
                         (map astro/alt-az)
                         (map #(projection-fn center %)))]
     (render/-render renderer projection))))
