package com.lwidev.survisland.api.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;

public class DirectionUtils {
    
    public static Component getDirectionComponent(Location from, Location to) {
        if (to == null) {
            return Component.text("❌ Aucun campement défini", NamedTextColor.RED);
        }
        
        if (!from.getWorld().equals(to.getWorld())) {
            return Component.text("❌ Campement dans un autre monde", NamedTextColor.RED);
        }
        
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        
        // Si on est très proche du campement (moins de 2 blocs), on considère qu'on y est
        if (distance < 2.0) return Component.text().append(Component.text("Vous êtes arrivé !", NamedTextColor.GREEN)).build();

        String direction = getCardinalDirection(dx, dz);
        String arrow = getDirectionArrow(dx, dz, from.getYaw());
        
        return Component.text()
            .append(Component.text("Campement : ", NamedTextColor.WHITE))
            .append(Component.text(arrow + " ", NamedTextColor.YELLOW))
            .append(Component.text(direction, NamedTextColor.GOLD))
            .append(Component.text(" (", NamedTextColor.GRAY))
            .append(Component.text(String.format("%.0f", distance), NamedTextColor.WHITE))
            .append(Component.text("m)", NamedTextColor.GRAY))
            .build();
    }
    
    private static String getCardinalDirection(double dx, double dz) {
        double angle = Math.atan2(dz, dx) * 180.0 / Math.PI;
        angle = (angle + 360) % 360;
        
        if (angle >= 337.5 || angle < 22.5) {
            return "EST";
        } else if (angle >= 22.5 && angle < 67.5) {
            return "SUD-EST";
        } else if (angle >= 67.5 && angle < 112.5) {
            return "SUD";
        } else if (angle >= 112.5 && angle < 157.5) {
            return "SUD-OUEST";
        } else if (angle >= 157.5 && angle < 202.5) {
            return "OUEST";
        } else if (angle >= 202.5 && angle < 247.5) {
            return "NORD-OUEST";
        } else if (angle >= 247.5 && angle < 292.5) {
            return "NORD";
        } else {
            return "NORD-EST";
        }
    }
    
    private static String getDirectionArrow(double dx, double dz, float playerYaw) {
        // Calcule l'angle vers la cible
        double targetAngle = Math.atan2(dz, dx) * 180.0 / Math.PI;
        
        // Calcule l'angle relatif par rapport à la direction du joueur
        // playerYaw: 0° = Sud, 90° = Ouest, 180° = Nord, 270° = Est
        double relativeAngle = targetAngle - (playerYaw - 90);
        relativeAngle = ((relativeAngle + 360) % 360);
        
        // Convertit l'angle relatif en flèche (inversé pour corriger)
        if (relativeAngle >= 337.5 || relativeAngle < 22.5) {
            return "↓"; // Derrière
        } else if (relativeAngle >= 22.5 && relativeAngle < 67.5) {
            return "↙"; // Arrière-gauche
        } else if (relativeAngle >= 67.5 && relativeAngle < 112.5) {
            return "←"; // Gauche
        } else if (relativeAngle >= 112.5 && relativeAngle < 157.5) {
            return "↖"; // Devant-gauche
        } else if (relativeAngle >= 157.5 && relativeAngle < 202.5) {
            return "↑"; // Devant
        } else if (relativeAngle >= 202.5 && relativeAngle < 247.5) {
            return "↗"; // Devant-droite
        } else if (relativeAngle >= 247.5 && relativeAngle < 292.5) {
            return "→"; // Droite
        } else {
            return "↘"; // Arrière-droite
        }
    }
}