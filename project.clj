(defproject clj-google-datastore "2.9"
  :description "Google datastore api"
  :url "https://github.com/xerp/clj-google-datastore"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [com.cemerick/url "0.1.1"]
                 [clj-http "3.10.0"]
                 [org.clojure/data.json "0.2.6"]
                 [clj-google "1.6"]]
  :repositories [["releases" {:url           "https://clojars.org/repo"
                              :username      :env/CLOJAR_USERNAME
                              :password      :env/CLOJAR_PASSWORD
                              :sign-releases false}]])