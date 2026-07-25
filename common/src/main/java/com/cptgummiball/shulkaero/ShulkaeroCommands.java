package com.cptgummiball.shulkaero;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.network.chat.Component;

/**
 * The /shulkaero client command, built loader-agnostically: the brigadier tree
 * is generic over the command source type, feedback goes through the given
 * consumer. Registered by the loader entrypoints (NeoForge:
 * RegisterClientCommandsEvent; Fabric: ClientCommandRegistrationCallback,
 * only when Fabric API is installed).
 */
public final class ShulkaeroCommands {

    private ShulkaeroCommands() {
    }

    public static <S> LiteralArgumentBuilder<S> build(BiConsumer<S, Component> feedback) {
        LiteralArgumentBuilder<S> root = LiteralArgumentBuilder.literal(Shulkaero.MOD_ID);
        root.executes(ctx -> {
            feedback.accept(ctx.getSource(), Component.translatable("shulkaero.command.usage"));
            return 1;
        });
        root.then(LiteralArgumentBuilder.<S>literal("list").executes(ctx -> {
            List<Component> lines = listLines();
            for (Component line : lines) {
                feedback.accept(ctx.getSource(), line);
            }
            return lines.size();
        }));
        root.then(LiteralArgumentBuilder.<S>literal("clear").executes(ctx -> {
            int count = ShulkerWaypointManager.clearCurrentWorld();
            feedback.accept(ctx.getSource(), Component.translatable("shulkaero.command.clear.done", count));
            return count;
        }));
        root.then(LiteralArgumentBuilder.<S>literal("toggle").executes(ctx -> {
            ShulkaeroConfig config = ShulkaeroConfig.get();
            config.enabled = !config.enabled;
            config.save();
            feedback.accept(ctx.getSource(), Component.translatable(
                    config.enabled ? "shulkaero.command.toggle.on" : "shulkaero.command.toggle.off"));
            return config.enabled ? 1 : 0;
        }));
        root.then(LiteralArgumentBuilder.<S>literal("reload").executes(ctx -> {
            ShulkaeroConfig.reload();
            feedback.accept(ctx.getSource(), Component.translatable("shulkaero.command.reload.done"));
            return 1;
        }));
        return root;
    }

    private static List<Component> listLines() {
        List<TrackingStore.Entry> entries = ShulkerWaypointManager.entriesForCurrentWorld();
        if (entries.isEmpty()) {
            return List.of(Component.translatable("shulkaero.command.list.empty"));
        }
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("shulkaero.command.list.header", entries.size()));
        for (TrackingStore.Entry entry : entries) {
            Component state = Component.translatable(entry.isDropped()
                    ? "shulkaero.command.state.dropped"
                    : "shulkaero.command.state.tracked");
            lines.add(Component.translatable("shulkaero.command.list.entry",
                    entry.waypointName, entry.x, entry.y, entry.z, state));
        }
        return lines;
    }
}
