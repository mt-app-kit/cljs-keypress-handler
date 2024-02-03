
(ns keypress-handler.state)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @ignore
;
; @atom (map)
; {*key-code* (keywords in vector)
;   [(keyword) event-id]}
(def EXCLUSIVE-EVENTS (atom {}))

; @ignore
;
; @atom (map)
; {:my-event-id (map)
;   {...}}
(def KEYPRESS-EVENTS (atom {}))

; @ignore
;
; @atom (map)
; {*key-code* (keywords in vector)
;   [(keyword) event-id]}
(def EVENT-CACHE (atom {}))

; @ignore
;
; @atom (map)
; {*key-code* (boolean)}
(def PREVENTED-KEYS (atom {}))

; @ignore
;
; @atom (map)
; {*key-code* (boolean)}
(def PRESSED-KEYS (atom {}))

; @ignore
;
; @atom (boolean)
(def TYPE-MODE? (atom false))
