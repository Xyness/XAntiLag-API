package fr.xyness.XAntiLag.API;

import java.util.UUID;

import org.bukkit.entity.Entity;

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

    /**
     * Marks an entity as one no automatic removal may take.
     *
     * <p>For a plugin that hands a player something it means them to keep: a companion, a mount, a
     * quest mob, an event decoration. XAntiLag already spares tamed animals, named mobs, mounts and
     * anything flagged as never-despawning — but a pet plugin that gives somebody a zombie gives
     * them a mob none of those tests recognise, and this settles it once and for all.</p>
     *
     * <p>The mark is persistent: it survives a restart, and it is honoured by clearlag, by the chunk
     * limiter and by both stackers.</p>
     *
     * @param entity The entity to protect.
     */
    void protect(Entity entity);

    /**
     * Removes the mark set by {@link #protect(Entity)}.
     *
     * @param entity The entity.
     */
    void unprotect(Entity entity);

    /**
     * Whether an entity is off-limits to every automatic removal — through {@link #protect(Entity)}
     * or through any of the rules XAntiLag applies on its own.
     *
     * @param entity The entity.
     * @return {@code true} when nothing will remove it.
     */
    boolean isProtected(Entity entity);

    /**
     * How many mobs an entity stands for.
     *
     * <p>The mob stacker keeps identical mobs as one entity carrying a count. Anything that hands
     * out a reward per mob — a quest, a job, a bounty — can ask here how many there are; see also
     * {@link MobStackKillEvent}, which says how many died.</p>
     *
     * <p>Unlike the rest of this interface, to be called from the thread that owns the entity.</p>
     *
     * @param entity The entity.
     * @return The number of mobs, {@code 1} for anything that is not a stack.
     */
    int getStackSize(Entity entity);

    /**
     * Whether an entity stands for more than one mob.
     *
     * @param entity The entity.
     * @return {@code true} for a stack of two or more.
     */
    default boolean isStacked(Entity entity) {
        return getStackSize(entity) > 1;
    }

    /**
     * How many items a ground item stands for.
     *
     * <p>The item stacker keeps a pile as one item entity showing at most one vanilla stack, with
     * its real amount stored beside it. A plugin that reads the entity's own stack sees what it
     * shows; this is what it holds.</p>
     *
     * <p>To be called from the thread that owns the item.</p>
     *
     * @param item The ground item.
     * @return The real amount; the amount of its own stack for an item the stacker never touched.
     */
    int getStackedAmount(org.bukkit.entity.Item item);
}
