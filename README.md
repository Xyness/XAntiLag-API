# XAntiLag-API

Public API for [XAntiLag](https://celestis.dev/), the all-in-one optimization addon for XCore.

Every anti-lag measure in one plugin fights the same problem alone. The plugins that actually
generate the load — cosmetics spraying particles, animated menus redrawing on a timer, spawners
ticking, schedulers hammering — have no idea the server is struggling, so they keep going exactly
when they should not. This is how they find out.

Interfaces and events only. Nothing here implements anything: the addon does.

---

## Installation

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    compileOnly 'com.github.Xyness:XAntiLag-API:1.1.0'
}
```

`compileOnly` on purpose — the classes are provided at runtime by XAntiLag itself, and XCore resolves
them across addons. Shading them into your own jar would give you a *different* class of the same
name, and an event listener registered on it would simply never fire.

The API version follows the addon's: `1.1.0` is the API shipped by XAntiLag 1.1.0.

---

## Reading the state

```java
if (XAntiLagProvider.isRegistered() && XAntiLagProvider.get().isUnderLoad()) {
    return; // skip the particle burst this tick
}
```

| Method | Answers |
|--------|---------|
| `currentTps()` | Smoothed ticks per second |
| `currentMspt()` | Smoothed milliseconds per tick |
| `activeLevel()` | `0` when healthy, `1..n` as configured performance levels trigger |
| `isUnderLoad()` | Whether any level is applied |
| `spawnRate()` | Percentage of natural mob spawns currently allowed |
| `isAfk(UUID)` | AFK state — real-movement based, not "has not typed in a while" |
| `afkCount()` | How many players are away |

Every method is safe to call from any thread. Always guard with `isRegistered()`: XAntiLag is
optional, and a plugin that hard-depends on it breaks the day an administrator removes it.

---

## Reacting to changes

Polling works. Listening costs nothing in between:

```java
@EventHandler
public void onLoad(PerformanceLevelChangeEvent event) {
    particlesEnabled = event.isRecovery();

    if (event.isEscalation() && event.getNewLevel() >= 2) {
        pauseHeavyTask();
    }
}
```

`PerformanceLevelChangeEvent` fires the moment a level activates, the moment the server moves
between levels, and the moment it recovers. It carries `getPreviousLevel()`, `getNewLevel()`,
`getTps()`, `getMspt()`, plus `isRecovery()` and `isEscalation()`. Fired on the server thread, never
cancellable: it reports a decision already applied.

---

## Requirements

- Java 21+
- Paper 1.21.1+ (or Folia)
- XCore, with XAntiLag installed on the server at runtime

## License

Free to use in any plugin, including commercial ones.
