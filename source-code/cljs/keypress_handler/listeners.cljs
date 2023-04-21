
(ns keypress-handler.listeners
    (:require [keypress-handler.side-effects :as side-effects]
              [keypress-handler.state        :as state]
              [window.api                    :as window]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @ignore
;
; @constant (function)
; BUG#0180
; https://stackoverflow.com/questions/6087959/prevent-javascript-keydown-event-from-being-handled-multiple-times-while-held-do
; https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/repeat
(def KEYDOWN-LISTENER (fn [e] (if (-> e .-repeat not)
                                  (let [key-code (.-keyCode e)]
                                       (if (get @state/PREVENTED-KEYS key-code)
                                           (.preventDefault e))
                                       (side-effects/key-pressed key-code)))))

; @ignore
;
; @constant (function)
; BUG#0180
(def KEYUP-LISTENER (fn [e] (if (-> e .-repeat not)
                                (let [key-code (.-keyCode e)]
                                     (if (get @state/PREVENTED-KEYS key-code)
                                         (.preventDefault e))
                                     (side-effects/key-released key-code)))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn add-keypress-listeners!
  ; @ignore
  []
  (window/add-event-listener! "keydown" KEYDOWN-LISTENER)
  (window/add-event-listener! "keyup"   KEYUP-LISTENER))

(defn remove-keypress-listeners!
  ; @ignore
  []
  (window/remove-event-listener! "keydown" KEYDOWN-LISTENER)
  (window/remove-event-listener! "keyup"   KEYUP-LISTENER))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; Initializing the keypress handler
(add-keypress-listeners!)
