# XAntiLag-API

Public API for [XAntiLag](https://celestis.dev/), the optimization addon for XCore. Lets a plugin
read the server's load state and react to it. Interfaces and events only; the addon provides the
implementation.

## Installation

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    compileOnly 'com.github.Xyness:XAntiLag-API:1.1.1'
}
```

`compileOnly` only. The classes come from XAntiLag at runtime and XCore resolves them across addons.
Shading them gives you a different class with the same name, and a listener registered on it never
fires.

The API version follows the addon: `1.1.1` ships with XAntiLag 1.1.1.

## Reading the state

```java
if (XAntiLagProvider.isRegistered() && XAntiLagProvider.get().isUnderLoad()) {
    return;
}
```

| Method | Returns |
|--------|---------|
| `currentTps()` | Smoothed ticks per second |
| `currentMspt()` | Smoothed milliseconds per tick |
| `activeLevel()` | `0` when healthy, `1..n` per configured performance level |
| `isUnderLoad()` | Whether any level is applied |
| `spawnRate()` | Percentage of natural mob spawns allowed |
| `isAfk(UUID)` | AFK state, based on movement |
| `afkCount()` | Players currently away |
| `isProtected(Entity)` | Whether the entity is exempt from removal |

Thread-safe. Guard with `isRegistered()`; XAntiLag is optional.

## Protecting an entity

```java
XAntiLagProvider.get().protect(pet);
XAntiLagProvider.get().unprotect(pet);
```

Marks an entity as exempt from clearlag, the chunk limiter and both stackers. The mark survives a
restart. Tamed animals, mounts and their riders, named mobs and mobs flagged to never despawn are
already exempt. `clearlag.protected-pdc-keys` does the same from the config side.

## Reacting to changes

```java
@EventHandler
public void onLoad(PerformanceLevelChangeEvent event) {
    particlesEnabled = event.isRecovery();

    if (event.isEscalation() && event.getNewLevel() >= 2) {
        pauseHeavyTask();
    }
}
```

Fired when a level activates, when the server moves between levels and when it recovers. Carries
`getPreviousLevel()`, `getNewLevel()`, `getTps()`, `getMspt()`, `isRecovery()` and `isEscalation()`.
Fired on the server thread, not cancellable.

## Requirements

- Java 21+
- Paper 1.21.1+ or Folia
- XCore, with XAntiLag installed at runtime

## License

Free to use in any plugin, commercial ones included.
