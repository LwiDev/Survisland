package com.lwidev.survisland.game;

import org.bukkit.World;

/** Wraps the world time-of-day controls exposed by the /menu game page. */
public final class GameClockService {

    private static final long DAY_LENGTH = 24000L;
    private static final long DAWN = 1000L;
    private static final long NIGHT = 13000L;

    public static void setDay(World world) {
        world.setTime(DAWN);
    }

    public static void setNight(World world) {
        world.setTime(NIGHT);
    }

    public static void skipToNextDay(World world) {
        long time = world.getFullTime();
        world.setFullTime(time - (time % DAY_LENGTH) + DAY_LENGTH + DAWN);
    }

    public static void addTime(World world, long ticks) {
        world.setFullTime(world.getFullTime() + ticks);
    }
}
