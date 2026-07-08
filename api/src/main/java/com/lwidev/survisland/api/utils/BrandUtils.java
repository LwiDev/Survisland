package com.lwidev.survisland.api.utils;

import net.kyori.adventure.text.format.TextColor;

/**
 * Survisland's brand palette, hex-exact so it renders identically everywhere Adventure components are
 * used (menus, chat messages, ...) instead of falling back to the nearest of the 16 legacy named colors.
 * Pick by role, not by taste: {@link #PRIMARY} for a screen's top-level/most prominent elements (titles,
 * main actions), {@link #SECONDARY} for regular navigation/actions, {@link #TERTIARY} for utility/admin
 * actions performed on a target (gamemode, teleport, effects...). Semantic colors (green/red for
 * confirm-cancel, enabled-disabled, success-error) are intentionally kept separate from this brand triad.
 */
public final class BrandUtils {

    /** Survisland orange — the brand's base color. */
    public static final TextColor PRIMARY = TextColor.color(0xFF8C1A);

    /** Warm golden yellow, analogous to {@link #PRIMARY} on the color wheel. */
    public static final TextColor SECONDARY = TextColor.color(0xFFC94D);

    /** Azure blue, {@link #PRIMARY}'s complement (opposite hue) on the color wheel. */
    public static final TextColor TERTIARY = TextColor.color(0x1BA8E0);

}
