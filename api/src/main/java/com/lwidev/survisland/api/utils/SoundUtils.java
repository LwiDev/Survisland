package com.lwidev.survisland.api.utils;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/** Centralized sounds by intent (not by feature), so any event needing the same cue reuses it. */
public class SoundUtils {

    /** Something needs the player's attention now (an announcement, a timer starting, ...). */
    private static final Sound NOTIFY = Sound.BLOCK_BELL_USE;
    /** Something just finished (a timer ending, ...). */
    private static final Sound COMPLETE = Sound.ENTITY_PLAYER_LEVELUP;

    public static void playNotify(Player player) {
        play(player, NOTIFY);
    }

    /** Same cue, to every online player. */
    public static void playNotify() {
        Bukkit.getOnlinePlayers().forEach(SoundUtils::playNotify);
    }

    public static void playComplete(Player player) {
        play(player, COMPLETE);
    }

    /** Same cue, to every online player. */
    public static void playComplete() {
        Bukkit.getOnlinePlayers().forEach(SoundUtils::playComplete);
    }

    public static void play(Player player, Sound sound) {
        player.playSound(player.getLocation(), sound, 5f, 1f);
    }

    /**
     * Resolves a sound from an admin-configured name in config.yml — the vanilla namespaced key, same
     * as used by {@code /playsound} (e.g. "entity.player.levelup", "block.note_block.bit" — note the
     * underscore there is part of the sound's own name, not a word separator to convert). Returns
     * {@code null} if it doesn't match a known sound.
     */
    public static Sound fromConfigName(String name) {
        return Registry.SOUNDS.get(NamespacedKey.minecraft(name.toLowerCase()));
    }
}
