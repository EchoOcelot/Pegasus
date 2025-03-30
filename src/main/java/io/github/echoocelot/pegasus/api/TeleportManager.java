package io.github.echoocelot.pegasus.api;

import io.github.echoocelot.pegasus.Pegasus;
import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static io.github.echoocelot.pegasus.Pegasus.isFoliaEnabled;

public class TeleportManager {
    @NotNull
    private static final Pegasus plugin = JavaPlugin.getPlugin(Pegasus.class);
    private static final Map<String, Object[]> teleports = new HashMap<>();

    public static void tpMountAndPlayer(Player player, Entity mount, Location to) {
        if (!(mount instanceof Boat || mount instanceof Minecart)) {
            if (mount instanceof Camel) {
                mount.getScheduler().execute(plugin, () -> {
                    List<Entity> passengers = mount.getPassengers();
                    if (passengers.size() == 2) {
                        for (Entity e : passengers) {
                            if (e instanceof Player)
                                PegasusMessaging.sendErrorMessage((Player) e, "You cannot teleport with two passengers on a camel!");
                        }
                    }
                }, null, 0L);
            }
            EntityScheduler mountScheduler = mount.getScheduler();

            mountScheduler.execute(plugin, mount::eject, null, 5L);

            if (isFoliaEnabled()) {
                @NotNull CompletableFuture<Boolean> mountTp = mount.teleportAsync(to);
                @NotNull CompletableFuture<Boolean> playerTp = player.teleportAsync(to);

                mountTp.thenCombine(playerTp, (__, ___) -> {
                    mountScheduler.execute(plugin, () -> {
                        mount.addPassenger(player);
                    }, null, 5L);
                    return null;
                });
            } else {
                EntityScheduler playerScheduler = player.getScheduler();
                mountScheduler.execute(plugin, () -> {
                    mount.teleport(to);
                }, null, 7L);

                playerScheduler.execute(plugin, () -> {
                    player.teleport(to);
                }, null, 9L);

                mountScheduler.execute(plugin, () -> {
                    mount.addPassenger(player);
                }, null, 11L);
            }
        }
    }

    public static CompletableFuture<Boolean> couldSuffocationOccur(Entity mount, Location to) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        RegionScheduler regionScheduler = Bukkit.getRegionScheduler();
        Block spawn = to.getBlock();
        AtomicInteger pendingChecks = new AtomicInteger(0);
        AtomicReference<Boolean> suffocation = new AtomicReference<>(false);

        switch (mount.getType()) {
            case PIG -> {
                for (int i = 0; i < 2; i++) {
                    Block toCheck = spawn.getRelative(0, i, 0);
                    pendingChecks.incrementAndGet();
                    regionScheduler.execute(plugin, toCheck.getLocation(), () -> {
                        if (!toCheck.isEmpty() || !toCheck.isPassable()) suffocation.set(true);
                        if (pendingChecks.decrementAndGet() == 0) future.complete(suffocation.get());
                    });
                }
            }
            case CAMEL -> {
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 4; j++) {
                        for (int k = 0; k < 2; k++) {
                            Block toCheck = spawn.getRelative(i, j, k);
                            pendingChecks.incrementAndGet();
                            regionScheduler.execute(plugin, toCheck.getLocation(), () -> {
                                if (!toCheck.isEmpty() || !toCheck.isPassable()) suffocation.set(true);
                                if (pendingChecks.decrementAndGet() == 0) future.complete(suffocation.get());
                            });
                        }
                    }
                }
            }
            default -> {
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        for (int k = 0; k < 2; k++) {
                            Block toCheck = spawn.getRelative(i, j, k);
                            pendingChecks.incrementAndGet();
                            regionScheduler.execute(plugin, toCheck.getLocation(), () -> {
                                if (!toCheck.isEmpty() || !toCheck.isPassable()) suffocation.set(true);
                                if (pendingChecks.decrementAndGet() == 0) future.complete(suffocation.get());
                            });
                        }
                    }
                }
            }
        }

        if (pendingChecks.get() == 0) {
            future.complete(false);
        }

        return future;
    }

    public static void createTeleportObject(Player player, Location to, Entity mount) {
        Object[] teleportInfo = new Object[2];
        teleportInfo[0] = to;
        teleportInfo[1] = mount;
        teleports.put(player.getName(), teleportInfo);
    }

    public static Object[] removeTeleportsKey(Player player) {
        return teleports.remove(player.getName());
    }
}
