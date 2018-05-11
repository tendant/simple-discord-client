(ns simple-discord.client
  (:require [clj-http.client :as http]))

;; https://discordapp.com/developers/docs/reference
(def base-url "https://discordapp.com/api")

(defn endpoint
  [uri]
  (str base-url uri))

(defn create-message
  "https://discordapp.com/developers/docs/resources/channel#create-message

  Before using this endpoint, you must connect to and identify with a gateway at least once.

  auth: {:bot TOKEN} or {:bearer TOKEN}
  channel-id: the id of this channel. Type snowflake
  message: {:content \"string, required, the message contents(up to 2000 characters)\"}"
  [auth channel-id message]
  (if (clojure.string/blank? (:content message))
    (println "discord create-message: blank message")
    (let [token-type (cond
                       (:bot auth) "Bot"
                       (:bearer auth) "Bearer")
          token (or (:bot auth) (:bearer auth))
          authorization (format "%s %s" token-type token)
          uri (format "/channels/%s/messages" channel-id)
          content (:content message)
          truncated-content (if (> (.length content) 2000)
                              (subs content 0 2000)
                              content)
          resp (http/post (endpoint uri)
                          {:headers {"Authorization" authorization}
                           :content-type :json
                           :accept :json
                           :form-params (assoc message :content truncated-content)
                           :throw-exceptions false})]
      (println "status:" (:status resp))
      resp)))

(defn create-message-long
  "Send long message, split message for up to 5 separated messages"
  [auth channel-id message]
  (if-let [content (:content message)]
    (let [col (re-seq #"(?s).{1,1990}" content)
          contents (take 5 col)]
      (doseq [msg contents]
        (create-message auth
                        channel-id
                        (assoc message :content msg))))))