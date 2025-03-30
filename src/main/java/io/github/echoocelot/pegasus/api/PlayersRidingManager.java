package io.github.echoocelot.pegasus.api;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class PlayersRidingManager {

    private static final Map<Player,Entity> playersRidingStuff = new HashMap<>();

    public static Boolean isPlayerRidingStuff(Player p) {
        return playersRidingStuff.containsKey(p);
    }

    public static Entity getMount(Player p) {
        return playersRidingStuff.get(p);
    }

    public static void addPlayerRidingStuff(Player p, Entity e) {
        playersRidingStuff.put(p, e);
    }

    public static void removePlayerRidingStuff(Player p) {
        playersRidingStuff.remove(p);
    }

}
