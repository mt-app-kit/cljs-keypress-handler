
(ns keypress-handler.api
    (:require [keypress-handler.listeners]
              [keypress-handler.env          :as env]
              [keypress-handler.side-effects :as side-effects]))

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
