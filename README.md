<p align="center">
  <img src="https://celestis.dev/banners/xantilag.png" alt="XAntiLag">
</p>

<p align="center">
  <b>The public API of XAntiLag: let your plugin read the server's load, or hear the moment it changes,<br>and ease off until the server recovers.</b>
</p>

<p align="center">
  <a href="https://jitpack.io/#Xyness/XAntiLag-API"><img src="https://img.shields.io/badge/version-1.2.0-6fb36f?style=flat-square&labelColor=24292f" alt="Version 1.2.0"></a>
  <a href="https://builtbybit.com/resources/xantilag.105658/"><img src="https://img.shields.io/badge/ships%20with-XAntiLag%202.1-57606a?style=flat-square&labelColor=24292f" alt="Ships with XAntiLag 2.1"></a>
  <img src="https://img.shields.io/badge/Paper-1.21.1%2B-57606a?style=flat-square&labelColor=24292f" alt="Paper 1.21.1+">
  <img src="https://img.shields.io/badge/Folia-supported-57606a?style=flat-square&labelColor=24292f" alt="Folia supported">
  <img src="https://img.shields.io/badge/Java-21%2B-57606a?style=flat-square&labelColor=24292f" alt="Java 21+">
</p>

<p align="center">
  <a href="#add-it-to-your-build"><b>Add it to your build</b></a>
  &nbsp;&middot;&nbsp;
  <a href="https://celestis.dev/plugins/xantilag/wiki/monitoring#api">Documentation</a>
  &nbsp;&middot;&nbsp;
  <a href="https://builtbybit.com/resources/xantilag.105658/">XAntiLag</a>
  &nbsp;&middot;&nbsp;
  <a href="https://discord.com/invite/6sRTGprM95">Discord</a>
</p>

<br>

Every anti-lag measure in one plugin fights the same problem alone. The plugins that actually make the load (cosmetics spraying particles, animated menus redrawing on a timer, spawners ticking) have no idea the server is struggling, so they keep going exactly when they should not. This API is how they find out.

[XAntiLag](https://builtbybit.com/resources/xantilag.105658/) watches TPS and MSPT and walks the server through its performance levels. Your plugin asks where things stand, or listens for the moment they change, and holds back while the server is under load. The API is four classes: an interface, the accessor that hands it to you, and two events. XAntiLag provides the implementation at runtime. Since 1.2.0 it also answers for the mob and item stackers: how many mobs an entity stands for, and how many of them just died.

<br>

## Add it to your build

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    compileOnly 'com.github.Xyness:XAntiLag-API:1.2.0'
}
```

<details>
<summary>Maven</summary>

<br>

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.Xyness</groupId>
        <artifactId>XAntiLag-API</artifactId>
        <version>1.2.0</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

</details>

> [!IMPORTANT]
> `compileOnly` only, never shaded. The classes come from XAntiLag at runtime. Shading them gives you a different class with the same name, and a listener registered on it never fires.

Since 2.0, XAntiLag is a Paper plugin of its own, so your plugin declares it to see those classes. In a `paper-plugin.yml`, under `dependencies.server`, with `join-classpath: true`:

```yaml
dependencies:
  server:
    XAntiLag:
      load: BEFORE
      required: false
      join-classpath: true
```

In a `plugin.yml`, `depend: [XAntiLag]` does the same. An XCore addon needs no declaration: XCore finds the classes among the server's plugins.

The API version is `1.2.0`, the one XAntiLag 2.1 ships with. Sources and Javadoc are published with the jar. What 1.2.0 adds (the stack readers and `MobStackKillEvent`) needs XAntiLag 2.1 at runtime; everything else works on 2.0 as well.

<br>

## Read the state

```java
if (XAntiLagProvider.isRegistered() && XAntiLagProvider.get().isUnderLoad()) {
    return; // skip the particle burst this tick
}
```

| Method | Returns |
|---|---|
| `currentTps()` | Smoothed ticks per second, 20 at most |
| `currentMspt()` | Smoothed milliseconds per tick |
| `activeLevel()` | `0` when healthy, `1..n` per configured performance level, higher meaning further behind |
| `isUnderLoad()` | Whether any level is applied |
| `spawnRate()` | Percentage of natural mob spawns allowed, 100 being vanilla |
| `isAfk(UUID)` | AFK state, based on movement |
| `afkCount()` | Players currently away |
| `isProtected(Entity)` | Whether the entity is exempt from every automatic removal |
| `getStackSize(Entity)` | How many mobs the entity stands for, `1` for anything that is not a stack |
| `isStacked(Entity)` | Whether it stands for two or more |
| `getStackedAmount(Item)` | How many items a ground item really holds |

Every method is safe to call from any thread, except the three stack readers, which read the entity and belong on the thread that owns it. Ask `isRegistered()` first: XAntiLag is optional, and `get()` throws while it is absent or not enabled yet.

AFK detection only credits real movement, so `isAfk` does not mean "has not typed in a while": riding a vehicle, turning the camera or walking a confined loop does not keep a player active.

<br>

## React when it changes

```java
@EventHandler
public void onLoad(PerformanceLevelChangeEvent event) {
    particlesEnabled = event.isRecovery();

    if (event.isEscalation() && event.getNewLevel() >= 2) {
        pauseHeavyTask();
    }
}
```

`PerformanceLevelChangeEvent` is fired when a level activates, when the server moves between levels and when it recovers. It carries `getPreviousLevel()`, `getNewLevel()`, `getTps()`, `getMspt()`, `isRecovery()` and `isEscalation()`. It is fired on the server thread and cannot be cancelled: it reports a decision already applied.

> [!TIP]
> Polling `isUnderLoad()` every tick works. A plugin that only needs to know when the state changes (stop the particle bursts, halve a scheduler's rate, pause a queue) should listen here instead, and pay nothing in between.

<br>

## Count the mobs of a stack

XAntiLag keeps identical mobs as one entity carrying a count. When that entity dies the server fires a single `EntityDeathEvent`, however many mobs went with it, so a plugin that counts kills from that event counts one.

```java
@EventHandler
public void onStackKill(MobStackKillEvent event) {
    if (event.getKiller() == null) return;
    // one of them was already counted from EntityDeathEvent
    addKills(event.getKiller(), event.getEntity().getType(), event.getAmount() - 1);
}
```

`MobStackKillEvent` is fired after the `EntityDeathEvent` of the entity, on the thread that owns it, and only when more than one mob died: `getAmount()` is always 2 or more, and `getKiller()` is `null` when nobody was credited. It cannot be cancelled.

Outside a death, `getStackSize(entity)` tells you how many mobs stand behind an entity, for anything that pays or counts per mob.

> [!NOTE]
> The game's own statistics (`KILL_ENTITY`, `MOB_KILLS`) are brought up to the real count by XAntiLag itself. A plugin that reads them has nothing to do.

<br>

## Protect an entity

```java
XAntiLagProvider.get().protect(pet);
XAntiLagProvider.get().unprotect(pet);
```

For something your plugin hands a player and means them to keep: a companion, a mount, a quest mob, an event decoration. The mark exempts the entity from clearlag, the chunk limiter and both stackers, and it survives a restart.

Tamed, named and leashed mobs, mounts and their riders, mobs released from a bucket, villagers with a profession or trades, filled item frames, armour stands wearing or holding something, minecarts and boats with a load, and mobs flagged to never despawn are already exempt. A pet plugin that gives somebody a zombie gives them a mob none of those rules recognise, and `protect` settles it. On the server side, [`clearlag.protected-pdc-keys`](https://celestis.dev/plugins/xantilag/wiki/anti-abuse#clearlag) does the same from the config.

<br>

## Requirements

- Java 21+
- Paper 1.21.1+ or Folia
- XAntiLag 2.1 and XCore 2.0.1 installed at runtime

## License

Free to use in any plugin, commercial ones included.

<p align="center">
  <sub>XAntiLag is part of the XCore family, with <a href="https://celestis.dev/plugins/xbans">XBans</a>, <a href="https://celestis.dev/plugins/xlogin">XLogin</a>, <a href="https://celestis.dev/plugins/xauctionhouse">XAuctionHouse</a> and <a href="https://celestis.dev/plugins/xshop">XShop</a>, all built on <a href="https://github.com/Xyness/XCore">XCore</a>. Made by <a href="https://github.com/Xyness">Xyness</a>.</sub>
</p>
