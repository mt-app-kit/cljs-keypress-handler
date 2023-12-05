
(ns keypress-handler.env
    (:require [keypress-handler.state :as state]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-pressed-keys
  ; @usage
  ; (get-pressed-keys?)
  ;
  ; @return (integers in vector)
  []
  (keys @state/PRESSED-KEYS))

(defn key-pressed?
  ; @param (integer) key-code
  ;
  ; @usage
  ; (key-pressed?)
  ;
  ; @return (boolean)
  [key-code]
  (get @state/PRESSED-KEYS key-code))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn keypress-prevented-by-event?
  ; @ignore
  ;
  ; @param (keyword) event-id
  ;
  ; @return (boolean)
  [event-id]
  (get-in @state/KEYPRESS-EVENTS [event-id :prevent-default?]))

(defn keypress-prevented-by-other-events?
  ; @ignore
  ;
  ; @param (keyword) event-id
  ;
  ; @return (boolean)
  [event-id]
  (let [key-code     (get-in @state/KEYPRESS-EVENTS [event-id :key-code])
        other-events (dissoc @state/KEYPRESS-EVENTS  event-id)]
       (letfn [(f0 [[_ event-props]] (and (:prevent-default? event-props)
                                          (= key-code (:key-code event-props))))]
              (some f0 other-events))))

(defn enable-default?
  ; @ignore
  ;
  ; @param (keyword) event-id
  ;
  ; @return (boolean)
  [event-id]
  ; Enable default if prevented by event and NOT prevented by other events ...
  (and      (keypress-prevented-by-event?        event-id)
       (not (keypress-prevented-by-other-events? event-id))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-keydown-event
  ; @ignore
  ;
  ; @param (keyword) event-id
  ;
  ; @return (function)
  [event-id]
  (let [{:keys [on-keydown required?]} (event-id @state/KEYPRESS-EVENTS)]
       (cond required? on-keydown (not @state/TYPE-MODE?) on-keydown)))

(defn get-keyup-event
  ; @ignore
  ;
  ; @param (keyword) event-id
  ;
  ; @return (function)
  [event-id]
  (let [{:keys [on-keyup required?]} (event-id @state/KEYPRESS-EVENTS)]
       (cond required? on-keyup (not @state/TYPE-MODE?) on-keyup)))

(defn get-keydown-events
  ; @ignore
  ;
  ; @description
  ; - Returns the keydown events registered for the given key code.
  ; - In type mode, only the {:required? true} events will be returned.
  ;
  ; @param (integer) key-code
  ;
  ; @return (functions in vector)
  [key-code]
  (letfn [(f0 [keydown-events event-id] (if-let [keydown-event (get-keydown-event event-id)] ; <- The 'get-keydown-event' function could return NIL in type mode
                                                (conj keydown-events keydown-event) keydown-events))]
         (let [event-ids (get-in @state/EVENT-CACHE [key-code :keydown-events])]
              (reduce f0 [] event-ids))))

(defn get-keyup-events
  ; @ignore
  ;
  ; @description
  ; - Returns the keyup events registered for the given key code.
  ; - In type mode only the {:required? true} events will be returned.
  ;
  ; @param (integer) key-code
  ;
  ; @return (functions in vector)
  [key-code]
  (letfn [(f0 [keyup-events event-id] (if-let [keyup-event (get-keyup-event event-id)] ; <- The 'get-keyup-event' function could return NIL in type mode
                                              (conj keyup-events keyup-event) keyup-events))]
         (let [event-ids (get-in @state/EVENT-CACHE [key-code :keyup-events])]
              (reduce f0 [] event-ids))))
