package io.github.echoocelot.pegasus.api;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;

public class PegasusMessaging {
    public static final Color PLUGIN_COLOR = new Color(0x7851A9);
    public static final Component PLUGIN_WORDMARK_COMPONENT = Component.text("Pegasus", TextColor.color(PLUGIN_COLOR.getRGB()));
    public static final Component PREFIX_COMPONENT = PLUGIN_WORDMARK_COMPONENT.append(Component.text(" » ").color(NamedTextColor.DARK_GRAY));

    public static void sendMessage(@NotNull Player player, @NotNull String message) {
        Component formattedMessage = PREFIX_COMPONENT.append(Component.text(message, NamedTextColor.YELLOW));
        player.sendMessage(formattedMessage);
    }

    public static void sendErrorMessage(@NotNull Player player, @NotNull String message) {
        Component formattedMessage = PREFIX_COMPONENT.append(Component.text(message, NamedTextColor.RED));
        player.sendMessage(formattedMessage);
    }

    public static void sendComponent(@NotNull Player player, @NotNull Component component) {
        Component formattedMessage = PREFIX_COMPONENT.append(component);
        player.sendMessage(formattedMessage);
    }
}
