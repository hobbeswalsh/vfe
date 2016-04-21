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

(defn vault-auth [creds]
  (let [username (:username creds)
        bar (:bar creds)
        password (:password creds)]
    (prn creds)
    (try
      (let [c (login-userpass username password)]
        {:identity @(:token c)})
      (catch Exception e nil))))

(defn index [request]
  (html
    [:h1 "Hebbo world"])) 


(defn login [request]
  (html
    [:h1 "Log in"
    [:div
     [:form {:method "POST" :action "/login"}
      [:p [:input {:type "text" :name "username" :placeholder "username"}]]
      [:p [:input {:type "text" :name "bar" :placeholder "BAR"}]]
      [:p [:input {:type "password" :name "password" :placeholder "assword"}]]
      [:p [:input {:type "submit" :name "commit" :value "Login"}]]]]]))

(defn wrap-prn [handler]
  (fn [request]
    (prn request)
    (handler request)))

(defn foo [path]
  (prn path)
  (html
    [:h1 "Cool yo"]
    )
  )

(defroutes unsecured-app
  (GET "/"  request  (index request))
  (GET "/login"  request  (login request))
  (GET "/secret/*" [path] (friend/authenticated (foo path)))
  (route/not-found "<h1>Page not found</h1>"))

(def app
  (-> unsecured-app
    (friend/authenticate {:credential-fn vault-auth
                          :workflows [(workflows/interactive-form)]})
    #_(wrap-prn)
    (wrap-params)
    (wrap-nested-params)
    (wrap-keyword-params)
    (wrap-session)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
