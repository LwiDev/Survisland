package com.lwidev.survisland;

import com.lwidev.survisland.api.command.SurvislandCommands;
import com.lwidev.survisland.commands.LiveCommand;
import com.lwidev.survisland.commands.SetLiveCommand;
import com.lwidev.survisland.commands.ConfessCommand;
import com.lwidev.survisland.commands.LinkCommand;
import com.lwidev.survisland.commands.CampCommand;
import com.lwidev.survisland.commands.PauseCommand;
import com.lwidev.survisland.commands.SkinCommand;
import com.lwidev.survisland.confess.ConfessLinkManager;
import com.lwidev.survisland.listeners.PauseListener;
import com.lwidev.survisland.confess.LinkCodeManager;
import com.lwidev.survisland.discord.EmbeddedDiscordBot;
import com.lwidev.survisland.chatspec.ChatSpecManager;
import com.lwidev.survisland.skins.SkinManager;
import com.lwidev.survisland.config.DiscordConfig;
import com.lwidev.survisland.utils.CompassTask;
import com.lwidev.survisland.utils.PauseManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class Survisland extends JavaPlugin {
    
    private EmbeddedDiscordBot discordBot;
    private ConfessLinkManager confessLinkManager;
    private LinkCodeManager linkCodeManager;
    private SkinManager skinManager;
    private DiscordConfig discordConfig;

    @Override
    public void onEnable() {
        try {
            // Load configuration
            saveDefaultConfig();
            loadDiscordConfig();
            
            // Initialize managers
            this.confessLinkManager = new ConfessLinkManager(this);
            this.linkCodeManager = new LinkCodeManager(this);
            this.discordBot = new EmbeddedDiscordBot(this, discordConfig);
            this.discordBot.setConfessLinkManager(confessLinkManager);
            this.skinManager = new SkinManager(this);
            new ChatSpecManager(this);

            // Initialize Discord bot asynchronously
            initializeDiscordBot();
            
            // Register commands
            registerCommands();

            // Register listeners
            getServer().getPluginManager().registerEvents(new PauseListener(), this);

            getLogger().info("Survisland plugin enabled successfully!");
            
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to enable Survisland plugin", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (discordBot != null) {
            discordBot.shutdown();
        }
        if (linkCodeManager != null) {
            linkCodeManager.shutdown();
        }
        if (skinManager != null) {
            skinManager.shutdown();
        }
        // Nettoyer toutes les tâches de boussole actives
        CompassTask.cleanupAll();
        // Nettoyer la pause
        PauseManager.cleanup();
        getLogger().info("Survisland plugin disabled");
    }
    
    private void loadDiscordConfig() {
        discordConfig = new DiscordConfig();
        
        // Load from plugin config
        String channelId = getConfig().getString("discord.live-channel-id", "");
        if (!channelId.isEmpty()) {
            discordConfig.setLiveChannelId(channelId);
        }
    }
    
    private void registerCommands() {
        SurvislandCommands.register(this,
                new LiveCommand(discordBot),
                new SetLiveCommand(this, discordBot),
                new ConfessCommand(discordBot, confessLinkManager),
                new LinkCommand(linkCodeManager),
                new CampCommand(this),
                new PauseCommand(this),
                new SkinCommand(skinManager)
        );

        getLogger().info("Commandes enregistrées : /live, /setlive, /confess, /link, /camp, /pause, /skin");
    }
    
    private void initializeDiscordBot() {
        discordBot.initialize().thenAccept(success -> {
            if (success) {
                getLogger().info("Discord bot integration enabled successfully!");
            } else {
                getLogger().warning("Discord bot failed to initialize. Discord features will be disabled.");
                getLogger().warning("Make sure to configure discord.bot.token in config.yml or set DISCORD_BOT_TOKEN environment variable");
            }
        }).exceptionally(throwable -> {
            getLogger().log(Level.SEVERE, "Error during Discord bot initialization", throwable);
            return null;
        });
    }
    
    public EmbeddedDiscordBot getDiscordBot() {
        return discordBot;
    }

    public DiscordConfig getDiscordConfig() {
        return discordConfig;
    }

    public ConfessLinkManager getConfessLinkManager() {
        return confessLinkManager;
    }
    
    public LinkCodeManager getLinkCodeManager() {
        return linkCodeManager;
    }
}
