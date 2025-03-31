package io.github.echoocelot.pegasus.listener;

import com.palmergames.bukkit.towny.event.NationSpawnEvent;
import com.palmergames.bukkit.towny.event.TownSpawnEvent;
import com.palmergames.bukkit.towny.event.teleport.ResidentSpawnEvent;
import com.palmergames.bukkit.towny.event.teleport.SuccessfulTownyTeleportEvent;
import io.github.echoocelot.pegasus.api.PegasusMessaging;
import io.github.echoocelot.pegasus.api.PlayersRidingManager;
import io.github.echoocelot.pegasus.api.TeleportManager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;

public class TownyTeleportListener implements Listener {

    @EventHandler
    public void onFinishedTownySpawn(SuccessfulTownyTeleportEvent event) {
        Player player = event.getResident().getPlayer();

        if (player == null) {
            Bukkit.getConsoleSender().sendMessage("Player was null in SuccessfulTownyTeleportEvent.");
            return;
        }
        if (player.hasPermission("pegasus.tp")) {
            Object[] teleportInfo = TeleportManager.removeTeleportsKey(player);
            if (teleportInfo != null && Objects.equals(event.getTeleportLocation().toString(), teleportInfo[0].toString())) {
                Entity mount = (Entity) teleportInfo[1];
                TeleportManager.tpMountAndPlayer(player, mount, event.getTeleportLocation());
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFirstRunTownSpawn(TownSpawnEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();

        if (player.hasPermission("pegasus.tp") && PlayersRidingManager.isPlayerRidingStuff(player)) {
            Entity mount = PlayersRidingManager.getMount(player);

            TeleportManager.createTeleportObject(player, to, mount);
            TeleportManager.couldSuffocationOccur(mount, to).thenAccept(suffocating -> {
                if (suffocating) {
                    PegasusMessaging.sendMessage(player, "Teleporting here could risk suffocation for you and your mount. Move to cancel this teleport.");
                }
            });
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFirstRunNationSpawn(NationSpawnEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();
        if (player.hasPermission("pegasus.tp") && PlayersRidingManager.isPlayerRidingStuff(player)) {
            Entity mount = PlayersRidingManager.getMount(player);

            TeleportManager.createTeleportObject(player, to, mount);
            TeleportManager.couldSuffocationOccur(mount, to).thenAccept(suffocating -> {
                if (suffocating) {
                    PegasusMessaging.sendMessage(player, "Teleporting here could risk suffocation for you and your mount. Move to cancel this teleport.");
                }
            });
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFirstRunResidentSpawn(ResidentSpawnEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();
        if (player.hasPermission("pegasus.tp") && PlayersRidingManager.isPlayerRidingStuff(player)) {
            Entity mount = PlayersRidingManager.getMount(player);

            TeleportManager.createTeleportObject(player, to, mount);
            TeleportManager.couldSuffocationOccur(mount, to).thenAccept(suffocating -> {
                if (suffocating) {
                    PegasusMessaging.sendMessage(player, "Teleporting here could risk suffocation for you and your mount. Move to cancel this teleport.");
                }
            });
        }
    }
}