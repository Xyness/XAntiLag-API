package fr.xyness.XAntiLag.API;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired when several mobs of a stack die at once.
 *
 * <p>XAntiLag keeps identical mobs as one entity carrying a count. When that entity dies, the server
 * fires a single {@code EntityDeathEvent}, however many mobs went with it — so a plugin that counts
 * kills from that event counts one. This event says how many there really were.</p>
 *
 * <pre>{@code
 * @EventHandler
 * public void onStackKill(MobStackKillEvent event) {
 *     if (event.getKiller() == null) return;
 *     // one of them was already counted from EntityDeathEvent
 *     addKills(event.getKiller(), event.getEntity().getType(), event.getAmount() - 1);
 * }
 * }</pre>
 *
 * <p>Fired after the {@code EntityDeathEvent} of the entity has been through every listener, on the
 * thread that owns the entity, and only when more than one mob died: the death of a single mob is
 * an ordinary death and raises nothing here. Not cancellable — it reports what already happened.
 * The game's own statistics ({@code KILL_ENTITY}, {@code MOB_KILLS}) are brought up to the real
 * count by XAntiLag itself.</p>
 */
public class MobStackKillEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final LivingEntity entity;
    private final Player killer;
    private final int amount;

    /**
     * @param entity The entity that died, standing for the mobs killed.
     * @param killer The player credited with the kill, or {@code null}.
     * @param amount How many mobs died with it, at least 2.
     */
    public MobStackKillEvent(LivingEntity entity, Player killer, int amount) {
        this.entity = entity;
        this.killer = killer;
        this.amount = amount;
    }

    /** @return The entity that died. Its type is the type of every mob counted here. */
    public LivingEntity getEntity() { return entity; }

    /** @return The player credited with the kill, or {@code null} when nobody was. */
    public Player getKiller() { return killer; }

    /** @return How many mobs died, the one the server already announced included. Always 2 or more. */
    public int getAmount() { return amount; }

    @Override
    public HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }
}
