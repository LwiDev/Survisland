package com.lwidev.survisland.api.utils;

/** French singular/plural agreement, so nobody has to hand-roll "(s)" suffixes at each call site. */
public final class PluralUtils {

    private PluralUtils() {
    }

    /** The singular or plural form of a word, agreeing with {@code count} (plural for every count but 1 and -1). */
    public static String agree(long count, String singular, String plural) {
        return Math.abs(count) == 1 ? singular : plural;
    }

    /** Same as {@link #agree(long, String, String)}, assuming a regular plural (just add an "s"). */
    public static String agree(long count, String singular) {
        return agree(count, singular, singular + "s");
    }

    /** {@code count} followed by its agreeing word, e.g. {@code withCount(1, "minute")} → "1 minute", {@code withCount(5, "minute")} → "5 minutes". */
    public static String withCount(long count, String singular, String plural) {
        return count + " " + agree(count, singular, plural);
    }

    /** Same as {@link #withCount(long, String, String)}, assuming a regular plural (just add an "s"). */
    public static String withCount(long count, String singular) {
        return withCount(count, singular, singular + "s");
    }
}
