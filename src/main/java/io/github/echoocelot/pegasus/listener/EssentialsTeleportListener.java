package io.github.echoocelot.pegasus.listener;

import io.github.echoocelot.pegasus.Pegasus;
import io.github.echoocelot.pegasus.api.PegasusMessaging;
import io.github.echoocelot.pegasus.api.PlayersRidingManager;
import io.github.echoocelot.pegasus.api.TeleportManager;
import net.ess3.api.events.UserTeleportHomeEvent;
import net.ess3.api.events.UserWarpEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import net.ess3.api.events.teleport.PreTeleportEvent;

import java.util.Objects;

public class EssentialsTeleportListener implements Listener {

	@EventHandler
	public void onPlayerEssentialsTeleport(PreTeleportEvent event) {
		Player player = event.getTeleportee().getBase();
		Location to = event.getTarget().getLocation();

		if (player.hasPermission("pegasus.tp") && PlayersRidingManager.isPlayerRidingStuff(player)) {
			Object[] teleportInfo = TeleportManager.removeTeleportsKey(player);
			if (teleportInfo != null && Objects.equals(to.toString(), teleportInfo[0].toString())) {
				Entity mount = (Entity) teleportInfo[1];
				TeleportManager.tpMountAndPlayer(player, mount, to);
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerEssentialsTeleportHome(UserTeleportHomeEvent event) {
		Player player = event.getUser().getBase();
		Location to = event.getHomeLocation();

		if (player.hasPermission("pegasus.tp")) {
			if (PlayersRidingManager.isPlayerRidingStuff(player)) {
				Entity mount = PlayersRidingManager.getMount(player);

				if (TeleportManager.isCamelWithTwoPassengers(mount)) {
					PegasusMessaging.sendErrorMessage(player, "You cannot teleport with two passengers on a camel!");
					event.setCancelled(true);
				} else {
					TeleportManager.createTeleportObject(player, to, mount);
					TeleportManager.couldSuffocationOccur(mount, to).thenAccept(suffocating -> {
						if (suffocating) {
							PegasusMessaging.sendMessage(player, "Teleporting here could risk suffocation for you and your mount. Move to cancel this teleport.");
						}
					});
				}
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerEssentialsTeleportWarp(UserWarpEvent event) {
		Player player = event.getUser().getBase();
		String warpName = event.getWarp();
		FileConfiguration warpInfo = Pegasus.getWarpInfo(warpName);
		if (warpInfo == null) return;

		// get warp location
		double x = warpInfo.getDouble("x");
		double y = warpInfo.getDouble("y");
		double z = warpInfo.getDouble("z");
		float yaw = (float) warpInfo.getDouble("yaw");
		float pitch = (float) warpInfo.getDouble("pitch");
		String w = warpInfo.getString("world-name");
		if (w == null) return;
		World world = Bukkit.getWorld(w);
		Location to = new Location(world, x, y, z, yaw, pitch);

		if (player.hasPermission("pegasus.tp")) {
			if (PlayersRidingManager.isPlayerRidingStuff(player)) {
				Entity mount = PlayersRidingManager.getMount(player);

				if (TeleportManager.isCamelWithTwoPassengers(mount)) {
					PegasusMessaging.sendErrorMessage(player, "You cannot teleport with two passengers on a camel!");
					event.setCancelled(true);
				} else {
					TeleportManager.createTeleportObject(player, to, mount);
					TeleportManager.couldSuffocationOccur(mount, to).thenAccept(suffocating -> {
						if (suffocating) {
							PegasusMessaging.sendMessage(player, "Teleporting here could risk suffocation for you and your mount. Move to cancel this teleport.");
						}
					});
				}
			}
		}
	}
}
