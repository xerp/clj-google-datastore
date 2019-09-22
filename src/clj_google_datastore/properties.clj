(ns clj-google-datastore.properties
  (:import (org.joda.time DateTime)
           (java.util Map List)))


(defprotocol PropertyValueProtocol
  (property-value [this]))

(defn reverse-property-value
  [value-map]
  (let [[value-type value] (first (map #(vector (key %) (val %)) value-map))]
    (case value-type
      :nullValue nil
      :stringValue value
      :booleanValue (Boolean/parseBoolean value)
      :integerValue (Long/parseLong value)
      :doubleValue (Double/parseDouble value)
      :timestampValue (DateTime/parse value)
      :arrayValue (vec (map reverse-property-value (value :values)))
      :entityValue (into {}
                         (map #(hash-map (key %) (reverse-property-value (val %)))
                              (value :properties))))))


; Primitive Properties
(extend-protocol PropertyValueProtocol
  String
  (property-value [this] {:stringValue this})
  Boolean
  (property-value [this] {:booleanValue this})
  Integer
  (property-value [this] {:integerValue (str this)})
  Long
  (property-value [this] {:integerValue (str this)})
  Double
  (property-value [this] {:doubleValue this})
  nil
  (property-value [this] {:nullValue this})
  DateTime                                                  ; Joda DateTime
  (property-value [this] {:timestampValue (str this)}))


; Complex Properties
(extend-protocol PropertyValueProtocol
  List
  (property-value [this] {:arrayValue
                          {:values (map property-value this)}})
  Map
  (property-value [this] (let [properties (into {} (drop-while #(= (key %) :key) this))]

                           {:entityValue {:properties (into {} (map #(hash-map
                                                                       (name (key %)) (property-value (val %)))
                                                                    properties))}})))


