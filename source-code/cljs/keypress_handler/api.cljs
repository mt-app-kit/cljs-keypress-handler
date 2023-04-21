
(ns keypress-handler.api
    (:require [keypress-handler.listeners]
              [keypress-handler.env          :as env]
              [keypress-handler.side-effects :as side-effects]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; keypress-handler.env
(def get-pressed-keys env/get-pressed-keys)
(def key-pressed?     env/key-pressed?)

; keypress-handler.side-effects
(def set-type-mode!         side-effects/set-type-mode!)
(def quit-type-mode!        side-effects/quit-type-mode!)
(def reg-keypress-event!    side-effects/reg-keypress-event!)
(def remove-keypress-event! side-effects/remove-keypress-event!)
