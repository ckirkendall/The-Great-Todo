(ns cinjug1.core
  (:use [cinjug1.security :only [defsecure-remote set-secure-tok]]
        [noir.core :only [defpage]]
        [noir.response :only [redirect]]
        [monger.core :only [connect! set-db! get-db]]
        [monger.collection :only [insert find-maps find-one find-one-as-map update]]
        [noir.fetch.remotes :only [defremote]])
  (:require [noir.server :as server]))

;;;;;;;;;;;;;;; initialize monodb connection ;;;;;;;;;;;;;;;;;;;;;
(connect!)
(set-db! (get-db "cinjug"))


;;;;;;;;;;;;;;; simple message protocol ;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn return-m [success message]
  {:success success :message message})


;;;;;;;;;;;;;;;; remote operations ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defremote reg-user [name email passwd]
  (let [ins #(insert "users" {:name name :email email :pass passwd})]
    (cond
     (empty? name) (return-m false "Name is invalid.")
     (not (re-matches #".+@.+\..+" email)) (return-m false "Email is invalid.")
     (empty? passwd) (return-m false "password is invalid")
     (.getError (ins)) (return-m false "Error occured writing to the database")
     :else (return-m true ""))))


(defremote login [email passwd]
  (let [result (find-one-as-map "users" {:email email :pass passwd})]
    (when result
      (set-secure-tok)
      (dissoc result :_id))))
    

(defsecure-remote add-todo [email todo]
  (let [ins #(insert "todos" {:email email :todo todo :complete false})]
    (cond
     (empty? email) (return-m false "Email is invalid.")
     (empty? todo) (return-m false "Todo is invalid")
     (.getError (ins)) (return-m false "Error occured writing to the database")
     :else (return-m true ""))))


(defsecure-remote complete-todo [email todo]
  (let [res (update "todos" {:email email :todo todo} { "$set" { :complete true } })]
    (if (.getError res)
      (return-m false "Error occured while updating")
      (return-m true (.getN res)))))      


(defsecure-remote find-todos [email]
  (let [res (find-maps "todos" {:email email})]
   (return-m true (map #(dissoc % :_id) res))))


;;;;;;;;;;;;;;;;;;; initialize server ;;;;;;;;;;;;;;;;;;;;;;;;
(defpage "/" [] (redirect "/index.html"))


(defn -main [& args]
  (server/start 10012 {:mode :dev :ns 'cinjug1 }))