
# cljs-keypress-handler

### Overview

The <strong>cljs-keypress-handler</strong> is a simple keypress event handler for Clojure projects.

### deps.edn

```
{:deps {bithandshake/cljs-keypress-handler {:git/url "https://github.com/bithandshake/cljs-keypress-handler"
                                            :sha     "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"}}}
```

### Current version

Check out the latest commit on the [release branch](https://github.com/bithandshake/cljs-keypress-handler/tree/release).

### Documentation

The <strong>cljs-keypress-handler</strong> functional documentation is [available here](https://bithandshake.github.io/cljs-keypress-handler).

### Changelog

You can track the changes of the <strong>cljs-keypress-handler</strong> library [here](CHANGES.md).

# Usage

> Some parameters of the following functions and some further functions are not discussed in this file.
  To learn more about the available functionality, check out the [functional documentation](documentation/COVER.md)!

### Index

- [How to register a keypress event?](#how-to-register-a-keypress-event)

- [How to remove an keypress event?](#how-to-remove-a-keypress-event)

- [How to get the currently pressed key list?](#how-to-get-the-currently-pressed-key-list)

- [How to check whether a key pressed now?](#how-to-check-whether-a-key-pressed-now)

- [How to enable type mode?](#how-to-enable-type-mode)

- [How to disable type mode?](#how-to-disable-type-mode)

- [How to provide exclusivity for a keypress event over other keypress events?](#how-to-provide-exclusivity-for-a-keypress-event-over-other-keypress-events)

### How to register a keypress event?

The [`keypress-handler.api/reg-keypress-event!`](documentation/cljs/keypress-handler/API.md#reg-keypress-event)
function registers a keypress-event.

The `:on-keydown` and `:on-keyup` properties take a function that will be fired when
the passed key-code pressed/released.

```
(reg-keypress-event! {:key-code 13
                      :on-keydown (fn [key-code] ...)
                      :on-keyup   (fn [key-code] ...)})
```

By passing an event ID you can remove the registered event later.

```
(reg-keypress-event! :my-event {...})
```

### How to remove a keypress event?

The [`keypress-handler.api/remove-keypress-event!`](documentation/cljs/keypress-handler/API.md#remove-keypress-event)
function removes a registered keypress event.

```
(remove-keypress-event! :my-event)
```

### How to get the currently pressed key list?

The [`keypress-handler.api/get-pressed-keys`](documentation/cljs/keypress-handler/API.md#get-pressed-keys)
function returns the currently pressed keys' codes in a vector.

```
(get-pressed-keys)

; For example it returns 13 and 65 if you are both pressing the ENTER and the A key.
; (get-pressed-keys)
; =>
; [13 65]
```

### How to check wether a key is currently pressed?

The [`keypress-handler.api/key-pressed?`](documentation/cljs/keypress-handler/API.md#key-pressed)
function returns TRUE if the given key-code is currently pressed.

```
(key-pressed? 13)
```

### How to enable type mode?

The [`keypress-handler.api/set-type-mode!`](documentation/cljs/keypress-handler/API.md#set-type-mode)
function enables the type mode and only those events will be fired which got the {:in-type-mode? true} setting.

```
(set-type-mode!)
```

```
; This event won't be fired while the type mode is on.
(reg-keypress-event! {:key-code  13
                      :on-keyup  (fn [key-code] ...)})

; This event will be fired even if the type mode is on.                      
(reg-keypress-event! {:key-code  13
                      :on-keyup  (fn [key-code] ...)
                      :in-type-mode? true})
```

### How to disable type mode?

The [`keypress-handler.api/quit-type-mode!`](documentation/cljs/keypress-handler/API.md#quit-type-mode)
function disables the type mode and all the registered events could be fired.

```
(quit-type-mode!)
```

### How to provide exclusivity for a keypress event over other keypress events?

Passing the `{:exclusive? true}` setting to a keypress event means that other keypress
events with the same key-code will be ignored while the exclusive one registered.
If more than one event (with the same key-code) got the `{:exclusive? true}` setting,
the last registered will be the exclusive one.

```
; This event won't be fired while at least one exclusive event registered with the same key-code.
(reg-keypress-event! {:key-code  13
                      :on-keyup  (fn [key-code] ...)})

; This event will be fired only when the ENTER (13) key pressed.
(reg-keypress-event! {:key-code   13
                      :on-keyup   (fn [key-code] ...)
                      :exclusive? true})
```
