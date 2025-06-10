package org.vg.markusbro.core.service.plugins;

public interface Plugin {

    /**
     * Means this plugin should never be applied
     */
    double SCORE_NEVER = 0.0;
    /**
     * Means this plugin could be applied as a last resort
     */
    double SCORE_LAST_RESORT = 0.0001;
    /**
     * Means this plugin should be applied for sure
     */
    double SCORE_ALWAYS = 1.0;

    /**
     * Get plugin id
     *
     * @return Plugin id
     */
    String getId();

    /**
     * Get confidence score for a given message
     *
     * @param context Message context
     * @return Confidence score, 0..1 (both inclusive)
     */
    double getScore(Context context);

    /**
     * Handle incoming message
     *
     * @param context Message context
     */
    void handle(Context context);
}
