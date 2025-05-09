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
		if (!(mount instanceof Boat || mount instanceof Minecart || mount instanceof ArmorStand)) {
			mount.getScheduler().execute(plugin, () -> {
				if (mount.getVehicle() != null) {
					// Prevent nested mounts (e.g. horse in a boat)
					PegasusMessaging.sendErrorMessage(player, "Teleport failed: Mount is inside another vehicle!");
					return;
				}

				EntityScheduler mountScheduler = mount.getScheduler();
				EntityScheduler playerScheduler = player.getScheduler();

				mountScheduler.execute(plugin, () -> {
					try {
						mount.eject();
					} catch (Exception e) {
						plugin.getLogger().warning("Failed to eject mount: " + e.getMessage());
						e.printStackTrace();
					}
				}, null, 5L);

				if (isFoliaEnabled()) {
					CompletableFuture<Boolean> mountTp = new CompletableFuture<>();
					CompletableFuture<Boolean> playerTp = new CompletableFuture<>();

					mountScheduler.execute(plugin, () -> {
						try {
							mount.teleportAsync(to).thenAccept(mountTp::complete);
						} catch (Exception e) {
							plugin.getLogger().warning("Mount teleport failed: " + e.getMessage());
							e.printStackTrace();
							mountTp.complete(false);
						}
					}, null, 7L);

					playerScheduler.execute(plugin, () -> {
						try {
							player.teleportAsync(to).thenAccept(playerTp::complete);
						} catch (Exception e) {
							plugin.getLogger().warning("Player teleport failed: " + e.getMessage());
							e.printStackTrace();
							playerTp.complete(false);
						}
					}, null, 7L);

					mountTp.thenCombine(playerTp, (mountSuccess, playerSuccess) -> {
						if (mountSuccess && playerSuccess) {
							mount.getScheduler().execute(plugin, () -> {
								try {
									mount.addPassenger(player);
								} catch (Exception e) {
									plugin.getLogger().warning("Failed to add passenger: " + e.getMessage());
									e.printStackTrace();
								}
							}, null, 5L);
						} else {
							plugin.getLogger().warning("Teleport failed — mountSuccess=" + mountSuccess + ", playerSuccess=" + playerSuccess);
						}
						return null;
					});
				} else {
					mountScheduler.execute(plugin, () -> {
						boolean mountTeleported = false;
						try {
							mountTeleported = mount.teleport(to);
						} catch (Exception e) {
							plugin.getLogger().warning("Mount teleport failed: " + e.getMessage());
							e.printStackTrace();
						}

						if (!mountTeleported) {
							plugin.getLogger().warning("Aborting teleport chain: mount teleport failed.");
							return;
						}

						playerScheduler.execute(plugin, () -> {
							boolean playerTeleported = false;
							try {
								playerTeleported = player.teleport(to);
							} catch (Exception e) {
								plugin.getLogger().warning("Player teleport failed after mount teleport: " + e.getMessage());
								e.printStackTrace();
							}

							if (!playerTeleported) {
								plugin.getLogger().warning("Aborting passenger addition: player teleport failed.");
								return;
							}

							mountScheduler.execute(plugin, () -> {
								try {
									mount.addPassenger(player);
								} catch (Exception e) {
									plugin.getLogger().warning("Failed to add passenger after teleport: " + e.getMessage());
									e.printStackTrace();
								}
							}, null, 6L);

						}, null, 4L);
					}, null, 2L);
				}
			}, null, 0L);
		}
	}


	public static Boolean isCamelWithTwoPassengers(Entity mount) {
		if (mount instanceof Camel) {
			List<Entity> passengers = mount.getPassengers();
			return passengers.size() == 2;
		} else return false;
	}

	public static CompletableFuture<Boolean> couldSuffocationOccur(Entity mount, Location to) {
		CompletableFuture<Boolean> future = new CompletableFuture<>();
		RegionScheduler regionScheduler = Bukkit.getRegionScheduler();
		Block spawn = to.getBlock();
		AtomicInteger pendingChecks = new AtomicInteger(0);
		AtomicReference<Boolean> suffocation = new AtomicReference<>(false);

		if (mount instanceof Boat || mount instanceof Minecart || mount instanceof ArmorStand) {
			// will not tp them with boat/minecart/armor stand, so don't give any warning
			future.complete(false);
			return future;
		}

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
