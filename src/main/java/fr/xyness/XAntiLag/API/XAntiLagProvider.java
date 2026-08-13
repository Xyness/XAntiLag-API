package fr.xyness.XAntiLag.API;

/**
 * Static accessor for the XAntiLag API.
 *
 * <p>Registered by the addon when it enables, dropped when it disables. Callers that are not sure
 * XAntiLag is installed should ask {@link #isRegistered()} first — the addon is optional, and a
 * plugin that hard-depends on it stops working the day an administrator removes it.</p>
 */
public final class XAntiLagProvider {

    /**
     * The live implementation.
     *
     * <p>{@code volatile} because registration happens once on the server thread while readers call
     * from wherever they happen to run — an async particle task, a region thread. Without it the JLS
     * offers no guarantee they would ever see the write.</p>
     */
    private static volatile XAntiLagAPI api;

    private XAntiLagProvider() {}

    /**
     * The API.
     *
     * @return The registered implementation.
     * @throws IllegalStateException When XAntiLag is absent or not enabled yet.
     */
    public static XAntiLagAPI get() {
        XAntiLagAPI current = api;
        if (current == null) {
            throw new IllegalStateException("XAntiLag API is not loaded yet!");
        }
        return current;
    }

    /**
     * Whether the API is available.
     *
     * @return {@code true} when {@link #get()} will answer.
     */
    public static boolean isRegistered() {
        return api != null;
    }

    /**
     * Registers the implementation. Called by XAntiLag itself.
     *
     * @param instance The implementation.
     */
    public static void register(XAntiLagAPI instance) {
        api = instance;
    }

    /** Drops the implementation. Called by XAntiLag when it disables. */
    public static void unregister() {
        api = null;
    }
}
