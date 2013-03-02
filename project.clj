(defproject cinjug1 "1.0.0-SNAPSHOT"
  :description "best todo app ever"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [noir "1.3.0"]
                 [fetch "0.1.0-alpha2" :exclusions [org.clojure/clojure]]
                 [enfocus "1.0.0"]
                 [com.novemberain/monger "1.4.2"]
                 [clj-crypto "1.0.0"]]
  :plugins      [[lein-cljsbuild "0.2.10"]]
  :main cinjug1.core
  :cljsbuild {
      :builds [{
         :source-path "src"
         :compiler {:output-to "resources/public/js/main.js"}}]})
