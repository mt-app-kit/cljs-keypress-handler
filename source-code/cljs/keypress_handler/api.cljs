
(ns keypress-handler.api
    (:require [keypress-handler.listeners]
              [keypress-handler.env          :as env]
              [keypress-handler.side-effects :as side-effects]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @redirect (keypress-handler.env)
(def get-pressed-keys env/get-pressed-keys)
(def key-pressed?     env/key-pressed?)

; @redirect (keypress-handler.side-effects)
(def set-type-mode!        side-effects/set-type-mode!)
(def quit-type-mode!       side-effects/quit-type-mode!)
(def reg-keypress-event!   side-effects/reg-keypress-event!)
(def dereg-keypress-event! side-effects/dereg-keypress-event!)
