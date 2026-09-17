package com.lwidev.survisland.api.command;

import com.lwidev.survisland.api.utils.MessageUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A linear chain of arguments ending in an executor, plus optional branch
 * alternatives (literal or argument) at the tail. Shared building block used
 * by both {@link SurvislandCommand} (top-level, flat commands) and
 * {@link Subcommand}.
 *
 * <p>Every non-final argument step gets a fallback executor so that stopping
 * early (e.g. {@code /skin force Lwi_} without the trailing skin) reports a
 * clear "Utilisation : ..." message instead of Brigadier's generic
 * "Unknown or incomplete command".
 */
final class CommandChain {

    private record ArgSpec<T>(String name, ArgumentType<T> type, String hint, SuggestionProvider<CommandSourceStack> suggestions, String permission) {
        RequiredArgumentBuilder<CommandSourceStack, T> toBuilder() {
            RequiredArgumentBuilder<CommandSourceStack, T> builder = Commands.argument(name, type);
            if (suggestions != null) {
                builder.suggests(suggestions);
            }
            if (permission != null) {
                builder.requires(source -> source.getSender().hasPermission(permission));
            }
            return builder;
        }
    }

    private sealed interface Branch {
        ArgumentBuilder<CommandSourceStack, ?> build();
    }

    private record LiteralBranch(String name, Command<CommandSourceStack> executor) implements Branch {
        @Override
        public ArgumentBuilder<CommandSourceStack, ?> build() {
            return Commands.literal(name).executes(executor);
        }
    }

    private record ArgumentBranch<T>(String name, ArgumentType<T> type, Command<CommandSourceStack> executor) implements Branch {
        @Override
        public ArgumentBuilder<CommandSourceStack, ?> build() {
            return Commands.argument(name, type).executes(executor);
        }
    }

    private final String label;
    private final List<ArgSpec<?>> steps = new ArrayList<>();
    private final List<Branch> branches = new ArrayList<>();
    private Command<CommandSourceStack> executor;
    private Command<CommandSourceStack> zeroArgExecutor;

    CommandChain(String label) {
        this.label = label;
    }

    <T> void argument(String name, ArgumentType<T> type) {
        argument(name, type, null);
    }

    /** @param hint short human-readable description of the expected value, shown when this argument is missing */
    <T> void argument(String name, ArgumentType<T> type, String hint) {
        argument(name, type, hint, null);
    }

    /**
     * @param hint        short human-readable description of the expected value, shown when this argument is missing
     * @param suggestions optional tab-completion suggestions for this argument
     */
    <T> void argument(String name, ArgumentType<T> type, String hint, SuggestionProvider<CommandSourceStack> suggestions) {
        steps.add(new ArgSpec<>(name, type, hint, suggestions, null));
    }

    /**
     * @param hint       short human-readable description of the expected value, shown when this argument is missing
     * @param permission permission node required to reach this argument (and anything chained after it) —
     *                   senders without it can't complete or tab-complete past this point
     */
    <T> void restrictedArgument(String name, ArgumentType<T> type, String hint, String permission) {
        steps.add(new ArgSpec<>(name, type, hint, null, permission));
    }

    /**
     * Sets the executor run when this chain's root node itself is reached with nothing more
     * to parse — whether that's because this chain has no steps at all, or because a sender
     * stopped right at the root of a chain that also has further steps (e.g. plain {@code /afk}
     * alongside a further {@code /afk <joueur>} step).
     */
    void executes(Command<CommandSourceStack> executor) {
        this.zeroArgExecutor = executor;
    }

    /** Sets the executor for this chain's tail step — run once all of this chain's arguments are supplied. */
    void tailExecutes(Command<CommandSourceStack> executor) {
        this.executor = executor;
    }

    /** Whether this chain has an executor set (used for root-level usage fallback). */
    boolean hasExecutor() {
        return executor != null || zeroArgExecutor != null;
    }

    void onLiteral(String name, Command<CommandSourceStack> executor) {
        branches.add(new LiteralBranch(name, executor));
    }

    <T> void onArgument(String name, ArgumentType<T> type, Command<CommandSourceStack> executor) {
        branches.add(new ArgumentBranch<>(name, type, executor));
    }

    /**
     * Attaches this chain's arguments/executor/branches onto {@code node}.
     *
     * <p>Brigadier's {@code ArgumentBuilder#then(ArgumentBuilder)} calls {@code build()}
     * on the child <em>immediately</em>, freezing an immutable snapshot of it. So every
     * node below must have its own {@code executes()}/{@code then()} calls finished
     * <em>before</em> it is attached to its parent — mutating a builder after it has
     * already been chained via {@code then()} has no effect on the tree that was built
     * from it. This method therefore configures nodes tail-first.
     */
    void applyTo(ArgumentBuilder<CommandSourceStack, ?> node) {
        if (steps.isEmpty()) {
            if (zeroArgExecutor != null) {
                node.executes(zeroArgExecutor);
            }
            for (Branch branch : branches) {
                node.then(branch.build());
            }
            return;
        }

        List<RequiredArgumentBuilder<CommandSourceStack, ?>> nodes = new ArrayList<>(steps.size());
        for (ArgSpec<?> step : steps) {
            nodes.add(step.toBuilder());
        }

        RequiredArgumentBuilder<CommandSourceStack, ?> tail = nodes.getLast();
        if (executor != null) {
            tail.executes(executor);
        }
        for (Branch branch : branches) {
            tail.then(branch.build());
        }

        for (int i = nodes.size() - 2; i >= 0; i--) {
            nodes.get(i).executes(usageFallback(steps.get(i + 1)));
            nodes.get(i).then(nodes.get(i + 1));
        }

        // Reached with zero arguments (e.g. "/skin force" alone): run the explicit zero-arg
        // executor if one was set (e.g. plain "/afk"), otherwise report what's missing instead
        // of leaving Brigadier to report a generic "incomplete command".
        node.executes(zeroArgExecutor != null ? zeroArgExecutor : usageFallback(steps.getFirst()));
        node.then(nodes.getFirst());
    }

    private Command<CommandSourceStack> usageFallback(ArgSpec<?> missing) {
        String usage = label + " " + steps.stream().map(step -> "<" + step.name() + ">").collect(Collectors.joining(" "));
        return ctx -> {
            var sender = ctx.getSource().getSender();
            MessageUtils.sendErrorMessage(sender, "Utilisation : " + usage);
            if (missing.hint() != null) {
                MessageUtils.sendSecondaryMessage(sender, "Vous devez renseigner " + missing.hint());
            }
            return Command.SINGLE_SUCCESS;
        };
    }
}
