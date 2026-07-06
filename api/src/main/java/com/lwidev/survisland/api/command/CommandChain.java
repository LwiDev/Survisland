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

    private record ArgSpec<T>(String name, ArgumentType<T> type, String hint, SuggestionProvider<CommandSourceStack> suggestions) {
        RequiredArgumentBuilder<CommandSourceStack, T> toBuilder() {
            RequiredArgumentBuilder<CommandSourceStack, T> builder = Commands.argument(name, type);
            if (suggestions != null) {
                builder.suggests(suggestions);
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
        steps.add(new ArgSpec<>(name, type, hint, suggestions));
    }

    void executes(Command<CommandSourceStack> executor) {
        this.executor = executor;
    }

    /** Whether this chain has a no-argument executor (used for root-level usage fallback). */
    boolean hasExecutor() {
        return executor != null;
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
            if (executor != null) {
                node.executes(executor);
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

        // Reached with zero arguments (e.g. "/skin force" alone): report what's missing
        // instead of leaving Brigadier to report a generic "incomplete command".
        node.executes(usageFallback(steps.getFirst()));
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
