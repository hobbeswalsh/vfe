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

(def users {"root" {:username "root"
                    :password (creds/hash-bcrypt "admin_password")
                    :roles #{::admin}}
            "robin" {:username "robin"
                     :password (creds/hash-bcrypt "asd")
                     :roles #{::user ::admin}}
            "jane" {:username "jane"
                    :password (creds/hash-bcrypt "user_password")
                    :roles #{::user}}})


(defn index [request]
  (prn request)
  (html
    [:h1 "Hebbo world"]
    ))



(defn login [request]
  (html
    [:h1 "Hokay. Log in plox."
    [:div
     [:form {:method "POST" :action "/login"}
      [:p
       [:input {:type "text" :name "username" :placeholder "username"}]]
      [:p
       [:input {:type "password" :name "password" :placeholder "assword"}]]
      [:p 
       [:input {:type "submit" :name "commit" :value "Login"}]]

       
      ]
     ]]
    )
  )

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
    (friend/authenticate {:credential-fn (partial creds/bcrypt-credential-fn users)
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
