
(ns keypress-handler.env
    (:require [fruits.vector.api      :as vector]
              [keypress-handler.state :as state]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn type-mode-enabled?
  ; @note
  ; Keypress events that are registered without the '{:in-type-mode? true}' setting are ignored while the type mode is enabled.
  ;
  ; @description
  ; Returns TRUE if the type mode of the keypress handler is enabled.
  ;
  ; @usage
  ; (type-mode-enabled?)
  ; =>
  ; true
  ;
  ; @return (boolean)
  []
  (-> state/TYPE-MODE? deref))

(defn type-mode-disabled?
  ; @note
  ; Keypress events that are registered without the '{:in-type-mode? true}' setting are ignored while the type mode is enabled.
  ;
  ; @description
  ; Returns TRUE if the type mode of the keypress handler is disabled.
  ;
  ; @usage
  ; (type-mode-disabled?)
  ; =>
  ; true
  ;
  ; @return (boolean)
  []
  (-> state/TYPE-MODE? deref not))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-pressed-keys
  ; @description
  ; Returns the key codes of the currently pressed keys.
  ;
  ; @usage
  ; (get-pressed-keys)
  ; =>
  ; [27 65]
  ;
  ; @return (integers in vector)
  []
  (keys @state/PRESSED-KEYS))

(defn key-pressed?
  ; @description
  ; Returns TRUE if the given key code corresponds to a currently pressed key.
  ;
  ; @param (integer) key-code
  ;
  ; @usage
  ; (key-pressed? 27)
  ; =>
  ; true
  ;
  ; @return (boolean)
  [key-code]
  (get @state/PRESSED-KEYS key-code))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-event-props
  ; @ignore
  ;
  ; @description
  ; Returns the properties of the registered keypress event that corresponds to the given event ID.
  ;
  ; @param (keyword) event-id
  ;
  ; @usage
  ; (get-event-props :my-event)
  ; =>
  ; {...}
  ;
  ; @return (map)
  [event-id]
  (get @state/KEYPRESS-EVENTS event-id))

(defn get-event-key-code
  ; @ignore
  ;
  ; @description
  ; Returns the key code associated with the registered keypress event that corresponds to the given event ID.
  ;
  ; @param (keyword) event-id
  ;
  ; @usage
  ; (get-event-key-code :my-event)
  ; =>
  ; 27
  ;
  ; @return (integer)
  [event-id]
  (if-let [event-props (get-event-props event-id)]
          (:key-code event-props)))

(defn get-other-events
  ; @ignore
  ;
  ; @description
  ; Returns the properties of the registered keypress events except the one that corresponds to the given event ID.
  ;
  ; @param (keyword) event-id
  ;
  ; @usage
  ; (get-other-events :my-event)
  ; =>
  ; {:another-event {...}}
  ;
  ; @return (map)
  [event-id]
  (dissoc @state/KEYPRESS-EVENTS event-id))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-event-on-keydown-f
  ; @ignore
  ;
  ; @description
  ; - Returns the 'on-keydown-f' function (if any) of the registered keypress event that corresponds to the given event ID.
  ; - If the keypress handler is in type mode, returns the 'on-keydown-f' function only when the event is registered
  ;   with the '{:in-type-mode? true}' setting.
  ;
  ; @param (keyword) event-id
  ;
  ; @usage
  ; (get-event-on-keydown-f :my-event)
  ; =>
  ; (fn [] ...)
  ;
  ; @return (function)
  [event-id]
  (let [{:keys [in-type-mode? on-keydown-f]} (get-event-props event-id)]
       (cond in-type-mode? on-keydown-f (type-mode-disabled?) on-keydown-f)))

(defn get-event-on-keyup-f
  ; @ignore
  ;
  ; @description
  ; - Returns the 'on-keyup-f' function (if any) of the registered keypress event that corresponds to the given event ID.
  ; - If the keypress handler is in type mode, returns the 'on-keyup-f' function only when the event is registered
  ;   with the '{:in-type-mode? true}' setting.
  ;
  ; @param (keyword) event-id
  ;
  ; @usage
  ; (get-event-on-keyup-f :my-event)
  ; =>
  ; (fn [] ...)
  ;
  ; @return (function)
  [event-id]
  (let [{:keys [in-type-mode? on-keyup-f]} (get-event-props event-id)]
       (cond in-type-mode? on-keyup-f (type-mode-disabled?) on-keyup-f)))

(defn get-events-on-keydown-f
  ; @ignore
  ;
  ; @description
  ; - Returns the 'on-keydown-f' functions of registered keypress events associated with the given key code.
  ; - If the keypress handler is in type mode, returns the 'on-keydown-f' functions only of keypress events
  ;   registered with the '{:in-type-mode? true}' setting.
  ; - Doesn't return the 'on-keydown-f' function of keypress events that are temporarly removed from the event
  ;   cache due to the exclusivity of another keypress event.
  ;
  ; @param (integer) key-code
  ;
  ; @usage
  ; (get-events-on-keydown-f 27)
  ; =>
  ; [(fn [] ...) (fn [] ...)]
  ;
  ; @return (functions in vector)
  [key-code]
  (letfn [(f0 [result event-id] (if-let [on-keydown-f (get-event-on-keydown-f event-id)]
                                        (-> result (conj on-keydown-f))
                                        (-> result)))]
         (let [event-ids (get-in @state/EVENT-CACHE [key-code :keydown-events])]
              (reduce f0 [] event-ids))))

(defn get-events-on-keyup-f
  ; @ignore
  ;
  ; @description
  ; - Returns the 'on-keyup-f' functions of registered keypress events associated with the given key code.
  ; - If the keypress handler is in type mode, returns the 'on-keyup-f' functions only of keypress events
  ;   registered with the '{:in-type-mode? true}' setting.
  ; - Doesn't return the 'on-keyup-f' function of keypress events that are temporarly removed from the event
  ;   cache due to the exclusivity of another keypress event.
  ;
  ; @param (integer) key-code
  ;
  ; @usage
  ; (get-events-on-keyup-f 27)
  ; =>
  ; [(fn [] ...) (fn [] ...)]
  ;
  ; @return (functions in vector)
  [key-code]
  (letfn [(f0 [result event-id] (if-let [on-keyup-f (get-event-on-keyup-f event-id)]
                                        (-> result (conj on-keyup-f))
                                        (-> result)))]
         (let [event-ids (get-in @state/EVENT-CACHE [key-code :keyup-events])]
              (reduce f0 [] event-ids))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn keypress-prevented-by-event?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if the registered keypress event (that corresponds to the given event ID),
  ; prevents the default browser keypress event of the associated key.
  ;
  ; @param (keyword) event-id
  ;
  ; @usage
  ; (keypress-prevented-by-event? :my-event)
  ;
  ; @return (boolean)
  [event-id]
  (if-let [event-props (get-event-props event-id)]
          (:prevent-default? event-props)))

(defn keypress-prevented-by-another-event?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if the default browser keypress event of the key associated with the registered
  ; keypress event (that corresponds to the given event ID) is prevented by another registered
  ; keypress event(s).
  ;
  ; @param (keyword) event-id
  ;
  ; @usage
  ; (keypress-prevented-by-another-event? :my-event)
  ;
  ; @return (boolean)
  [event-id]
  (let [key-code     (get-event-key-code event-id)
        other-events (get-other-events   event-id)]
       (letfn [(f0 [[_ event-props]] (and (-> event-props :key-code (= key-code))
                                          (-> event-props :prevent-default?)))]
              (some f0 other-events))))

(defn enable-default?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if the default browser keypress event associated with the key corresponding
  ; to the given event ID must be re-enabled after deregistering the keypress event.
  ;
  ; @param (keyword) event-id
  ;
  ; @usage
  ; (enable-default? :my-event)
  ;
  ; @return (boolean)
  [event-id]
  (and (-> event-id keypress-prevented-by-event?)
       (-> event-id keypress-prevented-by-another-event? not)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-key-exclusive-events
  ; @ignore
  ;
  ; @description
  ; Returns the event IDs of events associated with the given key code that are registered as exclusive.
  ;
  ; @param (integer) key-code
  ;
  ; @usage
  ; (get-exclusive-events 27)
  ; =>
  ; [:my-event :another-event]
  ;
  ; @return (keywords in vector)
  [key-code]
  (get @state/EXCLUSIVE-EVENTS key-code))

(defn any-exclusive-event-set?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if any keypress event associated with the given key code is registered as exclusive.
  ;
  ; @param (integer) key-code
  ;
  ; @usage
  ; (any-exclusive-event-set? 27)
  ; =>
  ; true
  ;
  ; @return (boolean)
  [key-code]
  (if-let [exclusive-events (get-key-exclusive-events key-code)]
          (-> exclusive-events empty? not)))

(defn get-key-second-exclusive-event
  ; @ignore
  ;
  ; @description
  ; Returns the event ID of the second exclusive keypress event associated with the given key code.
  ;
  ; @param (integer) key-code
  ;
  ; @usage
  ; (get-key-second-exclusive-event 27)
  ; =>
  ; :my-event
  ;
  ; @return (keyword)
  [key-code]
  (if-let [exclusive-events (get-key-exclusive-events key-code)]
          (-> exclusive-events vector/remove-last-item vector/last-item)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn event-registered-as-exclusive?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if the registered keypress event (that corresponds to the given event ID) is registered as exclusive.
  ;
  ; @param (keyword) event-id
  ;
  ; @usage
  ; (event-registered-as-exclusive? :my-event)
  ;
  ; @return (boolean)
  [event-id]
  ; If a keypress event is registered as exclusive that doesn't mean it is considered as the exclusive event at the moment.
  ; Another keypress event might be registered as exclusive later and took its exclusivity.
  (if-let [event-props (get-event-props event-id)]
          (:exclusive? event-props)))

(defn event-only-exclusive?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if ...
  ; ... the registered keypress event (that corresponds to the given event ID) is registered as exclusive,
  ; ... no other keypress events (associated with the same key code) are registered as exclusive.
  ;
  ; @param (keyword) event-id
  ;
  ; @usage
  ; (event-only-exclusive? :my-event)
  ;
  ; @return (boolean)
  [event-id]
  (let [key-code         (get-event-key-code event-id)
        exclusive-events (get-key-exclusive-events key-code)]
       (vector/item-only? exclusive-events event-id)))

(defn event-most-exclusive?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if ...
  ; ... the registered keypress event (that corresponds to the given event ID) is registered as exclusive,
  ; ... the registered keypress event is the 'most' or only exclusive event associated with the same key code.
  ;
  ; @param (keyword) event-id
  ;
  ; @usage
  ; (event-most-exclusive? :my-event)
  ;
  ; @return (boolean)
  [event-id]
  (let [key-code         (get-event-key-code event-id)
        exclusive-events (get-key-exclusive-events key-code)]
       (vector/item-last? exclusive-events event-id)))
