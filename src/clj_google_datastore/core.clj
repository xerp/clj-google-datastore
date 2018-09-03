(ns clj-google-datastore.core
  (:require [clj-http.client :as http]
            [cemerick.url :refer [url url-encode]]
            [clojure.data.json :as json]
            [clj-google.auth :refer [*access-token*]]))

(def ^:private datastore-base-url "https://datastore.googleapis.com")
(def ^:private datastore-api-version "v1")


(defn- datastore-url
  [project-id api-method]
  (str datastore-base-url "/" datastore-api-version "/projects/" project-id ":" (name api-method)))

(defn- make-body
  [kind filters namespace]
  (let [partition-id {:namespaceId (if namespace (name namespace) "")}]
    {:partitionId partition-id
     :query       {:kind   kind
                   :filter filters}}))

(defmacro defkind
  [kind-name kind]
  (let [map-kind #(hash-map :name (name %))
        kind-def (if (vector? kind)
                   (map map-kind kind)
                   [(map-kind kind)])]
    `(def ~kind-name ~kind-def)))

(defn query
  ([project-id kind filters]
   (query project-id kind filters nil))
  ([project-id kind filters namespace]
   (if-let [query-url (datastore-url project-id :runQuery)]
     (let [data {:oauth-token  *access-token*
                 :body         (json/json-str (make-body kind filters namespace))
                 :content-type :json
                 :accept       :json}]
       (if-let [response (http/post query-url data)]
         (let [json-response (json/read-str (:body response) :key-fn keyword)
               results (-> json-response :batch :entityResults)
               entities (map :entity results)]
           entities))))))