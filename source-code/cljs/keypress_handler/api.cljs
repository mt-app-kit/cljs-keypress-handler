
(ns keypress-handler.api
    (:require [keypress-handler.listeners]
              [keypress-handler.env          :as env]
              [keypress-handler.side-effects :as side-effects]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @tutorial Keypress events
;
; Keypress events are registered side effect functions associated to the keydown or keyup event of a specific key code.
;
; @usage
; (reg-keypress-event! {:key-code 65 {:on-keydown-f (fn [key-code] ...)
;                                     :on-keyup-f   (fn [key-code] ...)}})

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @redirect (keypress-handler.env/*)
(def type-mode-enabled?  env/type-mode-enabled?)
(def type-mode-disabled? env/type-mode-disabled?)
(def get-pressed-keys    env/get-pressed-keys)
(def key-pressed?        env/key-pressed?)

; @redirect (keypress-handler.side-effects/*)
(def enable-type-mode!     side-effects/enable-type-mode!)
(def disable-type-mode!    side-effects/disable-type-mode!)
(def reg-keypress-event!   side-effects/reg-keypress-event!)
(def dereg-keypress-event! side-effects/dereg-keypress-event!)
