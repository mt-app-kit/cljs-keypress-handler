
# keypress-handler.api ClojureScript namespace

##### [README](../../../README.md) > [DOCUMENTATION](../../COVER.md) > keypress-handler.api

### Index

- [get-pressed-keys](#get-pressed-keys)

- [key-pressed?](#key-pressed)

- [quit-type-mode!](#quit-type-mode)

- [reg-keypress-event!](#reg-keypress-event)

- [remove-keypress-event!](#remove-keypress-event)

- [set-type-mode!](#set-type-mode)

### get-pressed-keys

```
@usage
(get-pressed-keys?)
```

```
@return (integers in vector)
```

<details>
<summary>Source code</summary>

```
(defn get-pressed-keys
  []
  (keys @state/PRESSED-KEYS))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [keypress-handler.api :refer [get-pressed-keys]]))

(keypress-handler.api/get-pressed-keys)
(get-pressed-keys)
```

</details>

---

### key-pressed?

```
@param (integer) key-code
```

```
@usage
(key-pressed?)
```

```
@return (boolean)
```

<details>
<summary>Source code</summary>

```
(defn key-pressed?
  [key-code]
  (get @state/PRESSED-KEYS key-code))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [keypress-handler.api :refer [key-pressed?]]))

(keypress-handler.api/key-pressed? ...)
(key-pressed?                      ...)
```

</details>

---

### quit-type-mode!

```
@usage
(quit-type-mode!)
```

<details>
<summary>Source code</summary>

```
(defn quit-type-mode!
  []
  (reset! state/TYPE-MODE? false))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [keypress-handler.api :refer [quit-type-mode!]]))

(keypress-handler.api/quit-type-mode!)
(quit-type-mode!)
```

</details>

---

### reg-keypress-event!

```
@param (keyword)(opt) event-id
@param (map) event-props
{:exclusive? (boolean)(opt)
  If true, other (previously registered) keypress events with the same
  key-code will be ignored until the exclusive one removed.
  If more than one exclusive event registered with the same key-code, the
  last registered will be the exclusive one.
  Default: false
 :key-code (integer)
 :on-keydown (function)(opt)
 :on-keyup (function)(opt)
 :prevent-default? (boolean)(opt)
  Default: false
 :required? (boolean)(opt)
  If true, the event won't be ignored when the type mode is on.
  Default: false}
```

```
@usage
(reg-keypress-event! {...})
```

```
@usage
(reg-keypress-event! :my-keypress-event {...})
```

```
@usage
(reg-keypress-event! {:key-code 65 :on-keydown (fn [key-code] ...)})
```

<details>
<summary>Source code</summary>

```
(defn reg-keypress-event!
  ([event-props]
   (reg-keypress-event! (random/generate-keyword) event-props))

  ([event-id {:keys [exclusive? key-code prevent-default?] :as event-props}]
   (if prevent-default? (prevent-keypress-default! key-code))
   (if exclusive?       (set-exclusivity! event-id event-props))
   (if-let [no-exclusive-set? (-> @state/EXCLUSIVE-EVENTS (get key-code) empty?)]
           (cache-event! event-id event-props)
           (if exclusive? (cache-event! event-id event-props)))
   (store-event-props! event-id event-props)))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [keypress-handler.api :refer [reg-keypress-event!]]))

(keypress-handler.api/reg-keypress-event! ...)
(reg-keypress-event!                      ...)
```

</details>

---

### remove-keypress-event!

```
@param (keyword) event-id
```

```
@usage
(remove-keypress-event! :my-event)
```

<details>
<summary>Source code</summary>

```
(defn remove-keypress-event!
  [event-id]
  (if (env/enable-default? event-id)
      (let [key-code (get-in @state/KEYPRESS-EVENTS [event-id :key-code])]
           (enable-keypress-default! key-code)))
  (if-let [exclusive? (get-in @state/KEYPRESS-EVENTS [event-id :exclusive?])]
          (unset-exclusivity! event-id))
  (uncache-event!      event-id)
  (remove-event-props! event-id))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [keypress-handler.api :refer [remove-keypress-event!]]))

(keypress-handler.api/remove-keypress-event! ...)
(remove-keypress-event!                      ...)
```

</details>

---

### set-type-mode!

```
@usage
(set-type-mode!)
```

<details>
<summary>Source code</summary>

```
(defn set-type-mode!
  []
  (reset! state/TYPE-MODE? true))
```

</details>

<details>
<summary>Require</summary>

```
(ns my-namespace (:require [keypress-handler.api :refer [set-type-mode!]]))

(keypress-handler.api/set-type-mode!)
(set-type-mode!)
```

</details>

---

This documentation is generated with the [clj-docs-generator](https://github.com/bithandshake/clj-docs-generator) engine.

