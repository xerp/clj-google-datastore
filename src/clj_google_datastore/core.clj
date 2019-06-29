(ns clj-google-datastore.core
  (:require [clj-http.client :as http]
            [cemerick.url :refer [url url-encode]]
            [clojure.data.json :as json-data]
            [clj-google.auth :refer [*access-token*]]
            [clj-google.core :refer [json]]
            [clj-google-datastore.body :refer [request-body]]))

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

(defn- make-data
  [body-type body-data]
  {:oauth-token  *access-token*
   :body         (json-data/json-str (request-body body-type body-data))
   :content-type :json
   :accept       :json})


(defmacro defkind
  [kind-name kind]
  `(do
     (declare ~kind-name)
     (def ~kind-name {:name (name ~kind)})))

(defn query
  [project-id kind & {:keys [projections filters orders limit namespace]
                      :or   {projections [] filters {} limit nil orders [] namespace ""}}]
  (if-let [request-url (datastore-url project-id :runQuery)]
    (let [data (make-data :runQuery [kind projections filters
                                     orders limit namespace])]
      (if-let [json-response (json-data http/post request-url data)]
        (let [results (-> json-response :batch :entityResults)
              entities (map :entity results)]
          entities)))))

(defn generate-id
  [project-id kind & {:keys [namespace]
                      :or   {namespace ""}}]
  (if-let [request-url (datastore-url project-id :allocateIds)]
    (let [data (make-data :allocateIds [kind namespace])]
      (if-let [json-response (json-data http/post request-url data)]
        (let [result (:keys json-response)]
          result)))))

(defn begin-transaction
  [project-id transaction-option & {:keys [previous-transaction]
                                    :or   {previous-transaction ""}}]
  (if-let [request-url (datastore-url project-id :beginTransaction)]
    (let [data (make-data :beginTransaction [transaction-option previous-transaction])]
      (if-let [json-response (json-data http/post request-url data)]
        json-response))))

(defn read-only-transaction
  [project-id]
  (begin-transaction project-id :readOnly))

(defn read-write-transaction
  ([project-id]
   (read-write-transaction project-id ""))
  ([project-id previous-transaction]
   (begin-transaction project-id :readWrite :previous-transaction previous-transaction)))

(defn commit
  [project-id commit-type mode entity-data & {:keys [transaction]
                                              :or   {transaction {:transaction ""}}}]
  (if-let [request-url (datastore-url project-id :commit)]
    (let [data (make-data :commit [commit-type mode entity-data transaction])]
      (if-let [json-response (json-data http/post request-url data)]
        json-response))))


(defn insert
  [project-id mode transaction entity-data]
  (commit project-id :insert mode entity-data :transaction transaction))

(defn update
  [project-id mode transaction entity-data]
  (commit project-id :update mode entity-data :transaction transaction))

(defn delete
  [project-id mode transaction entity-data]
  (commit project-id :delete mode entity-data :transaction transaction))