(ns clj-google-datastore.factory
  (:require [clj-google-datastore.properties :refer [property-value reverse-property-value]]
            [clojure.data.json :refer [json-str]]
            [clj-google.auth :refer [*access-token*]]
            [clojure.string :as string]))


(defn make-kind
  [kind]
  {:name (name kind)})

(defn make-partition-id
  [namespace]
  {:namespaceId (name namespace)})

(defn make-properties
  [properties]
  (into {} (map #(hash-map (name (key %)) (property-value (val %))) properties)))

(defn make-reverse-properties
  [properties]
  (into {} (map #(hash-map (key %) (reverse-property-value (val %))) properties)))


(defn make-commit-mutation
  [commit-type entity-data]
  (let [{:keys [key properties]} entity-data]

    {commit-type (case commit-type :delete key

                                   {:key        key
                                    :properties (make-properties properties)})}))

(defn make-commit-mode
  [mode]
  (string/upper-case (string/replace (name mode) #"-" "_")))

(defn make-request-data
  [request-body-data]
  {:oauth-token  *access-token*
   :body         (json-str request-body-data)
   :content-type :json
   :accept       :json})
