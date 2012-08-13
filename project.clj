(defproject cinjug1 "1.0.0-SNAPSHOT"
  :description "best todo app ever"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [noir "1.2.0"]
                 [fetch "0.1.0-alpha2"]
                 [enfocus "1.0.0-alpha2"]
                 [com.novemberain/monger "1.0.0-beta4"]
                 [clj-crypto "1.0.0"]]
  :main cinjug1.core
  :cljsbuild {
      :builds [{
         :source-path "src"
         :compiler {:output-to "resources/public/js/main.js"}}]})
