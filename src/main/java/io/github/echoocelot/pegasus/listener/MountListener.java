package io.github.echoocelot.pegasus.listener;

import io.github.echoocelot.pegasus.api.PlayersRidingManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityMountEvent;

public class MountListener implements Listener {
    @EventHandler
    public void onEntityMount(EntityMountEvent event) {
        Entity entity = event.getEntity();
        if(entity.getType() == EntityType.PLAYER) {
            Player player = (Player) entity;
            if(player.hasPermission("pegasus.tp")) {
                PlayersRidingManager.addPlayerRidingStuff(player, event.getMount());
            }
        }
    }
}
