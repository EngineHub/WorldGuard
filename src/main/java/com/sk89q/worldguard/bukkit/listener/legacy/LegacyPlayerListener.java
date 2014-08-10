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

package com.sk89q.worldguard.bukkit.listener.legacy;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.blocks.ItemID;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.blacklist.event.ItemAcquireBlacklistEvent;
import com.sk89q.worldguard.blacklist.event.ItemUseBlacklistEvent;
import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.ConfigurationManager;
import com.sk89q.worldguard.bukkit.FlagStateManager.PlayerFlagState;
import com.sk89q.worldguard.bukkit.WorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.util.command.CommandFilter;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;

import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Pattern;

import static com.sk89q.worldguard.bukkit.BukkitUtil.createTarget;
import static com.sk89q.worldguard.bukkit.BukkitUtil.toVector;

/**
 * Handles all events thrown in relation to a player.
 */
public class LegacyPlayerListener implements Listener {

    private Pattern opPattern = Pattern.compile("^/op(?:\\s.*)?$", Pattern.CASE_INSENSITIVE);
    private WorldGuardPlugin plugin;

    /**
     * Construct the object;
     *
     * @param plugin
     */
    public LegacyPlayerListener(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Register events.
     */
    public void registerEvents() {
        final PluginManager pm = plugin.getServer().getPluginManager();
        pm.registerEvents(this, plugin);

        if (plugin.getGlobalStateManager().usePlayerMove) {
            pm.registerEvents(new PlayerMoveHandler(), plugin);
        }
    }

    /**
     * Handles movement related events, including changing gamemode, sending
     * greeting/farewell messages, etc.
     * A reference to WorldGuardPlugin is required to keep this method static
     * although WGBukkit.getPlugin() may be used.
     * @return true if the movement should not be allowed
     */
    public static boolean checkMove(WorldGuardPlugin plugin, Player player, Location from, Location to) {
        PlayerFlagState state = plugin.getFlagStateManager().getState(player);

        //Flush states in multiworld scenario
        if (state.lastWorld != null && !state.lastWorld.equals(to.getWorld())) {
            plugin.getFlagStateManager().forget(player);
            state = plugin.getFlagStateManager().getState(player);
        }

        World world = from.getWorld();
        World toWorld = to.getWorld();

        LocalPlayer localPlayer = plugin.wrapPlayer(player);
        boolean hasBypass = plugin.getGlobalRegionManager().hasBypass(player, world);
        boolean hasRemoteBypass;
        if (world.equals(toWorld)) {
            hasRemoteBypass = hasBypass;
        } else {
            hasRemoteBypass = plugin.getGlobalRegionManager().hasBypass(player, toWorld);
        }

        RegionManager mgr = plugin.getGlobalRegionManager().get(toWorld);
        Vector pt = new Vector(to.getBlockX(), to.getBlockY(), to.getBlockZ());
        ApplicableRegionSet set = mgr.getApplicableRegions(pt);

        boolean entryAllowed = set.allows(DefaultFlag.ENTRY, localPlayer);
        if (!hasRemoteBypass && (!entryAllowed /*|| regionFull*/)) {
            String message = /*maxPlayerMessage != null ? maxPlayerMessage :*/ "You are not permitted to enter this area.";

            player.sendMessage(ChatColor.DARK_RED + message);
            return true;
        }

        // Have to set this state
        if (state.lastExitAllowed == null) {
            state.lastExitAllowed = plugin.getGlobalRegionManager().get(world)
                        .getApplicableRegions(toVector(from))
                        .allows(DefaultFlag.EXIT, localPlayer);
        }

        boolean exitAllowed = set.allows(DefaultFlag.EXIT, localPlayer);
        if (!hasBypass && exitAllowed && !state.lastExitAllowed) {
            player.sendMessage(ChatColor.DARK_RED + "You are not permitted to leave this area.");
            return true;
        }

//        WorldGuardRegionMoveEvent event = new WorldGuardRegionMoveEvent(plugin, player, state, set, from, to);
//        Bukkit.getPluginManager().callEvent(event);

        String greeting = set.getFlag(DefaultFlag.GREET_MESSAGE);//, localPlayer);
        String farewell = set.getFlag(DefaultFlag.FAREWELL_MESSAGE);//, localPlayer);
        Boolean notifyEnter = set.getFlag(DefaultFlag.NOTIFY_ENTER);//, localPlayer);
        Boolean notifyLeave = set.getFlag(DefaultFlag.NOTIFY_LEAVE);//, localPlayer);
        GameMode gameMode = set.getFlag(DefaultFlag.GAME_MODE);

        if (state.lastFarewell != null && (farewell == null
                || !state.lastFarewell.equals(farewell))) {
            String replacedFarewell = plugin.replaceMacros(
                    player, BukkitUtil.replaceColorMacros(state.lastFarewell));
            player.sendMessage(replacedFarewell.replaceAll("\\\\n", "\n").split("\\n"));
        }

        if (greeting != null && (state.lastGreeting == null
                || !state.lastGreeting.equals(greeting))) {
            String replacedGreeting = plugin.replaceMacros(
                    player, BukkitUtil.replaceColorMacros(greeting));
            player.sendMessage(replacedGreeting.replaceAll("\\\\n", "\n").split("\\n"));
        }

        if ((notifyLeave == null || !notifyLeave)
                && state.notifiedForLeave != null && state.notifiedForLeave) {
            plugin.broadcastNotification(ChatColor.GRAY + "WG: "
                    + ChatColor.LIGHT_PURPLE + player.getName()
                    + ChatColor.GOLD + " left NOTIFY region");
        }

        if (notifyEnter != null && notifyEnter && (state.notifiedForEnter == null
                || !state.notifiedForEnter)) {
            StringBuilder regionList = new StringBuilder();

            for (ProtectedRegion region : set) {
                if (regionList.length() != 0) {
                    regionList.append(", ");
                }
                regionList.append(region.getId());
            }

            plugin.broadcastNotification(ChatColor.GRAY + "WG: "
                    + ChatColor.LIGHT_PURPLE + player.getName()
                    + ChatColor.GOLD + " entered NOTIFY region: "
                    + ChatColor.WHITE
                    + regionList);
        }

        if (!hasBypass && gameMode != null) {
            if (player.getGameMode() != gameMode) {
                state.lastGameMode = player.getGameMode();
                player.setGameMode(gameMode);
            } else if (state.lastGameMode == null) {
                state.lastGameMode = player.getServer().getDefaultGameMode();
            }
        } else {
            if (state.lastGameMode != null) {
                GameMode mode = state.lastGameMode;
                state.lastGameMode = null;
                player.setGameMode(mode);
            }
        }

        state.lastGreeting = greeting;
        state.lastFarewell = farewell;
        state.notifiedForEnter = notifyEnter;
        state.notifiedForLeave = notifyLeave;
        state.lastExitAllowed = exitAllowed;
        state.lastWorld = to.getWorld();
        state.lastBlockX = to.getBlockX();
        state.lastBlockY = to.getBlockY();
        state.lastBlockZ = to.getBlockZ();
        return false;
    }

    class PlayerMoveHandler implements Listener {
        @EventHandler(priority = EventPriority.HIGH)
        public void onPlayerMove(PlayerMoveEvent event) {
            Player player = event.getPlayer();
            World world = player.getWorld();

            ConfigurationManager cfg = plugin.getGlobalStateManager();
            WorldConfiguration wcfg = cfg.get(world);

            if (player.getVehicle() != null) {
                return; // handled in vehicle listener
            }
            if (wcfg.useRegions) {
                // Did we move a block?
                if (event.getFrom().getBlockX() != event.getTo().getBlockX()
                        || event.getFrom().getBlockY() != event.getTo().getBlockY()
                        || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
                    boolean result = checkMove(plugin, player, event.getFrom(), event.getTo());
                    if (result) {
                        Location newLoc = event.getFrom();
                        newLoc.setX(newLoc.getBlockX() + 0.5);
                        newLoc.setY(newLoc.getBlockY());
                        newLoc.setZ(newLoc.getBlockZ() + 0.5);
                        event.setTo(newLoc);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        WorldConfiguration wcfg = plugin.getGlobalStateManager().get(player.getWorld());
        if (wcfg.useRegions && !plugin.getGlobalRegionManager().hasBypass(player, player.getWorld())) {
            GameMode gameMode = plugin.getGlobalRegionManager().get(player.getWorld())
                    .getApplicableRegions(player.getLocation()).getFlag(DefaultFlag.GAME_MODE);
            if (plugin.getFlagStateManager().getState(player).lastGameMode != null
                    && gameMode != null && event.getNewGameMode() != gameMode) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);

        if (wcfg.fireSpreadDisableToggle) {
            player.sendMessage(ChatColor.YELLOW
                    + "Fire spread is currently globally disabled for this world.");
        }

        if (!cfg.hasCommandBookGodMode() && cfg.autoGodMode && (plugin.inGroup(player, "wg-invincible")
                || plugin.hasPermission(player, "worldguard.auto-invincible"))) {
            plugin.getLogger().log(Level.INFO, "Enabled auto-god mode for " + player.getName());
            cfg.enableGodMode(player);
        }

        if (plugin.inGroup(player, "wg-amphibious")) {
            plugin.getLogger().log(Level.INFO, "Enabled no-drowning mode for " + player.getName() + " (player is in group 'wg-amphibious')");
            cfg.enableAmphibiousMode(player);
        }

        if (wcfg.useRegions) {
            PlayerFlagState state = plugin.getFlagStateManager().getState(player);
            Location loc = player.getLocation();
            state.lastWorld = loc.getWorld();
            state.lastBlockX = loc.getBlockX();
            state.lastBlockY = loc.getBlockY();
            state.lastBlockZ = loc.getBlockZ();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        WorldConfiguration wcfg = plugin.getGlobalStateManager().get(player.getWorld());
        if (wcfg.useRegions) {
            if (!plugin.getGlobalRegionManager().allows(DefaultFlag.SEND_CHAT, player.getLocation())) {
                player.sendMessage(ChatColor.RED + "You don't have permission to chat in this region!");
                event.setCancelled(true);
                return;
            }

            for (Iterator<Player> i = event.getRecipients().iterator(); i.hasNext();) {
                if (!plugin.getGlobalRegionManager().allows(DefaultFlag.RECEIVE_CHAT, i.next().getLocation())) {
                    i.remove();
                }
            }
            if (event.getRecipients().isEmpty()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        ConfigurationManager cfg = plugin.getGlobalStateManager();

        String hostKey = cfg.hostKeys.get(player.getName().toLowerCase());
        if (hostKey != null) {
            String hostname = event.getHostname();
            int colonIndex = hostname.indexOf(':');
            if (colonIndex != -1) {
                hostname = hostname.substring(0, colonIndex);
            }

            if (!hostname.equals(hostKey)) {
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER,
                        "You did not join with the valid host key!");
                plugin.getLogger().warning("WorldGuard host key check: " +
                        player.getName() + " joined with '" + hostname +
                        "' but '" + hostKey + "' was expected. Kicked!");
                return;
            }
        }

        if (cfg.deopOnJoin) {
            player.setOp(false);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);

        // This is to make the enter/exit flags accurate -- move events are not
        // sent constantly, so it is possible to move just a little enough to
        // not trigger the event and then rejoin so that you are then considered
        // outside the border. This should work around that.
        if (wcfg.useRegions) {
            boolean hasBypass = plugin.getGlobalRegionManager().hasBypass(player, world);
            PlayerFlagState state = plugin.getFlagStateManager().getState(player);

            if (state.lastWorld != null && !hasBypass) {
                LocalPlayer localPlayer = plugin.wrapPlayer(player);
                RegionManager mgr = plugin.getGlobalRegionManager().get(world);
                Location loc = player.getLocation();
                Vector pt = new Vector(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                ApplicableRegionSet set = mgr.getApplicableRegions(pt);

                if (state.lastExitAllowed == null) {
                    state.lastExitAllowed = set.allows(DefaultFlag.EXIT, localPlayer);
                }

                if (!state.lastExitAllowed || !set.allows(DefaultFlag.ENTRY, localPlayer)) {
                    // Only if we have the last location cached
                    if (state.lastWorld.equals(world)) {
                        Location newLoc = new Location(world, state.lastBlockX + 0.5,
                                state.lastBlockY, state.lastBlockZ + 0.5);
                        player.teleport(newLoc);
                    }
                }
            }
        }

        cfg.forgetPlayer(plugin.wrapPlayer(player));
        plugin.forgetPlayer(player);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        ItemStack item = event.getItem();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            handleBlockRightClick(event);
        }

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);

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
        int type = block.getTypeId();
        Player player = event.getPlayer();
        ItemStack item = player.getItemInHand();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);

        // Infinite stack removal
        if ((type == BlockID.CHEST
                || type == BlockID.JUKEBOX
                || type == BlockID.DISPENSER
                || type == BlockID.FURNACE
                || type == BlockID.BURNING_FURNACE
                || type == BlockID.BREWING_STAND
                || type == BlockID.ENCHANTMENT_TABLE)
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
            Vector pt = toVector(block);
            RegionManager mgr = plugin.getGlobalRegionManager().get(world);
            Block placedIn = block.getRelative(event.getBlockFace());
            ApplicableRegionSet set = mgr.getApplicableRegions(pt);
            ApplicableRegionSet placedInSet = mgr.getApplicableRegions(placedIn.getLocation());
            LocalPlayer localPlayer = plugin.wrapPlayer(player);

            if (item.getTypeId() == wcfg.regionWand && plugin.hasPermission(player, "worldguard.region.wand")) {
                if (set.size() > 0) {
                    player.sendMessage(ChatColor.YELLOW + "Can you build? "
                            + (set.canBuild(localPlayer) ? "Yes" : "No"));

                    StringBuilder str = new StringBuilder();
                    for (Iterator<ProtectedRegion> it = set.iterator(); it.hasNext(); ) {
                        str.append(it.next().getId());
                        if (it.hasNext()) {
                            str.append(", ");
                        }
                    }

                    player.sendMessage(ChatColor.YELLOW + "Applicable regions: " + str.toString());
                } else {
                    player.sendMessage(ChatColor.YELLOW + "WorldGuard: No defined regions here!");
                }

                event.setCancelled(true);
                return;
            }

            if (BlockType.isRailBlock(type)
                    && (item.getTypeId() == ItemID.MINECART
                    || item.getTypeId() == ItemID.POWERED_MINECART
                    || item.getTypeId() == ItemID.STORAGE_MINECART
                    || item.getTypeId() == ItemID.TNT_MINECART
                    || item.getTypeId() == ItemID.HOPPER_MINECART)) {
                if (!plugin.getGlobalRegionManager().hasBypass(player, world)
                        && !placedInSet.canBuild(localPlayer)
                        && !placedInSet.allows(DefaultFlag.PLACE_VEHICLE, localPlayer)) {
                    player.sendMessage(ChatColor.DARK_RED + "You don't have permission to place vehicles here.");
                    event.setUseItemInHand(Result.DENY);
                    event.setCancelled(true);
                    return;
                }
            }

            if (item.getTypeId() == ItemID.WOOD_BOAT) {
                if (!plugin.getGlobalRegionManager().hasBypass(player, world)
                        && !placedInSet.canBuild(localPlayer)
                        && !placedInSet.allows(DefaultFlag.PLACE_VEHICLE, localPlayer)) {
                    player.sendMessage(ChatColor.DARK_RED + "You don't have permission to place vehicles here.");
                    event.setUseItemInHand(Result.DENY);
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getPlayer().getWorld());
        Player player = event.getPlayer();

        if (wcfg.useRegions) {
            if (!plugin.getGlobalRegionManager().hasBypass(player, player.getWorld())
                    && !plugin.getGlobalRegionManager().allows(DefaultFlag.ITEM_DROP, player.getLocation())) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You don't have permission to do that in this area.");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getPlayer().getWorld());

        if (wcfg.getBlacklist() != null) {
            Item ci = event.getItem();

            if (!wcfg.getBlacklist().check(
                    new ItemAcquireBlacklistEvent(plugin.wrapPlayer(event.getPlayer()),
                            toVector(ci.getLocation()), createTarget(ci.getItemStack())), false, true)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(player.getWorld());

        if (wcfg.useRegions) {
            Vector pt = toVector(location);
            RegionManager mgr = plugin.getGlobalRegionManager().get(player.getWorld());
            ApplicableRegionSet set = mgr.getApplicableRegions(pt);

            LocalPlayer localPlayer = plugin.wrapPlayer(player);
            com.sk89q.worldedit.Location spawn = set.getFlag(DefaultFlag.SPAWN_LOC, localPlayer);

            if (spawn != null) {
                event.setRespawnLocation(com.sk89q.worldedit.bukkit.BukkitUtil.toLocation(spawn));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onItemHeldChange(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(player.getWorld());

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

    @EventHandler(priority= EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        World world = event.getFrom().getWorld();
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);

        if (wcfg.useRegions) {
            RegionManager mgr = plugin.getGlobalRegionManager().get(event.getFrom().getWorld());
            Vector pt = new Vector(event.getTo().getBlockX(), event.getTo().getBlockY(), event.getTo().getBlockZ());
            Vector ptFrom = new Vector(event.getFrom().getBlockX(), event.getFrom().getBlockY(), event.getFrom().getBlockZ());
            ApplicableRegionSet set = mgr.getApplicableRegions(pt);
            ApplicableRegionSet setFrom = mgr.getApplicableRegions(ptFrom);
            LocalPlayer localPlayer = plugin.wrapPlayer(event.getPlayer());

            if (cfg.usePlayerTeleports) {
                boolean result = checkMove(plugin, event.getPlayer(), event.getFrom(), event.getTo());
                if (result) {
                    event.setCancelled(true);
                    return;
                }
            }

            if (event.getCause() == TeleportCause.ENDER_PEARL) {
                if (!plugin.getGlobalRegionManager().hasBypass(localPlayer, world)
                        && !(set.allows(DefaultFlag.ENDERPEARL, localPlayer)
                                && setFrom.allows(DefaultFlag.ENDERPEARL, localPlayer))) {
                    event.getPlayer().sendMessage(ChatColor.DARK_RED + "You're not allowed to go there.");
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        LocalPlayer localPlayer = plugin.wrapPlayer(player);
        World world = player.getWorld();
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);

        if (wcfg.useRegions && !plugin.getGlobalRegionManager().hasBypass(player, world)) {
            Vector pt = toVector(player.getLocation());
            RegionManager mgr = plugin.getGlobalRegionManager().get(world);
            ApplicableRegionSet set = mgr.getApplicableRegions(pt);

            Set<String> allowedCommands = set.getFlag(DefaultFlag.ALLOWED_CMDS, localPlayer);
            Set<String> blockedCommands = set.getFlag(DefaultFlag.BLOCKED_CMDS, localPlayer);
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
