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

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
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

/**
 * Listener for player events.
 */
class WorldGuardPlayerListener implements Listener {

    private WorldGuardPlugin plugin;

    /**
     * Construct the listener.
     *
     * @param plugin WorldGuard plugin
     */
    WorldGuardPlayerListener(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Register the events.
     */
    void registerEvents() {
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

        // RuleLists
        RuleSet rules = wcfg.getRuleList().get(KnownAttachment.CHAT);
        BukkitContext context = new BukkitContext(event);
        context.setSourceEntity(event.getPlayer());
        context.setMessage(event.getMessage());

        if (rules.process(context)) {
            event.setCancelled(true);
            return;
        }

        // Put back the chat
        event.setMessage(context.getMessage());
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

        // RuleLists
        RuleSet rules = wcfg.getRuleList().get(KnownAttachment.PLAYER_QUIT);
        BukkitContext context = new BukkitContext(event);
        context.setSourceEntity(player);
        rules.process(context);

        // Cleanup
        cfg.forgetPlayer(plugin.wrapPlayer(player));
        plugin.forgetPlayer(player);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(player.getWorld());

        /* --- No short-circuit returns below this line --- */

        // RuleLists
        RuleSet rules;
        BukkitContext context;

        // Send one for the block
        Block block = event.getClickedBlock();
        if (block != null) {
            rules = wcfg.getRuleList().get(KnownAttachment.BLOCK_INTERACT);
            context = new BukkitContext(event);
            context.setSourceEntity(player);
            context.setTargetBlock(event.getClickedBlock().getState());
            if (rules.process(context)) {
                event.setUseInteractedBlock(Result.DENY);
            }
        }

        // Send one for the item in the end
        ItemStack heldItem = event.getPlayer().getItemInHand();
        if (heldItem != null) {
            rules = wcfg.getRuleList().get(KnownAttachment.ITEM_USE);
            context = new BukkitContext(event);
            context.setSourceEntity(event.getPlayer());
            context.setItem(heldItem);
            if (rules.process(context)) {
                event.setUseItemInHand(Result.DENY);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getPlayer().getWorld());
        Player player = event.getPlayer();

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

        /* --- No short-circuit returns below this line --- */

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

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(player.getWorld());

        /* --- No short-circuit returns below this line --- */

        // RuleLists
        RuleSet rules = wcfg.getRuleList().get(KnownAttachment.PLAYER_RESPAWN);
        BukkitContext context = new BukkitContext(event);
        context.setSourceEntity(event.getPlayer());
        rules.process(context);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(player.getWorld());

        // RuleLists
        RuleSet rules = wcfg.getRuleList().get(KnownAttachment.BLOCK_INTERACT);
        BukkitContext context = new BukkitContext(event);
        context.setSourceEntity(player);
        context.setTargetBlock(event.getBed().getState());
        if (rules.process(context)) {
            event.setCancelled(true);
            return;
        }
    }
}
