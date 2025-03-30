package io.github.echoocelot.pegasus.listener;

import io.github.echoocelot.pegasus.api.PlayersRidingManager;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDismountEvent;

public class DismountListener implements Listener {
    @EventHandler()
    public void onEntityDismount(EntityDismountEvent event) {
        Entity entity = event.getEntity();
        if(entity.getType() == EntityType.PLAYER) {
            Player player = (Player) entity;
            if(player.hasPermission("pegasus.tp")) {
                PlayersRidingManager.removePlayerRidingStuff(player);
            }
        }
    }
}
