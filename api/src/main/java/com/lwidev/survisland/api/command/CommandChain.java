package com.lwidev.survisland.api.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

import java.util.ArrayList;
import java.util.List;

/**
 * A linear chain of arguments ending in an executor, plus optional branch
 * alternatives (literal or argument) at the tail. Shared building block used
 * by both {@link SurvislandCommand} (top-level, flat commands) and
 * {@link Subcommand}.
 */
final class CommandChain {

    private record ArgSpec<T>(String name, ArgumentType<T> type) {
        RequiredArgumentBuilder<CommandSourceStack, T> toBuilder() {
            return Commands.argument(name, type);
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

    private final List<ArgSpec<?>> steps = new ArrayList<>();
    private final List<Branch> branches = new ArrayList<>();
    private Command<CommandSourceStack> executor;

    <T> void argument(String name, ArgumentType<T> type) {
        steps.add(new ArgSpec<>(name, type));
    }

    void executes(Command<CommandSourceStack> executor) {
        this.executor = executor;
    }

    void onLiteral(String name, Command<CommandSourceStack> executor) {
        branches.add(new LiteralBranch(name, executor));
    }

    <T> void onArgument(String name, ArgumentType<T> type, Command<CommandSourceStack> executor) {
        branches.add(new ArgumentBranch<>(name, type, executor));
    }

    /** Attaches this chain's arguments/executor/branches onto {@code node}. */
    void applyTo(ArgumentBuilder<CommandSourceStack, ?> node) {
        ArgumentBuilder<CommandSourceStack, ?> tail = attachSteps(node);
        if (executor != null) {
            tail.executes(executor);
        }
        for (Branch branch : branches) {
            tail.then(branch.build());
        }
    }

    private ArgumentBuilder<CommandSourceStack, ?> attachSteps(ArgumentBuilder<CommandSourceStack, ?> root) {
        if (steps.isEmpty()) {
            return root;
        }

        List<RequiredArgumentBuilder<CommandSourceStack, ?>> nodes = new ArrayList<>(steps.size());
        for (ArgSpec<?> step : steps) {
            nodes.add(step.toBuilder());
        }
        for (int i = nodes.size() - 2; i >= 0; i--) {
            nodes.get(i).then(nodes.get(i + 1));
        }
        root.then(nodes.getFirst());
        return nodes.getLast();
    }
}
