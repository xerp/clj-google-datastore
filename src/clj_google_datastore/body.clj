(ns clj-google-datastore.body
  (:require [clj-google-datastore.filters :refer [get-filters]]
            [clj-google-datastore.projections :refer [get-projections]]
            [clj-google-datastore.orders :refer [get-orders]]
            [clj-google-datastore.factory :refer [make-partition-id
                                                  make-commit-mutation
                                                  make-commit-mode]]))

(defmulti request-body (fn [request-type _] request-type))

(defmethod request-body :runQuery [_ data]
  (let [[kind projections filters orders limit namespace] data
        partition-id (make-partition-id namespace)]
    {:partitionId partition-id
     :query       {:kind       [kind]
                   :filter     (get-filters filters)
                   :projection (get-projections projections)
                   :limit      limit
                   :order      (get-orders orders)}}))

(defmethod request-body :allocateIds [_ data]
  (let [[kind namespace] data]
    {:keys [{:partitionId (make-partition-id namespace)
             :path        [{:kind (:name kind)}]}]}))

(defmethod request-body :beginTransaction [_ data]
  (let [[transaction-option previousTransaction] data]
    {:transactionOptions {transaction-option (case transaction-option
                                               :readWrite {:previousTransaction previousTransaction}
                                               :readOnly {})}}))

(defmethod request-body :commit [_ data]
  (let [[commit-type mode entity-data transaction] data]
    {:mode        (make-commit-mode mode)
     :mutations   [(make-commit-mutation commit-type entity-data)]
     :transaction (transaction :transaction)}))