# snowth
A library for generating analemmas, given orbital parameters

## A library for... what now?

If you've learned about earth's seasons and axial tilt, you are probably familiar with the idea that the
sun's path through the sky moves from day to day.  If you were to take a picture of the sun every day at the
same time, from the same place, and from the same angle, the result of composing those images would be a pattern
that is specific to your latitude and the time of day you chose. That pattern is called an analemma, and this
is a library for calculating and displaying them.

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

By default, `snowth` outputs svg-flavored hiccup, but you can pass your own render function as the 5th or 6th
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
projection, you can pass a function as the 5th or 6th (if you're also passing a render function) argument to `analemma`.
Projection functions take two arguments:

- `center`: an `[altitude azimuth]` pair indicating the center of the analemma, which will be `[0 0]` in the projection
- `point`: an `[altitude aziumth]` pair to project to `[x y]` coordinates

The return value should be an `[x y]` pair.
