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

(defn page [body]
  (html
   [:head
    [:link {:rel "stylesheet" :href "http://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css"}]]
   [:body
    [:div
     [:nav {:class "navbar navbar-default"}
      [:div {:class "container-fluid"}
       [:div {:class "collapse navbar-collapse"}
        [:ul {:class "nav nav-tabs" :role "tablist"}
         [:li {:role "presentation" :class "active"} [:a {:aria-controls "one" :href "#one" :data-toggle "tab"} "One"]]
         [:li {:role "presentation"} [:a {:aria-controls "two" :href "#two" :data-toggle "tab"} "Two"]]]
        [:div {:class "tab-content"}
         [:div {:role "tabpanel" :class "tab-pane" :id "one"} [:h1 "ONE"]]
         [:div {:role "tabpanel" :class "tab-pane" :id "two"} [:h1 "TWO"]]]]]]]
    #_body]

   [:script {:src "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js"}]))

(defn index [request]
  (page
   [:nav {:class "navbar"}
    [:ol {:class "breadcrumb"} [:li "One"] [:li "Two"] [:li "Three"]]]))

(defn login [request]
  (page
   [:div {:class "container"}
    [:form {:class "form-inline" :method "POST" :action "/login"}
     [:div {:class "form-group"}

      [:p [:input {:type "text" :name "username" :placeholder "username"}]]]
     [:div {:class "form-group"}
      [:p [:input {:type "password" :name "password" :placeholder "password"}]]]
     [:p [:input {:type "submit" :name "commit" :value "Login"}]]]]))

(defn wrap-prn [handler]
  (fn [request]
    (prn request)
    (handler request)))

(defn foo [path]
  (prn path)
  (html
   [:h1 "Cool yo"]))

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
