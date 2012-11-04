// $Id$
/*
 * WorldGuard
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.sk89q.worldguard.bukkit;

import static com.sk89q.worldguard.bukkit.BukkitUtil.toVector;

import java.util.Iterator;
import java.util.Set;

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
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;

import com.sk89q.rulelists.KnownAttachment;
import com.sk89q.rulelists.RuleSet;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.blocks.ItemID;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.blacklist.events.BlockBreakBlacklistEvent;
import com.sk89q.worldguard.blacklist.events.BlockInteractBlacklistEvent;
import com.sk89q.worldguard.blacklist.events.BlockPlaceBlacklistEvent;
import com.sk89q.worldguard.blacklist.events.ItemAcquireBlacklistEvent;
import com.sk89q.worldguard.blacklist.events.ItemDropBlacklistEvent;
import com.sk89q.worldguard.blacklist.events.ItemUseBlacklistEvent;
import com.sk89q.worldguard.bukkit.FlagStateManager.PlayerFlagState;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

/**
 * Listener for player events.
 */
public class WorldGuardPlayerListener implements Listener {

    private WorldGuardPlugin plugin;

    public WorldGuardPlayerListener(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerEvents() {
        final PluginManager pm = plugin.getServer().getPluginManager();
        pm.registerEvents(this, plugin);

        if (plugin.getGlobalStateManager().usePlayerMove) {
            pm.registerEvents(new PlayerMoveHandler(), plugin);
        }
    }

    /**
     * Listener just for {@link PlayerMoveEvent}. This handler may not always be
     * registered for performance reasons.
     */
    class PlayerMoveHandler implements Listener {
        @EventHandler(priority = EventPriority.HIGH)
        public void onPlayerMove(PlayerMoveEvent event) {
            Player player = event.getPlayer();
            World world = player.getWorld();

            ConfigurationManager cfg = plugin.getGlobalStateManager();
            WorldConfiguration wcfg = cfg.get(world);

            if (player.getVehicle() != null) return; // handled in vehicle listener
            if (wcfg.useRegions) {
                // Did we move a block?
                if (event.getFrom().getBlockX() != event.getTo().getBlockX()
                        || event.getFrom().getBlockY() != event.getTo().getBlockY()
                        || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
                    PlayerFlagState state = plugin.getFlagStateManager().getState(player);

                    //Flush states in multiworld scenario
                    if (state.lastWorld != null && !state.lastWorld.equals(world)) {
                        plugin.getFlagStateManager().forget(player);
                        state = plugin.getFlagStateManager().getState(player);
                    }

                    LocalPlayer localPlayer = plugin.wrapPlayer(player);
                    boolean hasBypass = plugin.getGlobalRegionManager().hasBypass(player, world);

                    RegionManager mgr = plugin.getGlobalRegionManager().get(world);
                    Vector pt = new Vector(event.getTo().getBlockX(), event.getTo().getBlockY(), event.getTo().getBlockZ());
                    ApplicableRegionSet set = mgr.getApplicableRegions(pt);

                    boolean entryAllowed = set.allows(DefaultFlag.ENTRY, localPlayer);
                    if (!hasBypass && !entryAllowed) {
                        player.sendMessage(ChatColor.DARK_RED + "You are not permitted to enter this area.");

                        Location newLoc = event.getFrom();
                        newLoc.setX(newLoc.getBlockX() + 0.5);
                        newLoc.setY(newLoc.getBlockY());
                        newLoc.setZ(newLoc.getBlockZ() + 0.5);
                        event.setTo(newLoc);
                        return;
                    }

                    // Have to set this state
                    if (state.lastExitAllowed == null) {
                        state.lastExitAllowed = mgr.getApplicableRegions(toVector(event.getFrom()))
                                .allows(DefaultFlag.EXIT, localPlayer);
                    }

                    boolean exitAllowed = set.allows(DefaultFlag.EXIT, localPlayer);
                    if (!hasBypass && exitAllowed && !state.lastExitAllowed) {
                        player.sendMessage(ChatColor.DARK_RED + "You are not permitted to leave this area.");

                        Location newLoc = event.getFrom();
                        newLoc.setX(newLoc.getBlockX() + 0.5);
                        newLoc.setY(newLoc.getBlockY());
                        newLoc.setZ(newLoc.getBlockZ() + 0.5);
                        event.setTo(newLoc);
                        return;
                    }

                    String greeting = set.getFlag(DefaultFlag.GREET_MESSAGE);//, localPlayer);
                    String farewell = set.getFlag(DefaultFlag.FAREWELL_MESSAGE);//, localPlayer);
                    Boolean notifyEnter = set.getFlag(DefaultFlag.NOTIFY_ENTER);//, localPlayer);
                    Boolean notifyLeave = set.getFlag(DefaultFlag.NOTIFY_LEAVE);//, localPlayer);
                    GameMode gameMode = set.getFlag(DefaultFlag.GAME_MODE);

                    if (state.lastFarewell != null && (farewell == null
                            || !state.lastFarewell.equals(farewell))) {
                        String replacedFarewell = plugin.replaceMacros(
                                player, BukkitUtil.replaceColorMacros(state.lastFarewell));
                        for (String line : replacedFarewell.split("\n")) {
                            player.sendMessage(ChatColor.AQUA + " ** " + line);
                        }
                    }

                    if (greeting != null && (state.lastGreeting == null
                            || !state.lastGreeting.equals(greeting))) {
                        String replacedGreeting = plugin.replaceMacros(
                                player, BukkitUtil.replaceColorMacros(greeting));
                        for (String line : replacedGreeting.split("\n")) {
                            player.sendMessage(ChatColor.AQUA + " ** " + line);
                        }
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
                    state.lastWorld = event.getTo().getWorld();
                    state.lastBlockX = event.getTo().getBlockX();
                    state.lastBlockY = event.getTo().getBlockY();
                    state.lastBlockZ = event.getTo().getBlockZ();
                }
            }
        }
	}

    @EventHandler
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        WorldConfiguration wcfg = plugin.getGlobalStateManager().get(player.getWorld());

        // Regions
        if (wcfg.useRegions && !plugin.getGlobalRegionManager().hasBypass(player, player.getWorld())) {
            GameMode gameMode = plugin.getGlobalRegionManager().get(player.getWorld())
                    .getApplicableRegions(player.getLocation()).getFlag(DefaultFlag.GAME_MODE);
            if (plugin.getFlagStateManager().getState(player).lastGameMode != null
                    && gameMode != null && event.getNewGameMode() != gameMode) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Your game mode is locked to "
                        + gameMode + "in this region!");
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);

        /* --- No short-circuit returns below this line --- */

        if (wcfg.fireSpreadDisableToggle) {
            player.sendMessage(ChatColor.YELLOW
                    + "Fire spread is currently globally disabled for this world.");
        }

        if (plugin.inGroup(player, "wg-amphibious")) {
            cfg.enableAmphibiousMode(player);
        }

        // Regions
        if (wcfg.useRegions) {
            PlayerFlagState state = plugin.getFlagStateManager().getState(player);
            Location loc = player.getLocation();
            state.lastWorld = loc.getWorld();
            state.lastBlockX = loc.getBlockX();
            state.lastBlockY = loc.getBlockY();
            state.lastBlockZ = loc.getBlockZ();
        }

        // RuleLists
        RuleSet rules = wcfg.getRuleList().get(KnownAttachment.PLAYER_JOIN);
        BukkitContext context = new BukkitContext(event);
        context.setTargetEntity(player);
        rules.process(context);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        WorldConfiguration wcfg = plugin.getGlobalStateManager().get(player.getWorld());

        // Regions
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

            if (event.getRecipients().size() == 0) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        ConfigurationManager cfg = plugin.getGlobalStateManager();

        /* --- No short-circuit returns below this line --- */

        // Host keys
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
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);

        /* --- No short-circuit returns below this line --- */

        // Regions
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

        // RuleLists
        RuleSet rules = wcfg.getRuleList().get(KnownAttachment.PLAYER_QUIT);
        BukkitContext context = new BukkitContext(event);
        context.setSourceEntity(player);
        rules.process(context);

        // Cleanup
        cfg.forgetPlayer(plugin.wrapPlayer(player));
        plugin.forgetPlayer(player);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(player.getWorld());

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            handleBlockRightClick(event);
        } else if (event.getAction() == Action.RIGHT_CLICK_AIR) {
            handleAirRightClick(event);
        } else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            handleBlockLeftClick(event);
        } else if (event.getAction() == Action.LEFT_CLICK_AIR) {
            handleAirLeftClick(event);
        } else if (event.getAction() == Action.PHYSICAL) {
            handlePhysicalInteract(event);
        }

        // RuleLists
        RuleSet rules = wcfg.getRuleList().get(KnownAttachment.BLOCK_INTERACT);
        BukkitContext context = new BukkitContext(event);
        context.setSourceEntity(player);
        context.setTargetBlock(event.getClickedBlock().getState());
        if (rules.process(context)) {
            event.setCancelled(true);
            return;
        }
    }

    /**
     * Called when a player left clicks air.
     *
     * @param event Thrown event
     */
    private void handleAirLeftClick(PlayerInteractEvent event) {
         // I don't think we have to do anything here yet.
         return;
    }

    /**
     * Called when a player left clicks a block.
     *
     * @param event Thrown event
     */
    private void handleBlockLeftClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        int type = block.getTypeId();
        World world = player.getWorld();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);

        if (wcfg.useRegions) {
            Vector pt = toVector(block);
            RegionManager mgr = plugin.getGlobalRegionManager().get(world);
            ApplicableRegionSet set = mgr.getApplicableRegions(pt);
            LocalPlayer localPlayer = plugin.wrapPlayer(player);

            if (type == BlockID.STONE_BUTTON
                  || type == BlockID.LEVER
                  || type == BlockID.WOODEN_DOOR
                  || type == BlockID.TRAP_DOOR
                  || type == BlockID.NOTE_BLOCK) {
                if (!plugin.getGlobalRegionManager().hasBypass(player, world)
                        && !set.allows(DefaultFlag.USE, localPlayer)
                        && !set.canBuild(localPlayer)) {
                    player.sendMessage(ChatColor.DARK_RED + "You don't have permission to use that in this area.");
                    event.setUseInteractedBlock(Result.DENY);
                    event.setCancelled(true);
                    return;
                }
            }

            if (block.getRelative(event.getBlockFace()).getTypeId() == BlockID.FIRE) {
                if (!plugin.getGlobalRegionManager().hasBypass(player, world)
                        && !set.canBuild(localPlayer)) {
                    event.setUseInteractedBlock(Result.DENY);
                    event.setCancelled(true);
                    return;
                }
            }

        }

        if (type == BlockID.TNT && player.getItemInHand().getTypeId() == ItemID.FLINT_AND_TINDER) {
            if (wcfg.getBlacklist() != null) {
                if (!wcfg.getBlacklist().check(
                        new BlockBreakBlacklistEvent(plugin.wrapPlayer(player),
                        toVector(event.getClickedBlock()),
                        event.getClickedBlock().getTypeId()), false, false)) {
                    event.setUseInteractedBlock(Result.DENY);
                    event.setUseItemInHand(Result.DENY);
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    /**
     * Called when a player right clicks air.
     *
     * @param event Thrown event
     */
    private void handleAirRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        ItemStack item = player.getItemInHand();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);

        if (wcfg.getBlacklist() != null) {
            if (!wcfg.getBlacklist().check(
                    new ItemUseBlacklistEvent(plugin.wrapPlayer(player),
                            toVector(player.getLocation()),
                    item.getTypeId()), false, false)) {
                event.setCancelled(true);
                event.setUseItemInHand(Result.DENY);
                return;
            }
        }
    }

    /**
     * Called when a player right clicks a block.
     *
     * @param event Thrown event
     */
    private void handleBlockRightClick(PlayerInteractEvent event) {
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
                || type == BlockID.ENCHANTMENT_TABLE
                || type == BlockID.CAULDRON)
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
            ApplicableRegionSet set = mgr.getApplicableRegions(pt);
            LocalPlayer localPlayer = plugin.wrapPlayer(player);

            if (item.getTypeId() == wcfg.regionWand && plugin.hasPermission(player, "worldguard.region.wand")) {
                if (set.size() > 0) {
                    player.sendMessage(ChatColor.YELLOW + "Can you build? "
                            + (set.canBuild(localPlayer) ? "Yes" : "No"));

                    StringBuilder str = new StringBuilder();
                    for (Iterator<ProtectedRegion> it = set.iterator(); it.hasNext();) {
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

            if (item.getTypeId() == BlockID.TNT) {
                Block placedOn = block.getRelative(event.getBlockFace());
                if (!plugin.getGlobalRegionManager().hasBypass(player, world)
                        && !plugin.getGlobalRegionManager().allows(
                        DefaultFlag.TNT, placedOn.getLocation(), localPlayer)) {
                    event.setUseItemInHand(Result.DENY);
                    event.setCancelled(true);
                }
            }

            if (item.getTypeId() == ItemID.INK_SACK
                    && item.getData() != null
                    && item.getData().getData() == 15 // bonemeal
                    && type == BlockID.GRASS) {
                if (!plugin.getGlobalRegionManager().hasBypass(player, world)
                        && !set.canBuild(localPlayer)) {
                    event.setCancelled(true);
                    event.setUseItemInHand(Result.DENY);
                }
            }

            if (type == BlockID.BED) {
                if (!plugin.getGlobalRegionManager().hasBypass(player, world)
                        && !set.allows(DefaultFlag.SLEEP, localPlayer)) {
                    player.sendMessage(ChatColor.DARK_RED + "You're not allowed to use that bed.");
                    event.setUseInteractedBlock(Result.DENY);
                    event.setCancelled(true);
                    return;
                }
            }

            if (type == BlockID.CHEST
                    || type == BlockID.JUKEBOX //stores the (arguably) most valuable item
                    || type == BlockID.DISPENSER
                    || type == BlockID.FURNACE
                    || type == BlockID.BURNING_FURNACE
                    || type == BlockID.BREWING_STAND) {
                if (!plugin.getGlobalRegionManager().hasBypass(player, world)
                        && !set.canBuild(localPlayer)
                        && !set.allows(DefaultFlag.CHEST_ACCESS, localPlayer)) {
                    player.sendMessage(ChatColor.DARK_RED + "You don't have permission to open that in this area.");
                    event.setUseInteractedBlock(Result.DENY);
                    event.setCancelled(true);
                    return;
                }
            }

            if (type == BlockID.LEVER
                    || type == BlockID.STONE_BUTTON
                    || type == BlockID.NOTE_BLOCK
                    || type == BlockID.REDSTONE_REPEATER_OFF
                    || type == BlockID.REDSTONE_REPEATER_ON
                    || type == BlockID.WOODEN_DOOR
                    || type == BlockID.TRAP_DOOR
                    || type == BlockID.FENCE_GATE
                    || type == BlockID.JUKEBOX //stores the (arguably) most valuable item
                    || type == BlockID.DISPENSER
                    || type == BlockID.FURNACE
                    || type == BlockID.BURNING_FURNACE
                    || type == BlockID.WORKBENCH
                    || type == BlockID.BREWING_STAND
                    || type == BlockID.ENCHANTMENT_TABLE
                    || type == BlockID.CAULDRON
                    || type == BlockID.DRAGON_EGG) {
                if (!plugin.getGlobalRegionManager().hasBypass(player, world)
                        && !set.canBuild(localPlayer)
                        && !set.allows(DefaultFlag.USE, localPlayer)) {
                    player.sendMessage(ChatColor.DARK_RED + "You don't have permission to use that in this area.");
                    event.setUseInteractedBlock(Result.DENY);
                    event.setCancelled(true);
                    return;
                }
            }

            if (type == BlockID.CAKE_BLOCK) {
                if (!plugin.getGlobalRegionManager().hasBypass(player, world)
                        && !set.canBuild(localPlayer)
                        && !set.allows(DefaultFlag.USE, localPlayer)) {
                    player.sendMessage(ChatColor.DARK_RED + "You're not invited to this tea party!");
                    event.setUseInteractedBlock(Result.DENY);
                    event.setCancelled(true);
                    return;
                }
            }

            if (BlockType.isRailBlock(type) && item.getTypeId() == ItemID.MINECART) {
                if (!plugin.getGlobalRegionManager().hasBypass(player, world)
                        && !set.canBuild(localPlayer)
                        && !set.allows(DefaultFlag.PLACE_VEHICLE, localPlayer)) {
                    player.sendMessage(ChatColor.DARK_RED + "You don't have permission to place vehicles here.");
                    event.setUseItemInHand(Result.DENY);
                    event.setCancelled(true);
                    return;
                }
            }

            if (item.getTypeId() == ItemID.WOOD_BOAT) {
                if (!plugin.getGlobalRegionManager().hasBypass(player, world)
                        && !set.canBuild(localPlayer)
                        && !set.allows(DefaultFlag.PLACE_VEHICLE, localPlayer)) {
                    player.sendMessage(ChatColor.DARK_RED + "You don't have permission to place vehicles here.");
                    event.setUseItemInHand(Result.DENY);
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (wcfg.getBlacklist() != null) {
            if(type != BlockID.CHEST
                    && type != BlockID.DISPENSER
                    && type != BlockID.FURNACE
                    && type != BlockID.BURNING_FURNACE
                    && type != BlockID.BREWING_STAND
                    && type != BlockID.ENCHANTMENT_TABLE
                    && type != BlockID.CAULDRON) {
                if (!wcfg.getBlacklist().check(
                        new ItemUseBlacklistEvent(plugin.wrapPlayer(player), toVector(block),
                                item.getTypeId()), false, false)) {
                    event.setUseItemInHand(Result.DENY);
                    event.setCancelled(true);
                    return;
                }
            }

            if (!wcfg.getBlacklist().check(
                    new BlockInteractBlacklistEvent(plugin.wrapPlayer(player), toVector(block),
                            block.getTypeId()), false, false)) {
                event.setUseInteractedBlock(Result.DENY);
                event.setCancelled(true);
                return;
            }

            // Workaround for http://leaky.bukkit.org/issues/1034
            if (item.getTypeId() == BlockID.TNT) {
                Block placedOn = block.getRelative(event.getBlockFace());
                if (!wcfg.getBlacklist().check(
                        new BlockPlaceBlacklistEvent(plugin.wrapPlayer(player), toVector(placedOn),
                              item.getTypeId()), false, false)) {
                    event.setUseItemInHand(Result.DENY);
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if ((type == BlockID.CHEST
                || type == BlockID.DISPENSER
                || type == BlockID.FURNACE
                || type == BlockID.BURNING_FURNACE
                || type == BlockID.ENCHANTMENT_TABLE
                || type == BlockID.BREWING_STAND)) {

            if (wcfg.isChestProtected(block, player)) {
                player.sendMessage(ChatColor.DARK_RED + "The chest is protected.");
                event.setUseInteractedBlock(Result.DENY);
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * Called when a player steps on a pressure plate or tramples crops.
     *
     * @param event Thrown event
     */
    private void handlePhysicalInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock(); //not actually clicked but whatever
        int type = block.getTypeId();
        World world = player.getWorld();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);

        // Regions
        if (wcfg.useRegions) {
            Vector pt = toVector(block);
            RegionManager mgr = plugin.getGlobalRegionManager().get(world);
            ApplicableRegionSet set = mgr.getApplicableRegions(pt);
            LocalPlayer localPlayer = plugin.wrapPlayer(player);

            if (type == BlockID.STONE_PRESSURE_PLATE || type == BlockID.WOODEN_PRESSURE_PLATE) {
               if (!plugin.getGlobalRegionManager().hasBypass(player, world)
                       && !set.canBuild(localPlayer)
                       && !set.allows(DefaultFlag.USE, localPlayer)) {
                   event.setUseInteractedBlock(Result.DENY);
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

        // Regions
        if (wcfg.useRegions) {
            if (!plugin.getGlobalRegionManager().allows(DefaultFlag.ITEM_DROP, player.getLocation())) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You don't have permission to do that in this area.");
            }
        }

        // Blacklists
        if (wcfg.getBlacklist() != null) {
            Item ci = event.getItemDrop();

            if (!wcfg.getBlacklist().check(
                    new ItemDropBlacklistEvent(plugin.wrapPlayer(event.getPlayer()),
                            toVector(ci.getLocation()), ci.getItemStack().getTypeId()), false, false)) {
                event.setCancelled(true);
                return;
            }
        }

        // RuleLists
        RuleSet rules = wcfg.getRuleList().get(KnownAttachment.ITEM_DROP);
        BukkitContext context = new BukkitContext(event);
        context.setSourceEntity(player);
        context.setItem(event.getItemDrop().getItemStack());
        if (rules.process(context)) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getPlayer().getWorld());

        // Blacklist
        if (wcfg.getBlacklist() != null) {
            Item ci = event.getItem();

            if (!wcfg.getBlacklist().check(
                    new ItemAcquireBlacklistEvent(plugin.wrapPlayer(event.getPlayer()),
                            toVector(ci.getLocation()), ci.getItemStack().getTypeId()), false, true)) {
                event.setCancelled(true);
                return;
            }
        }

        // RuleLists
        RuleSet rules = wcfg.getRuleList().get(KnownAttachment.ITEM_PICKUP);
        BukkitContext context = new BukkitContext(event);
        context.setSourceEntity(event.getPlayer());
        context.setItem(event.getItem().getItemStack());
        if (rules.process(context)) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(player.getWorld());

        // RuleLists
        RuleSet rules = wcfg.getRuleList().get(KnownAttachment.ENTITY_DEATH);
        BukkitContext context = new BukkitContext(event);
        context.setTargetEntity(player);
        rules.process(context);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);

        // Regions
        if (!plugin.getGlobalRegionManager().canBuild(
                player, event.getBlockClicked().getRelative(event.getBlockFace()))) {
            player.sendMessage(ChatColor.DARK_RED + "You don't have permission for this area.");
            event.setCancelled(true);
            return;
        }

        // Blacklist
        if (wcfg.getBlacklist() != null) {
            if (!wcfg.getBlacklist().check(
                    new ItemUseBlacklistEvent(plugin.wrapPlayer(player),
                            toVector(player.getLocation()), event.getBucket().getId()), false, false)) {
                event.setCancelled(true);
                return;
            }
        }

        // RuleLists
        RuleSet rules = wcfg.getRuleList().get(KnownAttachment.ITEM_USE);
        BukkitContext context = new BukkitContext(event);
        context.setSourceEntity(event.getPlayer());
        context.setItem(event.getItemStack());
        if (rules.process(context)) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);

        // Regions
        if (!plugin.getGlobalRegionManager().canBuild(
                player, event.getBlockClicked().getRelative(event.getBlockFace()))) {
            player.sendMessage(ChatColor.DARK_RED + "You don't have permission for this area.");
            event.setCancelled(true);
            return;
        }

        // Blacklist
        if (wcfg.getBlacklist() != null) {
            if (!wcfg.getBlacklist().check(
                    new ItemUseBlacklistEvent(plugin.wrapPlayer(player),
                            toVector(player.getLocation()), event.getBucket().getId()), false, false)) {
                event.setCancelled(true);
                return;
            }
        }

        // RuleLists
        RuleSet rules = wcfg.getRuleList().get(KnownAttachment.ITEM_USE);
        BukkitContext context = new BukkitContext(event);
        context.setSourceEntity(event.getPlayer());
        context.setItem(event.getItemStack());
        if (rules.process(context)) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(player.getWorld());

        // Regions
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

        // RuleLists
        RuleSet rules = wcfg.getRuleList().get(KnownAttachment.PLAYER_RESPAWN);
        BukkitContext context = new BukkitContext(event);
        context.setSourceEntity(event.getPlayer());
        rules.process(context);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(player.getWorld());

        // Regions
        if (wcfg.useRegions) {
            Vector pt = toVector(location);
            RegionManager mgr = plugin.getGlobalRegionManager().get(player.getWorld());
            ApplicableRegionSet set = mgr.getApplicableRegions(pt);

            if (!plugin.getGlobalRegionManager().hasBypass(player, player.getWorld())
                && !set.allows(DefaultFlag.SLEEP, plugin.wrapPlayer(player))) {
                    event.setCancelled(true);
                    player.sendMessage("This bed doesn't belong to you!");
                    return;
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

        // Regions
        if (wcfg.useRegions && !plugin.getGlobalRegionManager().hasBypass(player, world)) {
            Vector pt = toVector(player.getLocation());
            RegionManager mgr = plugin.getGlobalRegionManager().get(world);
            ApplicableRegionSet set = mgr.getApplicableRegions(pt);

            String[] parts = event.getMessage().split(" ");
            String lowerCommand = parts[0].toLowerCase();

            Set<String> allowedCommands = set.getFlag(DefaultFlag.ALLOWED_CMDS, localPlayer);
            Set<String> blockedCommands = set.getFlag(DefaultFlag.BLOCKED_CMDS, localPlayer);

            if (allowedCommands != null && !allowedCommands.contains(lowerCommand)
                    && (blockedCommands == null || blockedCommands.contains(lowerCommand))) {
                player.sendMessage(ChatColor.RED + lowerCommand + " is not allowed in this area.");
                event.setMessage("/"); // Make sure that it's disabled
                event.setCancelled(true);
                return;
            }

            if (blockedCommands != null && blockedCommands.contains(lowerCommand)
                    && (allowedCommands == null || !allowedCommands.contains(lowerCommand))) {
                player.sendMessage(ChatColor.RED + lowerCommand + " is blocked in this area.");
                event.setMessage("/"); // Make sure that it's disabled
                event.setCancelled(true);
                return;
            }
        }
    }
}
