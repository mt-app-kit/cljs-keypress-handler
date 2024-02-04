
(ns keypress-handler.side-effects
    (:require [fruits.random.api      :as random]
              [fruits.vector.api      :as vector]
              [keypress-handler.env   :as env]
              [keypress-handler.state :as state]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn enable-type-mode!
  ; @note
  ; Keypress events that are registered without the '{:in-type-mode? true}' setting
  ; are ignored while the type mode is enabled.
  ;
  ; @description
  ; Enables the type mode of the keypress handler.
  ;
  ; @usage
  ; (enable-type-mode!)
  []
  (reset! state/TYPE-MODE? true))

(defn disable-type-mode!
  ; @note
  ; Keypress events that are registered without the '{:in-type-mode? true}' setting
  ; are ignored while the type mode is enabled.
  ;
  ; @description
  ; Disables the type mode of the keypress handler.
  ;
  ; @usage
  ; (disable-type-mode!)
  []
  (reset! state/TYPE-MODE? false))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn mark-key-as-pressed!
  ; @ignore
  ;
  ; @description
  ; Stores the given key code in the 'PRESSED-KEYS' atom.
  ;
  ; @param (integer) key-code
  ;
  ; @usage
  ; (mark-key-as-pressed! 27)
  [key-code]
  (swap! state/PRESSED-KEYS assoc key-code true))

(defn unmark-key-as-pressed!
  ; @ignore
  ;
  ; @description
  ; Removes the given key code from the 'PRESSED-KEYS' atom.
  ;
  ; @param (integer) key-code
  ;
  ; @usage
  ; (unmark-key-as-pressed! 27)
  [key-code]
  (swap! state/PRESSED-KEYS dissoc key-code))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn key-pressed
  ; @ignore
  ;
  ; @description
  ; - Stores the given key code in the 'PRESSED-KEYS' atom.
  ; - Calls the 'on-keydown-f' functions of registered keypress events associated with the given key code.
  ; - If the keypress handler is in type mode, calls the 'on-keydown-f' functions only of keypress events
  ;   registered with the '{:in-type-mode? true}' setting.
  ; - Doesn't call the 'on-keydown-f' function of keypress events that are temporarly removed from the event
  ;   cache due to the exclusivity of another keypress event.
  ;
  ; @param (integer) key-code
  ;
  ; @usage
  ; (key-pressed 27)
  [key-code]
  (mark-key-as-pressed! key-code)
  (doseq [on-keydown-f (env/get-events-on-keydown-f key-code)]
         (on-keydown-f key-code)))

(defn key-released
  ; @ignore
  ;
  ; @description
  ; - Removes the given key code from the 'PRESSED-KEYS' atom.
  ; - Calls the 'on-keyup-f' functions of registered keypress events associated with the given key code.
  ; - If the keypress handler is in type mode, calls the 'on-keyup-f' functions only of keypress events
  ;   registered with the '{:in-type-mode? true}' setting.
  ; - Doesn't call the 'on-keyup-f' function of keypress events that are temporarly removed from the event
  ;   cache due to the exclusivity of another keypress event.
  ;
  ; @param (integer) key-code
  ;
  ; @usage
  ; (key-released 27)
  [key-code]
  ; @bug (#5050)
  ; https://stackoverflow.com/questions/25438608/javascript-keyup-isnt-called-when-command-and-another-is-pressed
  (unmark-key-as-pressed! key-code)
  (doseq [on-keyup-f (env/get-events-on-keyup-f key-code)]
         (on-keyup-f key-code)))

(defn prevent-keypress-default!
  ; @ignore
  ;
  ; @description
  ; Enables the prevention of the default browser keypress event associated with the given key code.
  ;
  ; @param (integer) key-code
  ;
  ; @usage
  ; (prevent-keypress-default! 27)
  [key-code]
  (swap! state/PREVENTED-KEYS assoc key-code true))

(defn enable-keypress-default!
  ; @ignore
  ;
  ; @description
  ; Disables the prevention of the default browser keypress event associated with the given key code.
  ;
  ; @param (integer) key-code
  ;
  ; @usage
  ; (enable-keypress-default! 27)
  [key-code]
  (swap! state/PREVENTED-KEYS dissoc key-code))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn store-event-props!
  ; @ignore
  ;
  ; @description
  ; Stores the properties of the given keypress event (or overwrites it if the keypress event is already registered).
  ;
  ; @param (keyword) event-id
  ; @param (map) event-props
  ;
  ; @usage
  ; (store-event-props! :my-event {...})
  [event-id event-props]
  (swap! state/KEYPRESS-EVENTS assoc event-id event-props))

(defn remove-event-props!
  ; @ignore
  ;
  ; @description
  ; Removes the properties of the keypress event that corresponds to the given event ID.
  ;
  ; @param (keyword) event-id
  ;
  ; @usage
  ; (remove-event-props! :my-event)
  [event-id]
  (swap! state/KEYPRESS-EVENTS dissoc event-id))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn set-event-exclusivity!
  ; @ignore
  ;
  ; @note
  ; - If a keypress event is registered as exclusive, other keypress events associated with the same key code are ignored.
  ; - If multiple registered keypress events (associated with the same key code) are registered
  ;   as exclusive, the last registered takes presedence (as the 'most' exclusive).
  ; - Deregistering the exclusive keypress event restores exclusivity of the previous exclusive one (if any).
  ;   Therefore, event IDs of exclusive events are stored in a vector in order of registration time.
  ; - The event exclusivity is granted by removing other events (associated with the same key code) from the event cache.
  ;
  ; @description
  ; Grants exclusivity for a specific registered keypress event over other keypress events associated with the same key code.
  ;
  ; @param (keyword) event-id
  ; @param (map) event-props
  ; {:key-code (integer)}
  ;
  ; @usage
  ; (set-event-exclusivity! :my-event {...})
  [event-id {:keys [key-code]}]
  (swap! state/EXCLUSIVE-EVENTS update key-code vector/conj-item event-id))

(defn unset-event-exclusivity!
  ; @ignore
  ;
  ; @description
  ; ...
  ;
  ; @param (keyword) event-id
  ;
  ; @usage
  ; (unset-event-exclusivity! :my-event)
  [event-id]
  (let [key-code (env/get-event-key-code event-id)]
       (swap! state/EXCLUSIVE-EVENTS update key-code vector/remove-item event-id)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn cache-event!
  ; @ignore
  ;
  ; @note
  ; - The keypress handler uses cache to store event IDs associated with key codes.
  ; - When a key is pressed the keypress handler gets the event IDs (that are associated
  ;   with the key code of the pressed key) from the event cache.
  ; - Caching events helps the keypress handler to get the event IDs as quick as possible,
  ;   without performing any action (e.g., deriving event IDs from the event state) when a key gets pressed.
  ;
  ; @description
  ; Adds the given event ID to the corresponding key code in the event cache.
  ;
  ; @param (keyword) event-id
  ; @param (map) event-props
  ; {:key-code (integer)
  ;  :on-keydown-f (function)(opt)
  ;  :on-keyup-f (function)(opt)}
  ;
  ; @usage
  ; (cache-event! :my-event {...})
  [event-id {:keys [key-code on-keydown-f on-keyup-f]}]
  ; @note (#1160)
  ; Using the 'conj-item-once' function helps prevent duplications of event IDs within the event cache,
  ; escpecially when an event gets re-registered with the same event ID.
  (if on-keydown-f (swap! state/EVENT-CACHE update-in [key-code :keydown-events] vector/conj-item-once event-id))
  (if on-keyup-f   (swap! state/EVENT-CACHE update-in [key-code :keyup-events]   vector/conj-item-once event-id)))

(defn uncache-event!
  ; @ignore
  ;
  ; @description
  ; Removes the given event ID from the event cache.
  ;
  ; @param (keyword) event-id
  ;
  ; @usage
  ; (uncache-event! :my-event)
  [event-id]
  (let [key-code (env/get-event-key-code event-id)]
       (swap! state/EVENT-CACHE update-in [key-code :keydown-events] vector/remove-item event-id)
       (swap! state/EVENT-CACHE update-in [key-code :keyup-events]   vector/remove-item event-id)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn rebuild-key-cache!
  ; @ignore
  ;
  ; @description
  ; Rebuilds the cache of the given key code.
  ;
  ; @param (integer) key-code
  ;
  ; @usage
  ; (rebuild-key-cache! 27)
  [key-code]
  (doseq [[event-id event-props] @state/KEYPRESS-EVENTS]
         (if (= key-code (:key-code event-props))
             (cache-event! event-id event-props))))

(defn empty-key-cache!
  ; @ignore
  ;
  ; @description
  ; Removes cached event IDs associated with the given key code.
  ;
  ; @param (integer) key-code
  ;
  ; @usage
  ; (empty-key-cache! 27)
  [key-code]
  (swap! state/EVENT-CACHE dissoc key-code))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn cache-second-exclusive-event!
  ; @ignore
  ;
  ; @description
  ; Caches the event ID of the second exclusive event associated with the given key code.
  ;
  ; @param (integer) key-code
  ;
  ; @usage
  ; (cache-second-exclusive-event! 27)
  [key-code]
  (let [event-id    (env/get-key-second-exclusive-event key-code)
        event-props (env/get-event-props event-id)]
       (cache-event! event-id event-props)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn reg-keypress-event!
  ; @description
  ; - Registers a keypress event associated with the given key code.
  ; - When the key is pressed, the given 'on-keydown-f' function is called.
  ;   When the key is released, the given 'on-keyup-f' function is called.
  ; - If the type mode of the keypress handler is enabled, the event functions
  ;   are ignored, unless the event is registered with the '{:in-type-mode? true}' setting.
  ; - The '{:exclusive? true}' setting grants exclusivity over other registered keypress
  ;   events associated with the same key code (that are ignored until the exclusive one is removed).
  ;   If multiple events are registered as exclusive, the last registered is declared as the exclusive one.
  ;
  ; @param (keyword)(opt) event-id
  ; @param (map) event-props
  ; {:exclusive? (boolean)(opt)
  ;  :key-code (integer)
  ;  :in-type-mode? (boolean)(opt)
  ;  :on-keydown-f (function)(opt)
  ;  :on-keyup-f (function)(opt)
  ;  :prevent-default? (boolean)(opt)}
  ;
  ; @usage
  ; (reg-keypress-event! {...})
  ;
  ; @usage
  ; (reg-keypress-event! :my-keypress-event {...})
  ;
  ; @usage
  ; (reg-keypress-event! {:key-code 65 :on-keydown-f (fn [key-code] ...)
  ;                                    :on-keyup-f   (fn [key-code] ...)})
  ([event-props]
   (reg-keypress-event! (random/generate-keyword) event-props))

  ([event-id {:keys [exclusive? key-code prevent-default?] :as event-props}]
   (when prevent-default? (prevent-keypress-default! key-code))
   (when exclusive?       (empty-key-cache!          key-code)
                          (set-event-exclusivity!    event-id event-props)
                          (cache-event!              event-id event-props))
   (when :always          (store-event-props!        event-id event-props))
   (if-not (env/any-exclusive-event-set? key-code)
           (cache-event! event-id event-props))))

(defn dereg-keypress-event!
  ; @description
  ; - Deregisters the keypress event that corresponds to the given event ID.
  ; - If the keypress event is registered as exclusive, re-enables other temporarly
  ;   disabled keypress events associated with the same key code.
  ;
  ; @param (keyword) event-id
  ;
  ; @usage
  ; (dereg-keypress-event! :my-event)
  [event-id]
  ; If the deregistered keypress event ...
  ; ... was the only exclusive event associated with the same key code,
  ;     rebuilds the cache of the key code.
  ; ... was the most (and not only!) exclusive event associated with the same key code,
  ;     caches the second most exclusive keypress event.
  ; ... was not the only or most exclusive event associated with the same key code,
  ;     there is no need to change the cache.
  (let [key-code (env/get-event-key-code event-id)]
       (when (env/enable-default?                event-id) (enable-keypress-default!      key-code))
       (cond (env/event-only-exclusive?          event-id) (rebuild-key-cache!            key-code)
             (env/event-most-exclusive?          event-id) (cache-second-exclusive-event! key-code))
       (when (env/event-registered-as-exclusive? event-id) (unset-event-exclusivity!      event-id))
       (when :always                                       (remove-event-props!           event-id)
                                                           (uncache-event!                event-id))))
