(ns clj-google-datastore.projections
  (:import (java.util List)))

(defn- projection
  [property-name]
  {:property {:name (name property-name)}})

(defprotocol ProjectionProtocol
  (get-projections [data]))

(extend-type List
  ProjectionProtocol
  (get-projections [data] (map projection data)))