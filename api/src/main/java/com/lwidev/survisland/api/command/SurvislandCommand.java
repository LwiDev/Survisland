package com.lwidev.survisland.api.command;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.lwidev.survisland.api.utils.MessageUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.resolvers.PlayerProfileListResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Base class for every Survisland command. Handles permission derivation
 * ("survisland.&lt;name&gt;", or "survisland.&lt;name&gt;.&lt;subcommand&gt;" for
 * subcommands created via {@link #subcommand(String)}) and player-only
 * enforcement automatically.
 *
 * <p>Subclasses describe their command entirely from the constructor, e.g.:
 * <pre>{@code
 * public PauseCommand(Survisland plugin) {
 *     super("pause", "Activer ou désactiver la pause du jeu", PermissionDefault.OP);
 *     executes(ctx -> { PauseManager.toggle(plugin); return Command.SINGLE_SUCCESS; });
 * }
 * }</pre>
 * No {@code .then(...)} chaining and no manual registration bookkeeping needed —
 * {@link SurvislandCommandManager#register} builds and registers everything.
 */
public abstract class SurvislandCommand {

    private static final String PERMISSION_PREFIX = "survisland";

    private final String name;
    private final String description;
    private final List<String> aliases;
    private final boolean playerOnly;
    private final PermissionDefault permissionDefault;
    private final CommandChain rootChain;
    private final List<Subcommand> subcommands = new ArrayList<>();

    protected SurvislandCommand(String name, String description, PermissionDefault permissionDefault) {
        this(name, description, List.of(), false, permissionDefault);
    }

    protected SurvislandCommand(String name, String description, List<String> aliases,
                                 boolean playerOnly, PermissionDefault permissionDefault) {
        this.name = name;
        this.description = description;
        this.aliases = aliases;
        this.playerOnly = playerOnly;
        this.permissionDefault = permissionDefault;
        this.rootChain = new CommandChain("/" + name);
    }

    public final String description() {
        return description;
    }

    public final List<String> aliases() {
        return aliases;
    }

    public final PermissionDefault permissionDefault() {
        return permissionDefault;
    }

    /** Auto-derived permission node for this command: "survisland.&lt;name&gt;". */
    public final String permission() {
        return PERMISSION_PREFIX + "." + name;
    }

    /** Adds an argument to this command's own (flat) argument chain. */
    public final <T> SurvislandCommand argument(String name, ArgumentType<T> type) {
        rootChain.argument(name, type);
        return this;
    }

    /** @param hint short human-readable description of the expected value, shown if this argument ends up missing */
    public final <T> SurvislandCommand argument(String name, ArgumentType<T> type, String hint) {
        rootChain.argument(name, type, hint);
        return this;
    }

    /** Adds the final argument of this command's own (flat) chain, executed directly. */
    public final <T> SurvislandCommand argument(String name, ArgumentType<T> type, Command<CommandSourceStack> executor) {
        rootChain.argument(name, type);
        rootChain.executes(executor);
        return this;
    }

    /**
     * Adds the final argument of this command's own (flat) chain, executed directly.
     *
     * @param hint short human-readable description of the expected value, shown if this argument ends up missing
     */
    public final <T> SurvislandCommand argument(String name, ArgumentType<T> type, String hint, Command<CommandSourceStack> executor) {
        rootChain.argument(name, type, hint);
        rootChain.executes(executor);
        return this;
    }

    /** Sets the executor for this command's own (flat) argument chain (no-argument case). */
    public final SurvislandCommand executes(Command<CommandSourceStack> executor) {
        rootChain.executes(executor);
        return this;
    }

    /** Adds a literal alternative (e.g. a no-arg root behavior variant) at the flat chain's tail. */
    public final SurvislandCommand onLiteral(String name, Command<CommandSourceStack> executor) {
        rootChain.onLiteral(name, executor);
        return this;
    }

    /** Adds an argument alternative at the flat chain's tail. */
    public final <T> SurvislandCommand onArgument(String name, ArgumentType<T> type, Command<CommandSourceStack> executor) {
        rootChain.onArgument(name, type, executor);
        return this;
    }

    /**
     * Declares a subcommand (e.g. "force" for "/skin force ..."). Its permission
     * is auto-derived as {@code permission() + "." + name} and is registered
     * automatically by {@link SurvislandCommandManager#register}.
     */
    public final Subcommand subcommand(String name) {
        Subcommand sub = new Subcommand(name, permission() + "." + name, playerOnly, "/" + this.name);
        subcommands.add(sub);
        return sub;
    }

    /** Resolves a single-player selector argument (name or @a/@e/@p/@r/@s selector). */
    public final Player resolvePlayer(CommandContext<CommandSourceStack> ctx, String argName) throws CommandSyntaxException {
        PlayerSelectorArgumentResolver resolver = ctx.getArgument(argName, PlayerSelectorArgumentResolver.class);
        return resolver.resolve(ctx.getSource()).getFirst();
    }

    /** Resolves a multi-player selector argument (@a/@e/@r or a single name). */
    public final List<Player> resolvePlayers(CommandContext<CommandSourceStack> ctx, String argName) throws CommandSyntaxException {
        PlayerSelectorArgumentResolver resolver = ctx.getArgument(argName, PlayerSelectorArgumentResolver.class);
        return resolver.resolve(ctx.getSource());
    }

    /**
     * Resolves a player-profile argument (name or @a/@e/@p/@r/@s selector). Unlike
     * {@link #resolvePlayer}/{@link #resolvePlayers}, this also matches players who
     * are currently offline: for a plain name, it's looked up via the server's local
     * profile cache (usercache.json) first, falling back to a blocking Mojang HTTP
     * call only for a name never seen on this server before.
     */
    public final Collection<PlayerProfile> resolvePlayerProfiles(CommandContext<CommandSourceStack> ctx, String argName) throws CommandSyntaxException {
        PlayerProfileListResolver resolver = ctx.getArgument(argName, PlayerProfileListResolver.class);
        return resolver.resolve(ctx.getSource());
    }

    private boolean hasAccess(CommandSourceStack source) {
        CommandSender sender = source.getSender();
        if (!sender.hasPermission(permission())) {
            return false;
        }
        return !playerOnly || sender instanceof Player;
    }

    /** Subcommand names, used by {@link SurvislandCommandManager} to auto-register their permissions. */
    final List<String> subcommandNames() {
        return subcommands.stream().map(Subcommand::name).toList();
    }

    /** Builds the full Brigadier node for this command, ready to register. */
    final LiteralCommandNode<CommandSourceStack> buildNode() {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(name).requires(this::hasAccess);
        rootChain.applyTo(root);
        for (Subcommand sub : subcommands) {
            root.then(sub.build());
        }
        if (!rootChain.hasExecutor() && !subcommands.isEmpty()) {
            String usage = "Utilisation : /" + name + " <" + String.join("|", subcommandNames()) + ">";
            root.executes(ctx -> {
                MessageUtils.sendErrorMessage(ctx.getSource().getSender(), usage);
                return Command.SINGLE_SUCCESS;
            });
        }
        return root.build();
    }
}
