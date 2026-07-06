package com.lwidev.survisland.utils;

import com.lwidev.survisland.Survisland;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PauseManager {

    private static boolean isPaused = false;
    private static BukkitTask titleTask = null;
    private static final Set<UUID> frozenPlayers = new HashSet<>();
    private static final NamespacedKey FREEZE_MODIFIER_KEY = new NamespacedKey("survisland", "pause_freeze");

    public static void toggle(Survisland plugin) {
        if (isPaused) {
            unpause(plugin);
        } else {
            pause(plugin);
        }
    }

    public static void pause(Survisland plugin) {
        if (isPaused) {
            return;
        }

        isPaused = true;

        // Geler tous les joueurs en survie/aventure
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
                applyFreezeAttributes(player);
                frozenPlayers.add(player.getUniqueId());
            }
        }

        // Jouer le son de mise en pause (pitch descendant)
        playSoundSequence(plugin, false);

        // Démarrer la tâche d'affichage du titre
        titleTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isPaused) {
                    this.cancel();
                    return;
                }

                showPauseTitle();
            }
        }.runTaskTimer(plugin, 0L, 20L); // Toutes les 20 ticks (1 seconde)
    }

    public static void unpause(Survisland plugin) {
        if (!isPaused) {
            return;
        }

        isPaused = false;

        // Arrêter la tâche d'affichage du titre
        if (titleTask != null && !titleTask.isCancelled()) {
            titleTask.cancel();
            titleTask = null;
        }

        // Dégeler uniquement les joueurs qui ont été gelés
        frozenPlayers.stream().map(Bukkit::getPlayer).filter(player -> player != null && player.isOnline()).forEach(player -> {
            removeFreezeAttributes(player);
            player.clearTitle();
            player.sendActionBar(Component.text("Le jeu reprend !", NamedTextColor.GREEN));
        });
        frozenPlayers.clear();

        // Jouer le son de reprise (pitch montant)
        playSoundSequence(plugin, true);
    }

    private static void showPauseTitle() {
        Component pauseTitle = Component.text("PAUSE").color(NamedTextColor.RED).decorate(TextDecoration.BOLD);
        Title title = Title.title(pauseTitle, Component.empty(), Title.Times.times(Duration.ZERO, Duration.ofMillis(1500), Duration.ZERO));
        frozenPlayers.stream().map(Bukkit::getPlayer).filter(player -> player != null && player.isOnline()).filter(player -> player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE).forEach(player -> player.showTitle(title));
    }

    private static void playSoundSequence(Survisland plugin, boolean ascending) {
        float[] pitches;

        if (ascending) {
            // Montée : 0.5 -> 2.0
            pitches = new float[]{0.5f, 0.875f, 1.25f, 1.625f, 2.0f};
        } else {
            // Descente : 2.0 -> 0.5
            pitches = new float[]{2.0f, 1.625f, 1.25f, 0.875f, 0.5f};
        }

        new BukkitRunnable() {
            int index = 0;

            @Override
            public void run() {
                if (index >= pitches.length) {
                    this.cancel();
                    return;
                }

                // Jouer le son pour tous les joueurs
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, pitches[index]);
                }

                index++;
            }
        }.runTaskTimer(plugin, 0L, 2L); // Toutes les 2 ticks (0.1 seconde)
    }

    public static boolean isPaused() {
        return isPaused;
    }

    public static boolean isFrozen(Player player) {
        return frozenPlayers.contains(player.getUniqueId());
    }

    public static void freezePlayer(Player player) {
        if (isPaused && (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE)) {
            applyFreezeAttributes(player);
            frozenPlayers.add(player.getUniqueId());
            showPauseTitle();
        }
    }

    private static void applyFreezeAttributes(Player player) {
        AttributeModifier freezeModifier = new AttributeModifier(
                FREEZE_MODIFIER_KEY,
                -1.0,
                AttributeModifier.Operation.MULTIPLY_SCALAR_1
        );

        // Bloquer le mouvement
        AttributeInstance movementSpeed = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (movementSpeed != null) {
            movementSpeed.getModifiers().stream()
                    .filter(mod -> mod.getKey().equals(FREEZE_MODIFIER_KEY))
                    .findFirst()
                    .ifPresent(movementSpeed::removeModifier);
            movementSpeed.addModifier(freezeModifier);
        }

        // Bloquer le saut
        AttributeInstance jumpStrength = player.getAttribute(Attribute.JUMP_STRENGTH);
        if (jumpStrength != null) {
            jumpStrength.getModifiers().stream()
                    .filter(mod -> mod.getKey().equals(FREEZE_MODIFIER_KEY))
                    .findFirst()
                    .ifPresent(jumpStrength::removeModifier);
            jumpStrength.addModifier(freezeModifier);
        }

        // Bloquer le minage (comme mining fatigue extrême)
        AttributeInstance blockBreakSpeed = player.getAttribute(Attribute.BLOCK_BREAK_SPEED);
        if (blockBreakSpeed != null) {
            blockBreakSpeed.getModifiers().stream()
                    .filter(mod -> mod.getKey().equals(FREEZE_MODIFIER_KEY))
                    .findFirst()
                    .ifPresent(blockBreakSpeed::removeModifier);
            blockBreakSpeed.addModifier(freezeModifier);
        }

        // Bloquer la vitesse d'attaque (figer les animations de clic gauche)
        AttributeInstance attackSpeed = player.getAttribute(Attribute.ATTACK_SPEED);
        if (attackSpeed != null) {
            attackSpeed.getModifiers().stream()
                    .filter(mod -> mod.getKey().equals(FREEZE_MODIFIER_KEY))
                    .findFirst()
                    .ifPresent(attackSpeed::removeModifier);
            attackSpeed.addModifier(freezeModifier);
        }

        // Geler aussi la monture si le joueur en a une
        if (player.getVehicle() != null && player.getVehicle() instanceof org.bukkit.entity.LivingEntity mount) {
            AttributeInstance mountSpeed = mount.getAttribute(Attribute.MOVEMENT_SPEED);
            if (mountSpeed != null) {
                mountSpeed.getModifiers().stream()
                        .filter(mod -> mod.getKey().equals(FREEZE_MODIFIER_KEY))
                        .findFirst()
                        .ifPresent(mountSpeed::removeModifier);
                mountSpeed.addModifier(freezeModifier);
            }

            AttributeInstance mountJump = mount.getAttribute(Attribute.JUMP_STRENGTH);
            if (mountJump != null) {
                mountJump.getModifiers().stream()
                        .filter(mod -> mod.getKey().equals(FREEZE_MODIFIER_KEY))
                        .findFirst()
                        .ifPresent(mountJump::removeModifier);
                mountJump.addModifier(freezeModifier);
            }
        }
    }

    private static void removeFreezeAttributes(Player player) {
        // Débloquer le mouvement
        AttributeInstance movementSpeed = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (movementSpeed != null) {
            movementSpeed.getModifiers().stream()
                    .filter(mod -> mod.getKey().equals(FREEZE_MODIFIER_KEY))
                    .findFirst()
                    .ifPresent(movementSpeed::removeModifier);
        }

        // Débloquer le saut
        AttributeInstance jumpStrength = player.getAttribute(Attribute.JUMP_STRENGTH);
        if (jumpStrength != null) {
            jumpStrength.getModifiers().stream()
                    .filter(mod -> mod.getKey().equals(FREEZE_MODIFIER_KEY))
                    .findFirst()
                    .ifPresent(jumpStrength::removeModifier);
        }

        // Débloquer le minage
        AttributeInstance blockBreakSpeed = player.getAttribute(Attribute.BLOCK_BREAK_SPEED);
        if (blockBreakSpeed != null) {
            blockBreakSpeed.getModifiers().stream()
                    .filter(mod -> mod.getKey().equals(FREEZE_MODIFIER_KEY))
                    .findFirst()
                    .ifPresent(blockBreakSpeed::removeModifier);
        }

        // Débloquer la vitesse d'attaque
        AttributeInstance attackSpeed = player.getAttribute(Attribute.ATTACK_SPEED);
        if (attackSpeed != null) {
            attackSpeed.getModifiers().stream()
                    .filter(mod -> mod.getKey().equals(FREEZE_MODIFIER_KEY))
                    .findFirst()
                    .ifPresent(attackSpeed::removeModifier);
        }

        // Dégeler aussi la monture si le joueur en a une
        if (player.getVehicle() != null && player.getVehicle() instanceof org.bukkit.entity.LivingEntity mount) {
            AttributeInstance mountSpeed = mount.getAttribute(Attribute.MOVEMENT_SPEED);
            if (mountSpeed != null) {
                mountSpeed.getModifiers().stream()
                        .filter(mod -> mod.getKey().equals(FREEZE_MODIFIER_KEY))
                        .findFirst()
                        .ifPresent(mountSpeed::removeModifier);
            }

            AttributeInstance mountJump = mount.getAttribute(Attribute.JUMP_STRENGTH);
            if (mountJump != null) {
                mountJump.getModifiers().stream()
                        .filter(mod -> mod.getKey().equals(FREEZE_MODIFIER_KEY))
                        .findFirst()
                        .ifPresent(mountJump::removeModifier);
            }
        }
    }

    public static void cleanup() {
        isPaused = false;
        frozenPlayers.clear();

        if (titleTask != null && !titleTask.isCancelled()) {
            titleTask.cancel();
            titleTask = null;
        }
    }
}
