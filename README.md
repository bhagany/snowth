# snowth
A library for generating analemmas from orbital parameters

## A library for... what now?

Snowth makes these:

<img src="https://cdn.rawgit.com/bhagany/snowth/master/analemma.svg" height="350" alt="sample analemma" />

If you've learned about earth's seasons and axial tilt, you are probably familiar with the idea that the
sun's path through the sky moves from day to day. Because of this yearly motion, if you were to take a picture
of the sun every day at the same time, from the same place, and at the same angle, the result of composing those
images would make a pattern that looks a lot like the image above. Snowth calculates and displays these patterns,
called "analemmas."

(There is a bit more to it than this, but that's the basic idea)

On Earth, the analemma that the sun makes over the course of the year will always be very close to the same shape,
but its orientation will vary with your latitude and the time of day; the image above is what you'd see in the northern
hemisphere at noon. But other planets have analemmas too! [Jupiter's is an oval](https://bhagany.github.io/snowth/#!/snowth.devcards.planets/jupiter); [Mars has a teardrop](https://bhagany.github.io/snowth/#!/snowth.devcards.planets/mars). If we know some
basic orbital parameters of anything that orbits another thing, we can generate an analemma that one makes when viewed
from the other. Snowth can generate the analemmas for any of these other situations, too. [There are many more examples for you to explore](https://bhagany.github.io/snowth/).



## Installation
To install from Clojars, add this to your dependencies:
```
[snowth "0.1.0"]
```

## Basic usage

```clojure
(ns your.code
  (:require
    [snowth.core :as snowth]
    [snowth.satellites :as sat]))

;; Latitude and longitude of Hawthorne Fish House, Portland, OR, USA
(def svg-data (analemma sat/earth 45.5121466 -122.6196392 (js/Date.)))
```

This will return the analemma for the passed satellite, latitude, longitude, and datetime as Hiccup data

## Advanced usage

### Customizing satellites

Most people will probably want to generate analemmas for earth, but doing so for other satellites is supported,
as long as certain orbital parameters are known. The eight planets of the solar system (sorry, Pluto!) are already
defined in the `snowth.satellites` namespace, with plans for more in the future. You can also define your own
satellite by implementing the Satellite protocol; see the implementations in `snowth.satellites` for details.

### Customizing rendering

By default, snowth outputs svg-flavored hiccup, but you can pass your own render function as the 5th or 6th
parameter to `analemma`.  Render functions take 5 parameters:

- `projection`: a seq of `[x y]` points corresponding to the points of the analemma
- `horizon`: a seq of `[x y]` points corresponding to the horizon line
- `center-horizon`: an `[x y]` point specifying where the horizon crosses the vertical center line
- `zenith`: an `[x y]` point specifying where the zenith is in the given projection
- `nadir`: an `[x y]` point specifying where the nadir is in the given projection

PLEASE NOTE: The default projection functions produce `[x y]` coordinates on a plane in which positive y is "up"
and negative y is "down".  On computer screens, the y axis is flipped - your render functions need to account for this.

### Customizing projection

The algorithm for locating an object in a local observer's sky results in 3-d spherical coordinates - in this case
I opted for altitude and azimuth for easy calculation of the horizon, zenith, and nadir.  These 3-d coordinates need
to be projected onto a 2-d plane in order to display them on a screen. I've included both orthographic and sterographic
projections in `snowth.projections`, which are very similar to each other on the scale of earth's analemma. Unless you
have special requirements, these should be more than you need.  However, if you find yourself needing to customize the
projection, you can pass an object that implements the `snowth.projections/Project` protocol as the 5th or 6th (if
you're also passing a render function) argument to `analemma`. The protocol has only a single function, `project*`,
which takes three arguments:

- `self`: the object that implements `Project`
- `center`: a map with the following keys
  - `:snowth.astro/alt-az`: an `[altitude azimuth]` pair indicating the center of the analemma, which will be `[0 0]` in the projection
  - `:snowth.projections/sin-center-alt`: the `sin` of the center point's altitude, calculated beforehand because it is the same across multiple individual projections
  - `:snowth.projections/cos-center-alt`: the `cos` of the center point's altitude, calculated for the same reasons as `:snowth.projections/sin-center-alt`
- `point`: an `[altitude azimuth]` pair to project to `[x y]` coordinates

The return value should be an `[x y]` pair.
