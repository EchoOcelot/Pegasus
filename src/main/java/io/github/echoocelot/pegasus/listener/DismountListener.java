package io.github.echoocelot.pegasus.listener;

import io.github.echoocelot.pegasus.Pegasus;
import io.github.echoocelot.pegasus.api.PlayersRidingManager;
import io.github.echoocelot.pegasus.api.TeleportManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class DismountListener implements Listener {
	@NotNull
	private static final Pegasus plugin = JavaPlugin.getPlugin(Pegasus.class);
	@EventHandler()
	public void onEntityDismount(EntityDismountEvent event) {
		Entity entity = event.getEntity();
		if (entity.getType() == EntityType.PLAYER) {
			Player player = (Player) entity;
			if (player.hasPermission("pegasus.tp")) {
				PlayersRidingManager.removePlayerRidingStuff(player);
				
				Bukkit.getServer().getRegionScheduler().runDelayed(
					plugin,
					player.getLocation(),
					(task) -> TeleportManager.removeTeleportsKey(player),
					2L
				);
			}
		}
	}

}
