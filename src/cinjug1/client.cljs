(ns cinjug1.client
  (:require [enfocus.core :as ef]
            [clojure.browser.repl :as repl]
            [fetch.remotes :as rm])
  (:require-macros [enfocus.macros :as em]
                   [fetch.macros :as fm]))


(defn by-id [id] (.getElementById js/document id)) 

;;;;;;;;;;;;;;;defining models;;;;;;;;;;;;;;;;;;;;;

(def current-user (atom nil))

;;;;;;;;;;;;bringing in external content;;;;;;;;;;;;

(em/deftemplate index-html "index.html" [])

(em/deftemplate list-html "list.html" [])

(em/defsnippet reg-html "reg.html" ["#stage"] [])

(em/defsnippet comp-list-item "list.html" ["#completed-list > *:first-child"] [todo]
  [".todo"] (em/content todo))

(em/defsnippet todo-list-item "list.html" ["#todo-list > *:first-child"] [todo]
  ["input"] (em/set-attr :value todo)
  [".todo"] (em/content todo))

;;;;;;;;;;;;;; error handling ;;;;;;;;;;;;;;;;;;;;;;

(defn show-error [{:keys [message logout]}]
  (when logout
    (em/at js/document ["body"] (em/content (index-html))))
  (em/at js/document 
         ["#error-messages"] (em/do->
                              (em/content message)
                              (em/set-style :display ""))))

(em/defaction hide-error []
  ["#error-messages"] (em/set-style :display "none"))


;;;;;;;;;;;;;;;;;;defining views;;;;;;;;;;;;;;;;;;;

(em/defaction view-reg-form []
  ["#stage"] (em/content (reg-html))
  ["#sub-reg"] (em/listen :click submit-reg))


(defn view-list []
  (fm/letrem [result (find-todos (:email @current-user))]
    (let [todos (:message result)]
      (if (:success result)
        (let [completed (filter :complete todos)
              todo (filter #(not (:complete %)) todos)]
          (em/at js/document
                 ["body"] (em/content (list-html))
                 ["#username"] (em/content (:name @current-user))
                 ["#completed-list"] (em/content
                                      (map #(comp-list-item (:todo %)) completed))
                 ["#todo-list"] (em/content
                                 (map #(todo-list-item (:todo %)) todo))
                 ["#todo-list input"] (em/listen :change mark-done)
                 ["#add-todo"] (em/listen :click add-todo)))
        (show-error result)))))


;;;;;;;;;;;;;;;; defining actions ;;;;;;;;;;;;;;;;;;;;;;

(defn login [] 
  (let [email (.-value (by-id "email"))
        passwd (.-value (by-id "password"))]
    (fm/remote (login email passwd)
               [result]
               (if result
                 (do
                   (reset! current-user result)
                   (view-list))
                 (show-error {:message "You email and/or password were invalid."})))))

    
(defn add-todo []
  (hide-error)
  (let [todo (.-value (by-id "new_todo"))]
    (fm/remote (add-todo (:email @current-user) todo)
               [result]
               (if (:success result)
                 (view-list)
                 (show-error result)))))


(defn mark-done [event]
  (let [email (:email @current-user)
        todo (.-value (.-currentTarget event))]
    (fm/remote (complete-todo email todo)
             [result] (view-list))))


(defn submit-reg []
  (hide-error)
  (let [name (.-value (by-id "name"))
        email (.-value (by-id "email"))
        passwd1 (.-value (by-id "password"))
        passwd2 (.-value (by-id "password2"))]
    (if (= passwd1 passwd2)
      (fm/remote (reg-user name email passwd1)
                 [result]
                 (if (:success result)
                   (login)
                   (show-error result)))
      (show-error {:message "passwords much match"}))))


;;;;;;;;;;;;;;;; initialization ;;;;;;;;;;;;;;;;;;;;

(em/defaction init []
  ["#login-btn"] (em/listen :click login)
  ["#reg-btn"] (em/listen :click view-reg-form))


(set! (.-onload js/window)
      #(do
         (repl/connect "http://localhost:9000/repl")
         (init)))