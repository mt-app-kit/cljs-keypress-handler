
(ns keypress-handler.listeners
    (:require [keypress-handler.side-effects :as side-effects]
              [keypress-handler.state        :as state]
              [window.api                    :as window]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @ignore
;
; @constant (function)
;
; @bug (#0180)
; https://stackoverflow.com/questions/6087959/prevent-javascript-keydown-event-from-being-handled-multiple-times-while-held-do
; https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/repeat
(def ON-KEYDOWN-LISTENER (fn [e] (if (-> e .-repeat not)
                                     (let [key-code (.-keyCode e)]
                                          (if (-> state/PREVENTED-KEYS deref (get key-code))
                                              (-> e .preventDefault))
                                          (side-effects/key-pressed key-code)))))

; @ignore
;
; @constant (function)
;
; @bug (#0180)
(def ON-KEYUP-LISTENER (fn [e] (if (-> e .-repeat not)
                                   (let [key-code (.-keyCode e)]
                                        (if (-> state/PREVENTED-KEYS deref (get key-code))
                                            (-> e .preventDefault))
                                        (side-effects/key-released key-code)))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn add-keypress-listeners!
  ; @ignore
  ;
  ; @description
  ; Adds the keydown and keyup listener functions of the keypress handler to the Window object.
  []
  (window/add-event-listener! "keydown" ON-KEYDOWN-LISTENER)
  (window/add-event-listener! "keyup"   ON-KEYUP-LISTENER))

(defn remove-keypress-listeners!
  ; @ignore
  ;
  ; @description
  ; Removes the keydown and keyup listener functions of the keypress handler from the Window object.
  []
  (window/remove-event-listener! "keydown" ON-KEYDOWN-LISTENER)
  (window/remove-event-listener! "keyup"   ON-KEYUP-LISTENER))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; Initializing the keypress handler ...
(add-keypress-listeners!)
