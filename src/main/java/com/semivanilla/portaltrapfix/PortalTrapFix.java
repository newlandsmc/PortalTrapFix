package com.semivanilla.portaltrapfix;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class PortalTrapFix extends JavaPlugin implements Listener {
    private Map<UUID, Triplet<Long, Location, Location>> inPortal = new HashMap<>();
    private Map<UUID, Long> sendMessage = new HashMap<>();

    @Override
    public void onEnable() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            Iterator<Map.Entry<UUID, Long>> it = sendMessage.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<UUID, Long> entry = it.next();
                if (System.currentTimeMillis() >= entry.getValue()) {
                    it.remove();
                    Player player = Bukkit.getPlayer(entry.getKey());
                    if (player != null) {
                        player.sendMessage(ChatColor.RED + "Are you stuck? Sending you back in 5 seconds...");
                    }
                }
            }
            Iterator<Map.Entry<UUID, Triplet<Long, Location, Location>>> it1 = inPortal.entrySet().iterator();
            while (it1.hasNext()) {
                Map.Entry<UUID, Triplet<Long, Location, Location>> entry = it1.next();
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player == null) {
                    continue;
                }
                //check if player is in portal
                if (!isInPortal(player)) {
                    it1.remove();
                    continue;
                }

                if (System.currentTimeMillis() >= entry.getValue().getValue0()) {
                    it1.remove();
                    sendMessage.remove(entry.getKey());
                    getLogger().info("Sending " + player.getName() + " back to " + entry.getValue().getValue1().toString());
                    Bukkit.getScheduler().runTask(this, () -> player.teleport(entry.getValue().getValue1()));
                }
            }
        }, 0, 10);
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    public boolean isInPortal(Player player) {
        return player.getLocation().getBlock().getType() == Material.NETHER_PORTAL && player.getLocation().getBlock().getRelative(0, 1, 0).getType() == Material.NETHER_PORTAL;
    }

    @EventHandler
    public void onPortal(PlayerPortalEvent event) {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.NETHER_PORTAL)
            return;

        UUID uuid = event.getPlayer().getUniqueId();
        inPortal.put(uuid, new Triplet<>(System.currentTimeMillis() + 10000L, event.getFrom(), event.getTo()));
        sendMessage.put(uuid, System.currentTimeMillis() + 5000L);
    }
}
