(ns vfe.core
  (:gen-class)
  (:require
    [cemerick.friend :as friend]
    (cemerick.friend [workflows :as workflows]
                     [credentials :as creds])
    [compojure.core :refer :all]
    [compojure.route :as route]
    (ring.middleware
      [params :refer [wrap-params]]
      [nested-params :refer [wrap-nested-params]]
      [keyword-params :refer [wrap-keyword-params]]
      [session :refer [wrap-session]])
    [hiccup.core :refer [html]]
    [vault.client :as vault]))

(def vault-url "http://localhost:8200")

(defn login-userpass [username password]
  (let [c (vault/http-client vault-url)]
    (vault/authenticate! c :userpass  {:username username, :password password})))

(defn auth-user-pass [creds]
  (let [username (:username creds)
        password (:password creds)]
    (try
      (let [c (login-userpass username password)]
        {:identity @(:token c)})
      (catch Exception e nil))))

(defn index [request]
  (prn request)
  (html
    [:h1 "Hebbo world"])) 


(defn login [request]
  (html
    [:h1 "Hokay. Log in plox."
    [:div
     [:form {:method "POST" :action "/login"}
      [:p [:input {:type "text" :name "username" :placeholder "username"}]]
      [:p [:input {:type "password" :name "password" :placeholder "assword"}]]
      [:p [:input {:type "submit" :name "commit" :value "Login"}]]]]]))

(defn wrap-prn [handler]
  (fn [request]
    (prn request)
    (handler request)))

(defroutes unsecured-app
  (GET "/"  [request] (index request))
  (GET "/login"  [request] (login request))
  (route/not-found "<h1>Page not found</h1>"))

(def app
  (-> unsecured-app
    (friend/authenticate {:credential-fn auth-user-pass
                          :workflows [(workflows/interactive-form)]})
    (wrap-prn)
    (wrap-params)
    (wrap-nested-params)
    (wrap-keyword-params)
    (wrap-session)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
