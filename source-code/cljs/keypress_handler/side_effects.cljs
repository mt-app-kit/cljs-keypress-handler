
(ns keypress-handler.side-effects
    (:require [keypress-handler.env   :as env]
              [keypress-handler.state :as state]
              [random.api             :as random]
              [vector.api             :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn set-type-mode!
  ; @usage
  ; (set-type-mode!)
  []
  (reset! state/TYPE-MODE? true))

(defn quit-type-mode!
  ; @usage
  ; (quit-type-mode!)
  []
  (reset! state/TYPE-MODE? false))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn prevent-keypress-default!
  ; @ignore
  ;
  ; @param (integer) key-code
  [key-code]
  (swap! state/PREVENTED-KEYS assoc key-code true))

(defn enable-keypress-default!
  ; @ignore
  ;
  ; @param (integer) key-code
  [key-code]
  (swap! state/PREVENTED-KEYS dissoc key-code))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn store-event-props!
  ; @ignore
  ;
  ; @description
  ; XXX#1160
  ; Stores the event properties (or overwrites it when a keypress event registered again).
  ;
  ; @param (keyword) event-id
  ; @param (map) event-props
  [event-id event-props]
  (swap! state/KEYPRESS-EVENTS assoc event-id event-props))

(defn remove-event-props!
  ; @ignore
  ;
  ; @param (keyword) event-id
  [event-id]
  (swap! state/KEYPRESS-EVENTS dissoc event-id))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn cache-event!
  ; @ignore
  ;
  ; @description
  ; When registering a keypress event the event ID will be cached for performance reasons.
  ; By caching event IDs the keypress handler doesn't have to look up all
  ; the registered keypress events when a key pressed or released.
  ; Every registered key-code has a vector in the cache with the event IDs of
  ; the events which use that key-code.
  ;
  ; XXX#1160
  ; If a keypress event registered again, the cache doesn't store its ID again
  ; (to avoid duplicates in the cache).
  ;
  ; @param (keyword) event-id
  ; @param (map) event-props
  ; {:key-code (integer)
  ;  :on-keydown (metamorphic-event)(opt)
  ;  :on-keyup (metamorphic-event)(opt)}
  [event-id {:keys [key-code on-keydown on-keyup]}]
  (if on-keydown (swap! state/EVENT-CACHE update-in [key-code :keydown-events] vector/conj-item-once event-id))
  (if on-keyup   (swap! state/EVENT-CACHE update-in [key-code :keyup-events]   vector/conj-item-once event-id)))

(defn uncache-event!
  ; @ignore
  ;
  ; @description
  ; Removes the event from the cache.
  ;
  ; @param (keyword) event-id
  [event-id]
  (let [key-code (get-in @state/KEYPRESS-EVENTS [event-id :key-code])]
       (swap! state/EVENT-CACHE update-in [key-code :keydown-events] vector/remove-item event-id)
       (swap! state/EVENT-CACHE update-in [key-code :keyup-events]   vector/remove-item event-id)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn set-exclusivity!
  ; @ignore
  ;
  ; @param (keyword) event-id
  ; @param (map) event-props
  ; {}
  [event-id {:keys [key-code] :as event-props}]
  ; By storing the event ID in the EXCLUSIVE-EVENTS vector, later when the event
  ; will be removed, the previous most exclusive event could be restored using the
  ; EXCLUSIVE-EVENTS vector.
  (swap! state/EXCLUSIVE-EVENTS update key-code vector/conj-item event-id)
  ; Removing all the registered events from the cache with the same key-code
  ; and the event will be the only one in the cache with this key-code.
  (swap! state/EVENT-CACHE dissoc key-code))

(defn unset-exclusivity!
  ; @ignore
  ;
  ; @param (keyword) event-id
  [event-id]
  ; Most exclusive: last registered exclusive event with the same key-code.
  ; Only exclusive: only registered exclusive event with the same key-code.
  ; + The only exclusive is the most exclusive of course.
  (let [key-code         (get-in @state/KEYPRESS-EVENTS [event-id :key-code])
        exclusive-events (get @state/EXCLUSIVE-EVENTS key-code)
        most-exclusive?  (vector/item-last? exclusive-events event-id)
        only-exclusive?  (vector/only-item? exclusive-events event-id)]
       (swap! state/EXCLUSIVE-EVENTS update key-code vector/remove-item event-id)
       ; If the event was ...
       ; ... the only exclusive with the same key-code the event cache
       ;     will be restored.
       ; ... the most but not only exclusive with the same key-code
       ;     the second most exclusive will be the new most exclusive event.
       ; ... not the only or most exclusive with the same key-code
       ;     the cache will be unchanged.
       (cond only-exclusive? (doseq [[event-id event-props] @state/KEYPRESS-EVENTS]
                                    (if (= key-code (:key-code event-props))
                                        (cache-event! event-id event-props)))
             most-exclusive? (let [second-exclusive-id    (-> exclusive-events vector/remove-last-item vector/last-item)
                                   second-exclusive-props (get @state/KEYPRESS-EVENTS second-exclusive-id)]
                                  (cache-event! second-exclusive-id second-exclusive-props)))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn mark-key-as-pressed!
  ; @ignore
  ;
  ; @param (integer) key-code
  [key-code]
  (swap! state/PRESSED-KEYS assoc key-code true))

(defn unmark-key-as-pressed!
  ; @ignore
  ;
  ; @param (integer) key-code
  [key-code]
  (swap! state/PRESSED-KEYS dissoc key-code))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn reg-keypress-event!
  ; @param (keyword)(opt) event-id
  ; @param (map) event-props
  ; {:exclusive? (boolean)(opt)
  ;   If true, other (previously registered) keypress events with the same
  ;   key-code will be ignored until the exclusive one removed.
  ;   If more than one exclusive event registered with the same key-code, the
  ;   last registered will be the exclusive one.
  ;   Default: false
  ;  :key-code (integer)
  ;  :on-keydown (function)(opt)
  ;  :on-keyup (function)(opt)
  ;  :prevent-default? (boolean)(opt)
  ;   Default: false
  ;  :required? (boolean)(opt)
  ;   If true, the event won't be ignored when the type mode is on.
  ;   Default: false}
  ;
  ; @usage
  ; (reg-keypress-event! {...})
  ;
  ; @usage
  ; (reg-keypress-event! :my-keypress-event {...})
  ;
  ; @usage
  ; (reg-keypress-event! {:key-code 65 :on-keydown (fn [key-code] ...)})
  ([event-props]
   (reg-keypress-event! (random/generate-keyword) event-props))

  ([event-id {:keys [exclusive? key-code prevent-default?] :as event-props}]
   (if prevent-default? (prevent-keypress-default! key-code))
   (if exclusive?       (set-exclusivity! event-id event-props))
   (if-let [no-exclusive-set? (-> @state/EXCLUSIVE-EVENTS (get key-code) empty?)]
           (cache-event! event-id event-props)
           (if exclusive? (cache-event! event-id event-props)))
   (store-event-props! event-id event-props)))

(defn remove-keypress-event!
  ; @param (keyword) event-id
  ;
  ; @usage
  ; (remove-keypress-event! :my-event)
  [event-id]
  (if (env/enable-default? event-id)
      (let [key-code (get-in @state/KEYPRESS-EVENTS [event-id :key-code])]
           (enable-keypress-default! key-code)))
  (if-let [exclusive? (get-in @state/KEYPRESS-EVENTS [event-id :exclusive?])]
          (unset-exclusivity! event-id))
  (uncache-event!      event-id)
  (remove-event-props! event-id))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn key-pressed
  ; @ignore
  ;
  ; @param (integer) key-code
  [key-code]
  (mark-key-as-pressed! key-code)
  (doseq [on-keydown (env/get-keydown-events key-code)]
         (on-keydown)))

(defn key-released
  ; @ignore
  ;
  ; @param (integer) key-code
  [key-code]
  ; BUG#5050
  ; https://stackoverflow.com/questions/25438608/javascript-keyup-isnt-called-when-command-and-another-is-pressed
  (unmark-key-as-pressed! key-code)
  (doseq [on-keyup (env/get-keyup-events key-code)]
         (on-keyup)))
