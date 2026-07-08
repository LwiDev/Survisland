package com.lwidev.survisland;

import com.lwidev.survisland.api.command.SurvislandCommandManager;
import com.lwidev.survisland.api.menu.SurvislandMenuManager;
import com.lwidev.survisland.api.utils.Shutdownable;
import com.lwidev.survisland.commands.LiveCommand;
import com.lwidev.survisland.commands.SetLiveCommand;
import com.lwidev.survisland.commands.ConfessCommand;
import com.lwidev.survisland.commands.LinkCommand;
import com.lwidev.survisland.commands.CampCommand;
import com.lwidev.survisland.commands.FollowCommand;
import com.lwidev.survisland.commands.MenuCommand;
import com.lwidev.survisland.commands.PauseCommand;
import com.lwidev.survisland.commands.SkinCommand;
import com.lwidev.survisland.confess.ConfessLinkManager;
import com.lwidev.survisland.game.AnnouncementService;
import com.lwidev.survisland.game.TimerService;
import com.lwidev.survisland.game.VoteService;
import com.lwidev.survisland.listeners.PauseListener;
import com.lwidev.survisland.confess.LinkCodeManager;
import com.lwidev.survisland.discord.EmbeddedDiscordBot;
import com.lwidev.survisland.chatspec.ChatSpecManager;
import com.lwidev.survisland.menu.MenuContext;
import com.lwidev.survisland.services.FollowManager;
import com.lwidev.survisland.services.PauseManager;
import com.lwidev.survisland.skins.SkinManager;
import com.lwidev.survisland.config.DiscordConfig;
import com.lwidev.survisland.teams.TeamManager;
import com.lwidev.survisland.utils.CompassTask;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public final class Survisland extends JavaPlugin {

    private EmbeddedDiscordBot discordBot;
    private ConfessLinkManager confessLinkManager;
    private LinkCodeManager linkCodeManager;
    private SkinManager skinManager;
    private DiscordConfig discordConfig;
    private TimerService timerService;
    private FollowManager followManager;
    private PauseManager pauseManager;
    private final List<Shutdownable> shutdownables = new ArrayList<>();

    @Override
    public void onEnable() {
        try {
            // Load configuration
            saveDefaultConfig();
            loadDiscordConfig();

            // Initialize managers
            this.confessLinkManager = new ConfessLinkManager(this);
            this.linkCodeManager = track(new LinkCodeManager(this));
            this.discordBot = track(new EmbeddedDiscordBot(this, discordConfig));
            this.discordBot.setConfessLinkManager(confessLinkManager);
            this.skinManager = track(new SkinManager(this));
            new ChatSpecManager(this);
            this.timerService = track(new TimerService(this));
            this.followManager = track(new FollowManager(this));
            this.pauseManager = track(new PauseManager(this));
            new PauseListener(this, pauseManager);
            shutdownables.add(CompassTask::shutdownAll);

            // Initialize Discord bot asynchronously
            initializeDiscordBot();

            // Register commands
            registerCommands();

            // Register the menu system's single listener
            SurvislandMenuManager.register(this);

            getLogger().info("Survisland plugin enabled successfully!");

        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to enable Survisland plugin", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        shutdownables.forEach(Shutdownable::shutdown);
        getLogger().info("Survisland plugin disabled");
    }

    /** Registers a manager/service for automatic shutdown() on plugin disable, and returns it for inline assignment. */
    private <T extends Shutdownable> T track(T shutdownable) {
        shutdownables.add(shutdownable);
        return shutdownable;
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
        SurvislandCommandManager.register(this,
                new LiveCommand(discordBot),
                new SetLiveCommand(this, discordBot),
                new ConfessCommand(discordBot, confessLinkManager),
                new LinkCommand(linkCodeManager),
                new CampCommand(this),
                new PauseCommand(pauseManager),
                new SkinCommand(skinManager),
                new FollowCommand(this, followManager),
                new MenuCommand(new MenuContext(this, new TeamManager(), new AnnouncementService(this), timerService, new VoteService(this), pauseManager))
        );

        getLogger().info("Commandes enregistrées : /live, /setlive, /confess, /link, /camp, /pause, /skin, /follow, /menu");
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
