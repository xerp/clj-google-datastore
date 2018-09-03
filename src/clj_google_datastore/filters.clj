(ns clj-google-datastore.filters)


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

(defn property
  [property-name operator value]
  {:propertyFilter {:property {:name (name property-name)}
                    :op       (operators operator)
                    :value    (property-value value)}})