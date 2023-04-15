
(ns keypress-handler.listeners
    (:require [keypress-handler.side-effects :as side-effects]
              [keypress-handler.state        :as state]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @ignore
;
; @constant (function)
(def KEYDOWN-LISTENER (fn [e] (let [key-code (.-keyCode e)]
                                   (if (get @state/PREVENTED-KEYS key-code)
                                       (-> e .preventDefault))
                                   (side-effects/key-pressed key-code))))

; @ignore
;
; @constant (function)
(def KEYUP-LISTENER (fn [e] (let [key-code (.-keyCode e)]
                                 (if (get @state/PREVENTED-KEYS key-code)
                                     (-> e .preventDefault))
                                 (side-effects/key-released key-code))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn add-keypress-listeners!
  ; @ignore
  []
  (window/add-event-listener! "keydown" listeners/KEYDOWN-LISTENER)
  (window/add-event-listener! "keyup"   listeners/KEYUP-LISTENER))

(defn remove-keypress-listeners!
  ; @ignore
  []
  (window/remove-event-listener! "keydown" listeners/KEYDOWN-LISTENER)
  (window/remove-event-listener! "keyup"   listeners/KEYUP-LISTENER))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; Initializing the keypress handler
(add-keypress-listeners!)
