package com.lwidev.survisland.api.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * A subcommand of a {@link SurvislandCommand}, created via
 * {@link SurvislandCommand#subcommand(String)}. Its permission node is
 * auto-derived as "&lt;command permission&gt;.&lt;name&gt;".
 */
public final class Subcommand {

    private final String name;
    private final String permission;
    private final boolean playerOnly;
    private final CommandChain chain;

    Subcommand(String name, String permission, boolean playerOnly, String parentLabel) {
        this.name = name;
        this.permission = permission;
        this.playerOnly = playerOnly;
        this.chain = new CommandChain(parentLabel + " " + name);
    }

    public <T> Subcommand argument(String name, ArgumentType<T> type) {
        chain.argument(name, type);
        return this;
    }

    /** @param hint short human-readable description of the expected value, shown if this argument ends up missing */
    public <T> Subcommand argument(String name, ArgumentType<T> type, String hint) {
        chain.argument(name, type, hint);
        return this;
    }

    /** Adds the final argument of this subcommand's chain, executed directly. */
    public <T> Subcommand argument(String name, ArgumentType<T> type, Command<CommandSourceStack> executor) {
        chain.argument(name, type);
        chain.executes(executor);
        return this;
    }

    /**
     * Adds the final argument of this subcommand's chain, executed directly.
     *
     * @param hint short human-readable description of the expected value, shown if this argument ends up missing
     */
    public <T> Subcommand argument(String name, ArgumentType<T> type, String hint, Command<CommandSourceStack> executor) {
        chain.argument(name, type, hint);
        chain.executes(executor);
        return this;
    }

    /** @param suggestions tab-completion suggestions for this argument */
    public <T> Subcommand argument(String name, ArgumentType<T> type, String hint, SuggestionProvider<CommandSourceStack> suggestions) {
        chain.argument(name, type, hint, suggestions);
        return this;
    }

    /**
     * Adds the final argument of this subcommand's chain, executed directly.
     *
     * @param hint        short human-readable description of the expected value, shown if this argument ends up missing
     * @param suggestions tab-completion suggestions for this argument
     */
    public <T> Subcommand argument(String name, ArgumentType<T> type, String hint, SuggestionProvider<CommandSourceStack> suggestions, Command<CommandSourceStack> executor) {
        chain.argument(name, type, hint, suggestions);
        chain.executes(executor);
        return this;
    }

    public Subcommand executes(Command<CommandSourceStack> executor) {
        chain.executes(executor);
        return this;
    }

    /** Adds a literal alternative (e.g. "restore") at the current tail of this subcommand. */
    public Subcommand onLiteral(String name, Command<CommandSourceStack> executor) {
        chain.onLiteral(name, executor);
        return this;
    }

    /** Adds an argument alternative at the current tail of this subcommand. */
    public <T> Subcommand onArgument(String name, ArgumentType<T> type, Command<CommandSourceStack> executor) {
        chain.onArgument(name, type, executor);
        return this;
    }

    String name() {
        return name;
    }

    LiteralArgumentBuilder<CommandSourceStack> build() {
        LiteralArgumentBuilder<CommandSourceStack> node = Commands.literal(name)
                .requires(source -> {
                    CommandSender sender = source.getSender();
                    if (!sender.hasPermission(permission)) {
                        return false;
                    }
                    return !playerOnly || sender instanceof Player;
                });
        chain.applyTo(node);
        return node;
    }
}
