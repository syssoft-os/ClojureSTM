(ns clj-stm.core
  (:gen-class))

(def account-a (ref 1000))
(def account-b (ref 1000))

(def counter (atom 0))

(defn transfer [x y amount]
  (dosync
   (alter x - amount)
   (Thread/sleep 100)
   (alter y + amount)
   (swap! counter inc)))

(defn -main
  [& args]
  ; The argument is the number of transfers to perform
  (let [num-transfers (Integer/parseInt (first args))
        ; Create a list of futures, each of which transfers $1 from A to B and back again
        futures (for [n (range num-transfers)]
                  [(future (transfer account-a account-b 1))
                   (future (transfer account-b account-a 1))])]
    ; Wait for all the futures to complete
    (doseq [f (flatten futures)] (deref f))
    ; Print the balances, the accounts should be back where they started
    (println "Account A: " @account-a)
    (println "Account B: " @account-b)
    ; Print the counter
    (println "Counter: " @counter))
  ; Shut down the agents that are used by the STM
  (shutdown-agents))