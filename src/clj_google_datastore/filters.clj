(ns clj-google-datastore.filters
  (:import (java.util Map)))

(def ^:private primitive-value-types {String  :stringValue
                                      Boolean :booleanValue
                                      Long    :integerValue
                                      Double  :doubleValue})

(def ^:private operators {=  :EQUAL
                          <  :LESS_THAN
                          <= :LESS_THAN_OR_EQUAL
                          >  :GREATER_THAN
                          >= :GREATER_THAN_OR_EQUAL})

(defn- property-value
  [value]
  (let [value-type (primitive-value-types (type value))]
    {value-type value}))

(defn- property-filter
  [property-name operator value]
  {:propertyFilter {:property {:name (name property-name)}
                    :op       (operators operator)
                    :value    (property-value value)}})

(defn- composite-filter
  [filters]
  {:compositeFilter {:op      :AND
                     :filters (map #(let [property-value (val %)]
                                      (if (map? property-value)
                                        (composite-filter property-value)
                                        (let [property-name (key %)]
                                          (property-filter property-name
                                                           (first property-value)
                                                           (second property-value)))))
                                   filters)}})


(defprotocol FilterProtocol
  (get-filters [data]))


(extend-type Map
  FilterProtocol
  (get-filters [data] (let [count-data (count data)]
                        (cond
                          (= 1 count-data) (let [filter-data (first data)
                                                 property-name (first filter-data)
                                                 property-value (second filter-data)]
                                             (property-filter property-name
                                                              (first property-value)
                                                              (second property-value)))
                          (< 1 count-data) (composite-filter data)))))