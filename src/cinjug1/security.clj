(ns cinjug1.security
  (:require [clj-crypto.core :as crypto]
            [noir.cookies :as cookie]
            [noir.fetch.remotes :as rem]
            [noir.request :as req]
            [ring.util.codec :as codec]))

(def secret "secret")
(def tok-name :secure-tok)
(def session-timeout (* 1000 60 15)) 

(defn fail [message] 
  {:success false :logout true :message message})
  
(defn check-secure []
  (if-let [tok (cookie/get tok-name)]
    (let [cod (codec/base64-decode tok)
          dec (crypto/password-decrypt secret cod)
          time (System/currentTimeMillis)]
      (if (>(read-string dec) time)
        :valid
        :invalid))
    :missing))
	  
(defn set-secure-tok []
  (let [time (+ session-timeout (System/currentTimeMillis))
        tval (crypto/password-encrypt secret (.toString time))]
    (when (bound? #'req/*request*)
      (cookie/put! tok-name (codec/base64-encode tval))))) 			

(defmacro defsecure-remote [sym arg-vec & body]
  `(rem/defremote ~sym ~arg-vec
     (let [sec# (if (bound? #'req/*request*)
                  (check-secure) :valid)]
       (cond
        (= :valid sec#) (do (set-secure-tok) ~@body)
        (= :invalid sec#) (fail "Session Timeout")
       :else (fail "Forbidden")))))