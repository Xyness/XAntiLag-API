package fr.xyness.XAntiLag.API;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired when XAntiLag activates a performance level, moves between levels, or recovers.
 *
 * <p>The moment to turn something off, and the moment to turn it back on. Polling
 * {@link XAntiLagAPI#isUnderLoad()} every tick works, but a plugin that only needs to know when the
 * state <i>changes</i> — stop the particle bursts, halve a scheduler's rate, pause a queue — should
 * listen here instead and pay nothing in between.</p>
 *
 * <pre>{@code
 * @EventHandler
 * public void onLoad(PerformanceLevelChangeEvent event) {
 *     particlesEnabled = event.isRecovery();
 * }
 * }</pre>
 *
 * <p>Fired on the server thread, never cancellable: it reports a decision already applied.</p>
 */
public class PerformanceLevelChangeEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final int previousLevel;
    private final int newLevel;
    private final double tps;
    private final double mspt;

    /**
     * @param previousLevel The level that was active, 0 when none.
     * @param newLevel      The level now active, 0 when the server recovered.
     * @param tps           The tick rate that triggered the change.
     * @param mspt          The tick duration that triggered the change.
     */
    public PerformanceLevelChangeEvent(int previousLevel, int newLevel, double tps, double mspt) {
        this.previousLevel = previousLevel;
        this.newLevel = newLevel;
        this.tps = tps;
        this.mspt = mspt;
    }

    /** @return The level that was active before this change, {@code 0} when none was. */
    public int getPreviousLevel() { return previousLevel; }

    /** @return The level now active, {@code 0} when the server recovered. */
    public int getNewLevel() { return newLevel; }

    /** @return The smoothed tick rate at the moment of the change. */
    public double getTps() { return tps; }

    /** @return The smoothed tick duration at the moment of the change. */
    public double getMspt() { return mspt; }

    /** @return {@code true} when the server just came back to health. */
    public boolean isRecovery() { return newLevel == 0; }

    /** @return {@code true} when the server just got worse, not better. */
    public boolean isEscalation() { return newLevel > previousLevel; }

    @Override
    public HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }
}
