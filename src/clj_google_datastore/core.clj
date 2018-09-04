(ns clj-google-datastore.core
  (:require [clj-http.client :as http]
            [cemerick.url :refer [url url-encode]]
            [clojure.data.json :as json]
            [clj-google.auth :refer [*access-token*]]
            [clj-google-datastore.filters :refer [get-filters]]
            [clj-google-datastore.projections :refer [get-projections]]
            [clj-google-datastore.orders :refer [get-orders]]))

(def ^:private datastore-base-url "https://datastore.googleapis.com")
(def ^:private datastore-api-version "v1")


(defn- datastore-url
  [project-id api-method]
  (str datastore-base-url "/" datastore-api-version "/projects/" project-id ":" (name api-method)))

(defn- request-query-body
  [kind projections filters orders limit namespace]
  (let [partition-id {:namespaceId (name namespace)}]
    {:partitionId partition-id
     :query       {:kind       [kind]
                   :filter     (get-filters filters)
                   :projection (get-projections projections)
                   :limit      limit
                   :order      (get-orders orders)}}))

(defmacro defkind
  [kind-name kind]
  `(def ~kind-name {:name (name ~kind)}))

(defn query
  [project-id kind & {:keys [projections filters orders limit namespace]
                      :or   {projections [] filters {} limit nil orders [] namespace ""}}]
  (if-let [query-url (datastore-url project-id :runQuery)]
    (let [data {:oauth-token  *access-token*
                :body         (json/json-str (request-query-body kind projections filters
                                                                 orders limit namespace))
                :content-type :json
                :accept       :json}]
      (if-let [response (http/post query-url data)]
        (let [json-response (json/read-str (:body response) :key-fn keyword)
              results (-> json-response :batch :entityResults)
              entities (map :entity results)]
          entities)))))