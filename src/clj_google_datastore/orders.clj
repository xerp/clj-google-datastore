(ns clj-google-datastore.orders
  (:import (java.util List Map)
           (clojure.lang Keyword)))


(def ^:private directions {:asc  :ASCENDING
                           :desc :DESCENDING})

(defn- order
  [property-name direction]
  {:property  {:name (name property-name)}
   :direction (direction directions)})


(defprotocol OrderProtocol
  (get-orders [data]))


(extend-protocol OrderProtocol
  Keyword
  (get-orders [data] (order data :asc))
  List
  (get-orders [data] (map get-orders data))
  Map
  (get-orders [data] (map #(order (key %) (val %)) data)))