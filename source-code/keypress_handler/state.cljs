
(ns keypress-handler.state)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @ignore
;
; @atom (map)
(def KEYPRESS-EVENTS (atom {}))

; @ignore
;
; @atom (map)
(def EVENT-CACHE (atom {}))

; @ignore
;
; @atom (map)
(def PREVENTED-KEYS (atom {}))

; @ignore
;
; @atom (map)
(def PRESSED-KEYS (atom {}))

; @ignore
;
; @atom (boolean)
(def TYPE-MODE? (atom false))
