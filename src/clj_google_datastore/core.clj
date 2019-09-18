(ns clj-google-datastore.core
  (:require [clj-http.client :as http]
            [cemerick.url :refer [url url-encode]]
            [clj-google.core :refer [json]]
            [clj-google-datastore.body :refer [request-body]]
            [clj-google-datastore.factory :refer [make-request-data]]))

(def ^:private datastore-base-url "https://datastore.googleapis.com")
(def ^:private datastore-api-version "v1")

(defn- datastore-url
  [project-id api-method]
  (str datastore-base-url "/" datastore-api-version "/projects/" project-id ":" (name api-method)))


(defn- json-data
  [http-fn request-url data]
  (if-let [response (http-fn request-url data)]
    (let [json-response (json (:body response))]
      json-response)))

(defn generate-id
  [project-id kind & {:keys [namespace]
                      :or   {namespace ""}}]
  (if-let [request-url (datastore-url project-id :allocateIds)]
    (let [data (make-request-data (request-body :allocateIds [kind namespace]))]
      (if-let [json-response (json-data http/post request-url data)]
        (let [result (:keys json-response)]
          result)))))

(defn begin-transaction
  [project-id transaction-option & {:keys [previous-transaction]
                                    :or   {previous-transaction ""}}]
  (if-let [request-url (datastore-url project-id :beginTransaction)]
    (let [data (make-request-data (request-body :beginTransaction [transaction-option previous-transaction]))]
      (if-let [json-response (json-data http/post request-url data)]
        json-response))))

(defn read-write-transaction
  ([project-id]
   (read-write-transaction project-id ""))
  ([project-id previous-transaction]
   (begin-transaction project-id :readWrite :previous-transaction previous-transaction)))

(defn read-only-transaction
  [project-id]
  (begin-transaction project-id :readOnly))

(defn query
  [project-id kind & {:keys [projections filters orders limit namespace]
                      :or   {projections [] filters {} limit nil orders [] namespace ""}}]
  (if-let [request-url (datastore-url project-id :runQuery)]
    (let [data (make-request-data (request-body :runQuery [kind projections filters
                                                           orders limit namespace]))]
      (if-let [json-response (json-data http/post request-url data)]
        (let [results (-> json-response :batch :entityResults)
              entities (map :entity results)]
          entities)))))

(defn commit
  [project-id commit-type mode entity-data & {:keys [transaction]
                                              :or   {transaction {:transaction ""}}}]
  (if-let [request-url (datastore-url project-id :commit)]
    (let [data (make-request-data (request-body :commit [commit-type mode entity-data transaction]))]
      (if-let [json-response (json-data http/post request-url data)]
        json-response))))


(defn insert
  [project-id mode entity-data & {:keys [transaction]
                                  :or   {transaction {:transaction ""}}}]

  (commit project-id :insert mode entity-data :transaction transaction))

(defn update
  [project-id mode entity-data & {:keys [transaction]
                                  :or   {transaction {:transaction ""}}}]

  (commit project-id :update mode entity-data :transaction transaction))

(defn delete
  [project-id mode entity-data & {:keys [transaction]
                                  :or   {transaction {:transaction ""}}}]

  (commit project-id :delete mode entity-data :transaction transaction))