package fr.xyness.XAntiLag.API;

import java.util.UUID;

/**
 * What XAntiLag tells the rest of the server about its load.
 *
 * <p>Every anti-lag measure in one plugin fights the same problem alone. The plugins that actually
 * generate the load — cosmetics spraying particles, animated menus redrawing on a timer, spawners
 * ticking — have no idea the server is struggling, so they keep going exactly when they should not.
 * This interface, and {@link PerformanceLevelChangeEvent}, are the whole point: read the state, or
 * react to the moment it changes, and moderate yourself.</p>
 *
 * <pre>{@code
 * if (XAntiLagProvider.isRegistered() && XAntiLagProvider.get().isUnderLoad()) {
 *     // skip the particle burst this tick
 * }
 * }</pre>
 *
 * <p>Obtained through {@link XAntiLagProvider}. Every method is safe to call from any thread.</p>
 */
public interface XAntiLagAPI {

    /**
     * The smoothed tick rate.
     *
     * @return Ticks per second, at most 20.
     */
    double currentTps();

    /**
     * The smoothed tick duration.
     *
     * @return Milliseconds per tick.
     */
    double currentMspt();

    /**
     * The performance level currently applied.
     *
     * <p>Levels are ordered by severity, as configured: 1 is the first threshold crossed, higher
     * numbers mean the server is further behind.</p>
     *
     * @return The level number, or {@code 0} when the server is healthy.
     */
    int activeLevel();

    /**
     * Whether any performance level is active.
     *
     * @return {@code true} when the server is being helped along.
     */
    boolean isUnderLoad();

    /**
     * Percentage of natural mob spawns currently allowed.
     *
     * @return 0 to 100, where 100 means vanilla.
     */
    int spawnRate();

    /**
     * Whether a player is currently away.
     *
     * <p>Detection only credits real movement, so this is not "has not typed in a while": vehicles,
     * camera movement and confined loops do not keep a player active.</p>
     *
     * @param player The player's UUID.
     * @return {@code true} when they are marked AFK.
     */
    boolean isAfk(UUID player);

    /**
     * How many players are currently away.
     *
     * @return The count.
     */
    int afkCount();
}
