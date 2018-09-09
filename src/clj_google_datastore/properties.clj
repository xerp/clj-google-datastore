(ns clj-google-datastore.properties)

(def ^:private primitive-value-types {String  :stringValue
                                      Boolean :booleanValue
                                      Long    :integerValue
                                      Double  :doubleValue})

(defn property-value
  [value]
  (let [value-type (primitive-value-types (type value))]
    {value-type value}))
