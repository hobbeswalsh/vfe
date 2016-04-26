(ns vfe.core
  (:gen-class)
  (:require
   [cemerick.friend :as friend]
   (cemerick.friend [workflows :as workflows]
                    [credentials :as creds])
   [clojure.walk :refer [keywordize-keys]]
   [compojure.core :refer :all]
   [compojure.route :as route]
   (ring.middleware
    [params :refer [wrap-params]]
    [nested-params :refer [wrap-nested-params]]
    [keyword-params :refer [wrap-keyword-params]]
    [session :refer [wrap-session]])
   [hiccup.core :refer [html]]
   [hiccup.page :refer [html5 include-css include-js]]
   [vault.client :as vault]))

(def vault-url "http://localhost:8200")

(defn login-userpass [username password]
  (let [c (vault/http-client vault-url)]
    (vault/authenticate! c :userpass  {:username username, :password password})))

(defn login-appid [appid userid]
  (let [c (vault/http-client vault-url)]
    (vault/authenticate! c :appid {:appid appid, :userid userid})))

(defn vault-auth [creds]
  (try
    (cond
      (and (:username creds) (:password creds))
        (let [c (login-userpass (:username creds) (:password creds))]
          {:identity @(:token c)})
      (and (:appid creds) (:userid creds))
        (let [c (login-appid (:appid creds) (:userid creds))]
          {:identity @(:token c)})
      :else nil)
    (catch Exception e nil)))

(def login-form
  [:div
   [:nav {:class "navbar navbar-default"}
    [:div {:class "container-fluid"}
     [:div {:class "collapse navbar-collapse"}
      [:ul {:class "nav nav-tabs" :role "tablist"}
       [:li {:role "presentation"} [:a {:aria-controls "userpass" :href "#userpass" :data-toggle "tab"} "Userpass"]]
       [:li {:role "presentation"} [:a {:aria-controls "appid" :href "#appid" :data-toggle "tab"} "AppID"]]]
      [:div {:class "tab-content"}
       [:div {:role "tabpanel" :class "tab-pane fade in active" :id "userpass"}
        [:div {:class "container"}
         [:form {:class "form-inline" :method "POST" :action "/login"}
          [:div {:class "form-group"}

           [:p [:input {:type "text" :name "username" :placeholder "username"}]]]
          [:div {:class "form-group"}
           [:p [:input {:type "password" :name "password" :placeholder "password"}]]]
          [:p [:input {:type "submit" :name "commit" :value "Login"}]]]]]
       [:div {:role "tabpanel" :class "tab-pane fade in" :id "appid"}
        [:div {:class "container"}
         [:form {:class "form-inline" :method "POST" :action "/login"}
          [:div {:class "form-group"}

           [:p [:input {:type "text" :name "appid" :placeholder "AppID"}]]]
          [:div {:class "form-group"}
           [:p [:input {:type "password" :name "userid" :placeholder "UserID"}]]]
          [:p [:input {:type "submit" :name "commit" :value "Login"}]]]]]]]]]])

(defn page [body]
  (html5
   [:head
    (include-css "http://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css")]
   [:body 
    body
    (include-js "https://ajax.googleapis.com/ajax/libs/jquery/1.12.0/jquery.min.js")
    (include-js "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js")]))

(defn index []
  (page
   [:nav {:class "navbar"}
    [:ol {:class "breadcrumb"} [:li "One"] [:li "Two"] [:li "Three"]]]))

(defn login []
  (page login-form))

(defn wrap-prn [handler]
  (fn [request]
    (prn request)
    (handler request)))

(defn foo [path]
  (html
   [:h1 "Cool yo"]))

(defroutes unsecured-app
  (GET "/"  []  (index))
  (GET "/login"  [] (login))
  (GET "/secret/*" [path] (friend/authenticated (foo path)))
  (route/not-found "<h1>Page not found</h1>"))

(defn do-login [req]
  (vault-auth (keywordize-keys (:params req))))

(defn vault-credential-fn [creds]
  (prn "in the thing")
  (prn creds)
  )

(defn password-workflow [req]
  (when (and (= (:request-method req) :post)
             (=  (:uri req) "/login"))
        (do-login req))
  (prn req)
  #_(prn (keywordize-keys (:params req)))
  )


(defn authme [req]
  (prn req)
  (workflows/make-auth {:identity "robin" :roles #{::user}}))

(def app
  (-> unsecured-app
      (friend/authenticate {:credential-fn vault-credential-fn
                            :workflows [password-workflow]})
      #_(wrap-prn)
      (wrap-params)
      (wrap-nested-params)
      (wrap-keyword-params)
      (wrap-session)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
