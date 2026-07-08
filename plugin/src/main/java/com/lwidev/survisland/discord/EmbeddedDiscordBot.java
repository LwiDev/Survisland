package com.lwidev.survisland.discord;

import com.lwidev.survisland.Survisland;
import com.lwidev.survisland.api.utils.Shutdownable;
import com.lwidev.survisland.config.DiscordConfig;
import com.lwidev.survisland.confess.ConfessLinkManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.logging.Level;

public class EmbeddedDiscordBot extends ListenerAdapter implements Shutdownable {
    
    private final Survisland plugin;
    private final DiscordConfig config;
    private ConfessLinkManager confessLinkManager;
    private JDA jda;
    private volatile boolean isInitialized = false;
    
    public EmbeddedDiscordBot(Survisland plugin, DiscordConfig config) {
        this.plugin = plugin;
        this.config = config;
    }
    
    public CompletableFuture<Boolean> initialize() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String token = getDiscordToken();
                
                if (token == null || token.isEmpty()) {
                    plugin.getLogger().warning("Discord bot token not configured. Set DISCORD_BOT_TOKEN environment variable or add discord.bot.token to config.yml");
                    return false;
                }
                
                plugin.getLogger().info("Initializing Discord bot...");
                
                jda = JDABuilder.createDefault(token)
                        .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES)
                        .addEventListeners(this)
                        .build();
                        
                jda.awaitReady();
                
                // Register slash commands
                jda.updateCommands().addCommands(
                    Commands.slash("verify", "Vérifier votre code de liaison Minecraft")
                        .addOption(OptionType.STRING, "code", "Le code généré avec /link en jeu", true)
                ).queue();
                
                isInitialized = true;
                
                plugin.getLogger().info("Discord bot initialized successfully! Connected as: " + jda.getSelfUser().getAsTag());
                return true;
                
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to initialize Discord bot", e);
                return false;
            }
        });
    }
    
    public void sendLiveMessage(String message, String sender, BiConsumer<Boolean, String> callback) {
        String channelId = config.getLiveChannelId();
        
        if (channelId == null || channelId.isEmpty()) {
            callback.accept(false, "Aucun channel Discord configuré. Utilisez /setlive <channel_id>");
            return;
        }
        
        sendMessage(channelId, message, sender, callback);
    }
    
    public void sendMessage(String channelId, String message, String sender, BiConsumer<Boolean, String> callback) {
        sendMessage(channelId, message, sender, false, callback);
    }

    public void sendConfessMessage(String channelId, String message, String sender, BiConsumer<Boolean, String> callback) {
        sendMessage(channelId, message, sender, true, callback);
    }
    
    private void sendMessage(String channelId, String message, String sender, boolean isConfess, BiConsumer<Boolean, String> callback) {
        if (!isInitialized || jda == null) {
            callback.accept(false, "Discord bot not initialized");
            return;
        }
        
        CompletableFuture.runAsync(() -> {
            try {
                TextChannel channel = jda.getTextChannelById(channelId);
                if (channel == null) {
                    callback.accept(false, "Channel Discord introuvable : " + channelId);
                    return;
                }
                
                String formattedMessage;
                if (isConfess) {
                    // Format spécial pour les confess
                    String confessFormat = plugin.getConfig().getString("discord.confess-format", "🎮 {message}");
                    formattedMessage = confessFormat.replace("{sender}", sender).replace("{message}", message);
                } else if (sender != null && !sender.isEmpty()) {
                    // Format normal avec sender
                    String messageFormat = plugin.getConfig().getString("discord.message-format", "**[{sender}]** {message}");
                    formattedMessage = messageFormat.replace("{sender}", sender).replace("{message}", message);
                } else {
                    formattedMessage = message;
                }
                
                channel.sendMessage(formattedMessage).queue(
                    sentMessage -> {
                        callback.accept(true, null);
                        plugin.getLogger().info("Message sent to Discord channel " + channelId);
                    },
                    error -> {
                        callback.accept(false, error.getMessage());
                        plugin.getLogger().warning("Failed to send message to Discord: " + error.getMessage());
                    }
                );
                
            } catch (Exception e) {
                callback.accept(false, e.getMessage());
                plugin.getLogger().severe("Error sending message to Discord: " + e.getMessage());
            }
        });
    }
    
    @Override
    public void shutdown() {
        if (jda != null) {
            plugin.getLogger().info("Shutting down Discord bot...");
            jda.shutdown();
            jda = null;
        }
        isInitialized = false;
    }
    
    public boolean isConnected() {
        return isInitialized && jda != null && jda.getStatus() == JDA.Status.CONNECTED;
    }
    
    public JDA getJDA() {
        return jda;
    }

    private String getDiscordToken() {
        // Try environment variable first (Docker/production friendly)
        String token = System.getenv("DISCORD_BOT_TOKEN");
        
        if (token == null || token.isEmpty()) {
            // Try plugin config
            token = plugin.getConfig().getString("discord.token", "");
        }
        
        return token;
    }
    
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("verify")) {
            return;
        }
        
        // Vérifier que la commande est utilisée dans un confess
        String channelName = event.getChannel().getName();
        if (!channelName.startsWith("confess-")) {
            event.reply("❌ Cette commande ne peut être utilisée que dans un salon de confess !").setEphemeral(true).queue();
            return;
        }
        
        String verificationCode = Objects.requireNonNull(event.getOption("code")).getAsString().toUpperCase();
        if (confessLinkManager == null) {
            event.reply("❌ Le système de confession n'est pas initialisé !").setEphemeral(true).queue();
            return;
        }
        
        // Vérifier le code via le LinkCodeManager
        if (plugin.getLinkCodeManager() == null) {
            event.reply("❌ Le système de codes de liaison n'est pas initialisé !").setEphemeral(true).queue();
            return;
        }
        
        boolean success = plugin.getLinkCodeManager().validateAndConsumeLinkCode(verificationCode, channelName);
        
        if (success) {
            event.reply("✅ Votre compte a été lié avec succès au salon " + channelName + " !\n" +
                       "Vous pouvez maintenant utiliser `/confess <message>` en jeu.").setEphemeral(true).queue();
            
            plugin.getLogger().info("Discord user successfully verified code " + verificationCode + " and linked to " + channelName);
        } else {
            event.reply("❌ Code de vérification invalide ou expiré !\n" +
                       "Générez un nouveau code avec `/link` en jeu.").setEphemeral(true).queue();
                       
            plugin.getLogger().warning("Discord user failed to verify code: " + verificationCode);
        }
    }
    
    public void setConfessLinkManager(ConfessLinkManager confessLinkManager) {
        this.confessLinkManager = confessLinkManager;
    }
}