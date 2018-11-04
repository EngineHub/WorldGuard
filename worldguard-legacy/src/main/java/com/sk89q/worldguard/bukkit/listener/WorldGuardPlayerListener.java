/*
 * WorldGuard, a suite of tools for Minecraft
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldGuard team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldguard.bukkit.listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.gamemode.GameMode;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.BukkitWorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.event.player.ProcessPlayerEvent;
import com.sk89q.worldguard.bukkit.util.Events;
import com.sk89q.worldguard.config.ConfigurationManager;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.GameModeFlag;
import com.sk89q.worldguard.util.command.CommandFilter;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TravelAgent;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;

import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

/**
 * Handles all events thrown in relation to a player.
 */
public class WorldGuardPlayerListener implements Listener {

    private static final Logger log = Logger.getLogger(WorldGuardPlayerListener.class.getCanonicalName());
    private static final Pattern opPattern = Pattern.compile("^/(?:minecraft:)(?:bukkit:)?op(?:\\s.*)?$", Pattern.CASE_INSENSITIVE);
    private WorldGuardPlugin plugin;

    /**
     * Construct the object;
     *
     * @param plugin
     */
    public WorldGuardPlayerListener(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Register events.
     */
    public void registerEvents() {
        PluginManager pm = plugin.getServer().getPluginManager();
        pm.registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        LocalPlayer localPlayer = plugin.wrapPlayer(player);
        BukkitWorldConfiguration wcfg =
                (BukkitWorldConfiguration) WorldGuard.getInstance().getPlatform().getGlobalStateManager().get(localPlayer.getWorld());
        Session session = WorldGuard.getInstance().getPlatform().getSessionManager().getIfPresent(localPlayer);
        if (session != null) {
            GameModeFlag handler = session.getHandler(GameModeFlag.class);
            if (handler != null && wcfg.useRegions && !WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(localPlayer,
                    localPlayer.getWorld())) {
                GameMode expected = handler.getSetGameMode();
                if (handler.getOriginalGameMode() != null && expected != null && expected != BukkitAdapter.adapt(event.getNewGameMode())) {
                    log.info("Game mode change on " + player.getName() + " has been blocked due to the region GAMEMODE flag");
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        LocalPlayer localPlayer = plugin.wrapPlayer(player);
        World world = player.getWorld();

        ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
        BukkitWorldConfiguration wcfg = (BukkitWorldConfiguration) cfg.get(localPlayer.getWorld());

        if (cfg.activityHaltToggle) {
            player.sendMessage(ChatColor.YELLOW
                    + "Intensive server activity has been HALTED.");

            int removed = 0;

            for (Entity entity : world.getEntities()) {
                if (BukkitUtil.isIntensiveEntity(entity)) {
                    entity.remove();
                    removed++;
                }
            }

            if (removed > 10) {
                log.info("Halt-Act: " + removed + " entities (>10) auto-removed from "
                        + player.getWorld().toString());
            }
        }

        if (wcfg.fireSpreadDisableToggle) {
            player.sendMessage(ChatColor.YELLOW
                    + "Fire spread is currently globally disabled for this world.");
        }

        Events.fire(new ProcessPlayerEvent(player));

        WorldGuard.getInstance().getPlatform().getSessionManager().get(localPlayer); // Initializes a session
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        LocalPlayer localPlayer = plugin.wrapPlayer(player);
        BukkitWorldConfiguration wcfg =
                (BukkitWorldConfiguration) WorldGuard.getInstance().getPlatform().getGlobalStateManager().get(localPlayer.getWorld());
        if (wcfg.useRegions) {
            if (!StateFlag.test(WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().queryState(localPlayer.getLocation(), localPlayer, Flags.SEND_CHAT))) {
                player.sendMessage(ChatColor.RED + "You don't have permission to chat in this region!");
                event.setCancelled(true);
                return;
            }

            for (Iterator<Player> i = event.getRecipients().iterator(); i.hasNext();) {
                Player rPlayer = i.next();
                LocalPlayer rLocal = plugin.wrapPlayer(rPlayer);
                if (!StateFlag.test(WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().queryState(rLocal.getLocation(), rLocal, Flags.RECEIVE_CHAT))) {
                    i.remove();
                }
            }
            if (event.getRecipients().size() == 0) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();

        String hostKey = cfg.hostKeys.get(player.getUniqueId().toString());
        if (hostKey == null) {
            hostKey = cfg.hostKeys.get(player.getName().toLowerCase());
        }

        if (hostKey != null) {
            String hostname = event.getHostname();
            int colonIndex = hostname.indexOf(':');
            if (colonIndex != -1) {
                hostname = hostname.substring(0, colonIndex);
            }

            if (!hostname.equals(hostKey)
                    && !(cfg.hostKeysAllowFMLClients && hostname.equals(hostKey + "\u0000FML\u0000"))) {
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER,
                        "You did not join with the valid host key!");
                log.warning("WorldGuard host key check: " +
                        player.getName() + " joined with '" + hostname +
                        "' but '" + hostKey + "' was expected. Kicked!");
                return;
            }
        }

        if (cfg.deopOnJoin) {
            player.setOp(false);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            handleBlockRightClick(event);
        } else if (event.getAction() == Action.PHYSICAL) {
            handlePhysicalInteract(event);
        }

        ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
        BukkitWorldConfiguration wcfg = (BukkitWorldConfiguration) cfg.get(BukkitAdapter.adapt(world));

        if (wcfg.removeInfiniteStacks
                && !plugin.hasPermission(player, "worldguard.override.infinite-stack")) {
            int slot = player.getInventory().getHeldItemSlot();
            ItemStack heldItem = player.getInventory().getItem(slot);
            if (heldItem != null && heldItem.getAmount() < 0) {
                player.getInventory().setItem(slot, null);
                player.sendMessage(ChatColor.RED + "Infinite stack removed.");
            }
        }
    }

    /**
     * Called when a player right clicks a block.
     *
     * @param event Thrown event
     */
    private void handleBlockRightClick(PlayerInteractEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Block block = event.getClickedBlock();
        World world = block.getWorld();
        Material type = block.getType();
        Player player = event.getPlayer();
        @Nullable ItemStack item = event.getItem();

        ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
        BukkitWorldConfiguration wcfg = (BukkitWorldConfiguration) cfg.get(BukkitAdapter.adapt(world));

        // Infinite stack removal
        if ((type == Material.CHEST
                || type == Material.JUKEBOX
                || type == Material.DISPENSER
                || type == Material.FURNACE
                || type == Material.DROPPER
                || type == Material.BREWING_STAND
                || type == Material.TRAPPED_CHEST
                || type == Material.ENCHANTING_TABLE)
                && wcfg.removeInfiniteStacks
                && !plugin.hasPermission(player, "worldguard.override.infinite-stack")) {
            for (int slot = 0; slot < 40; slot++) {
                ItemStack heldItem = player.getInventory().getItem(slot);
                if (heldItem != null && heldItem.getAmount() < 0) {
                    player.getInventory().setItem(slot, null);
                    player.sendMessage(ChatColor.RED + "Infinite stack in slot #" + slot + " removed.");
                }
            }
        }

        if (wcfg.useRegions) {
            //Block placedIn = block.getRelative(event.getBlockFace());
            ApplicableRegionSet set =
                    WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().getApplicableRegions(BukkitAdapter.adapt(block.getLocation()));
            //ApplicableRegionSet placedInSet = plugin.getRegionContainer().createQuery().getApplicableRegions(placedIn.getLocation());
            LocalPlayer localPlayer = plugin.wrapPlayer(player);

            if (item != null && item.getType().getKey().toString().equals(wcfg.regionWand) && plugin.hasPermission(player, "worldguard.region.wand")) {
                if (set.size() > 0) {
                    player.sendMessage(ChatColor.YELLOW + "Can you build? " + (set.testState(localPlayer, Flags.BUILD) ? "Yes" : "No"));

                    StringBuilder str = new StringBuilder();
                    for (Iterator<ProtectedRegion> it = set.iterator(); it.hasNext();) {
                        str.append(it.next().getId());
                        if (it.hasNext()) {
                            str.append(", ");
                        }
                    }

                    localPlayer.print("Applicable regions: " + str.toString());
                } else {
                    localPlayer.print("WorldGuard: No defined regions here!");
                }

                event.setCancelled(true);
            }
        }
    }

    /**
     * Called when a player steps on a pressure plate or tramples crops.
     *
     * @param event Thrown event
     */
    private void handlePhysicalInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        Block block = event.getClickedBlock(); //not actually clicked but whatever
        //int type = block.getTypeId();
        World world = player.getWorld();

        ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
        BukkitWorldConfiguration wcfg = (BukkitWorldConfiguration) cfg.get(BukkitAdapter.adapt(world));

        if (block.getType() == Material.FARMLAND && wcfg.disablePlayerCropTrampling) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        LocalPlayer localPlayer = plugin.wrapPlayer(player);

        ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
        BukkitWorldConfiguration wcfg = (BukkitWorldConfiguration) cfg.get(localPlayer.getWorld());

        if (wcfg.useRegions) {
            ApplicableRegionSet set =
                    WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().getApplicableRegions(localPlayer.getLocation());

            com.sk89q.worldedit.util.Location spawn = set.queryValue(localPlayer, Flags.SPAWN_LOC);

            if (spawn != null) {
                event.setRespawnLocation(BukkitAdapter.adapt(spawn));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onItemHeldChange(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();

        ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
        BukkitWorldConfiguration wcfg = (BukkitWorldConfiguration) cfg.get(BukkitAdapter.adapt(player.getWorld()));

        if (wcfg.removeInfiniteStacks
                && !plugin.hasPermission(player, "worldguard.override.infinite-stack")) {
            int newSlot = event.getNewSlot();
            ItemStack heldItem = player.getInventory().getItem(newSlot);
            if (heldItem != null && heldItem.getAmount() < 0) {
                player.getInventory().setItem(newSlot, null);
                player.sendMessage(ChatColor.RED + "Infinite stack removed.");
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        World world = event.getFrom().getWorld();
        Player player = event.getPlayer();
        LocalPlayer localPlayer = plugin.wrapPlayer(player);
        ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
        BukkitWorldConfiguration wcfg = (BukkitWorldConfiguration) cfg.get(localPlayer.getWorld());

        if (wcfg.useRegions) {
            ApplicableRegionSet set =
                    WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().getApplicableRegions(BukkitAdapter.adapt(event.getTo()));
            ApplicableRegionSet setFrom =
                    WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().getApplicableRegions(BukkitAdapter.adapt(event.getFrom()));

            if (cfg.usePlayerTeleports) {
                if (null != WorldGuard.getInstance().getPlatform().getSessionManager().get(localPlayer).testMoveTo(localPlayer,
                        BukkitAdapter.adapt(event.getTo()),
                        MoveType.TELEPORT)) {
                    event.setCancelled(true);
                    return;
                }
            }

            if (event.getCause() == TeleportCause.ENDER_PEARL) {
                if (!WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(localPlayer, localPlayer.getWorld())
                        && !(set.testState(localPlayer, Flags.ENDERPEARL)
                                && setFrom.testState(localPlayer, Flags.ENDERPEARL))) {
                    player.sendMessage(ChatColor.DARK_RED + "You're not allowed to go there.");
                    event.setCancelled(true);
                    return;
                }
            }
            try {
                if (event.getCause() == TeleportCause.CHORUS_FRUIT) {
                    if (!WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(localPlayer, localPlayer.getWorld())) {
                        boolean allowFrom = setFrom.testState(localPlayer, Flags.CHORUS_TELEPORT);
                        boolean allowTo = set.testState(localPlayer, Flags.CHORUS_TELEPORT);
                        if (!allowFrom || !allowTo) {
                            player.sendMessage(ChatColor.DARK_RED + "You're not allowed to teleport " +
                                    (!allowFrom ? "from here." : "there."));
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            } catch (NoSuchFieldError ignored) {}
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (event.getTo() == null) { // apparently this counts as a cancelled event, implementation specific though
            return;
        }
        ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
        LocalPlayer localPlayer = plugin.wrapPlayer(event.getPlayer());
        BukkitWorldConfiguration wcfg = (BukkitWorldConfiguration) cfg.get(BukkitAdapter.adapt(event.getTo().getWorld()));
        if (!wcfg.regionNetherPortalProtection) return;
        if (event.getCause() != TeleportCause.NETHER_PORTAL) {
            return;
        }
        if (!event.useTravelAgent()) { // either end travel (even though we checked cause) or another plugin is fucking with us, shouldn't create a portal though
            return;
        }
        TravelAgent pta = event.getPortalTravelAgent();
        if (pta == null) { // possible, but shouldn't create a portal
            return;
        }
        if (pta.findPortal(event.getTo()) != null) {
            return; // portal exists...it shouldn't make a new one
        }
        // HOPEFULLY covered everything the server can throw at us...proceed protection checking
        // a lot of that is implementation specific though

        // hackily estimate the area that could be effected by this event, since the server refuses to tell us
        int radius = pta.getCreationRadius();
        Location min = event.getTo().clone().subtract(radius, radius, radius);
        Location max = event.getTo().clone().add(radius, radius, radius);
        com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(event.getTo().getWorld());

        ProtectedRegion check = new ProtectedCuboidRegion("__portalcheck__", BukkitAdapter.adapt(min.getBlock().getLocation()).toVector().toBlockPoint(),
                BukkitAdapter.adapt(max.getBlock().getLocation()).toVector().toBlockPoint());

        if (wcfg.useRegions && !WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(localPlayer, world)) {
            RegionManager mgr = WorldGuard.getInstance().getPlatform().getRegionContainer().get(world);
            if (mgr == null) return;
            ApplicableRegionSet set = mgr.getApplicableRegions(check);
            if (!set.testState(plugin.wrapPlayer(event.getPlayer()), Flags.BUILD)) {
                event.getPlayer().sendMessage(ChatColor.RED + "Destination is in a protected area.");
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        LocalPlayer localPlayer = plugin.wrapPlayer(player);
        ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
        BukkitWorldConfiguration wcfg = (BukkitWorldConfiguration) cfg.get(localPlayer.getWorld());

        if (wcfg.useRegions && !WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(localPlayer, localPlayer.getWorld())) {
            ApplicableRegionSet set =
                    WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().getApplicableRegions(localPlayer.getLocation());

            Set<String> allowedCommands = set.queryValue(localPlayer, Flags.ALLOWED_CMDS);
            Set<String> blockedCommands = set.queryValue(localPlayer, Flags.BLOCKED_CMDS);
            CommandFilter test = new CommandFilter(allowedCommands, blockedCommands);

            if (!test.apply(event.getMessage())) {
                player.sendMessage(ChatColor.RED + event.getMessage() + " is not allowed in this area.");
                event.setCancelled(true);
                return;
            }
        }

        if (cfg.blockInGameOp) {
            if (opPattern.matcher(event.getMessage()).matches()) {
                player.sendMessage(ChatColor.RED + "/op can only be used in console (as set by a WG setting).");
                event.setCancelled(true);
                return;
            }
        }
    }
}
