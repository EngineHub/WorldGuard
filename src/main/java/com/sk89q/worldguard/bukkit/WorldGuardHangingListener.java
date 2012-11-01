package com.sk89q.worldguard.bukkit;

import static com.sk89q.worldguard.bukkit.BukkitUtil.toVector;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;

import com.sk89q.worldedit.blocks.ItemID;
import com.sk89q.worldguard.blacklist.events.BlockBreakBlacklistEvent;
import com.sk89q.worldguard.blacklist.events.ItemUseBlacklistEvent;
import com.sk89q.worldguard.protection.flags.DefaultFlag;

/**
 * Listener for painting related events.
 *
 * @author BangL <henno.rickowski@gmail.com>
 */
public class WorldGuardHangingListener implements Listener {

    private WorldGuardPlugin plugin;

    /**
     * Construct the object;
     *
     * @param plugin The plugin instance
     */
    public WorldGuardHangingListener(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Register events.
     */
    public void registerEvents() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHangingingBreak(HangingBreakEvent breakEvent) {
        if (!(breakEvent instanceof HangingBreakByEntityEvent)) {
            return;
        }

        HangingBreakByEntityEvent event = (HangingBreakByEntityEvent) breakEvent;
        Hanging hanging = event.getEntity();
        World world = hanging.getWorld();
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);

        if (event.getRemover() instanceof Player) {
            Player player = (Player) event.getRemover();

            if (wcfg.getBlacklist() != null) {
                if (hanging instanceof Painting
                        && !wcfg.getBlacklist().check(
                            new BlockBreakBlacklistEvent(plugin.wrapPlayer(player),
                                    toVector(player.getLocation()), ItemID.PAINTING), false, false)) {
                    event.setCancelled(true);
                    return;
                } else if (hanging instanceof ItemFrame
                        && !wcfg.getBlacklist().check(
                            new BlockBreakBlacklistEvent(plugin.wrapPlayer(player),
                                    toVector(player.getLocation()), ItemID.ITEM_FRAME), false, false)) {
                    event.setCancelled(true);
                    return;
                }
            }

            if (wcfg.useRegions) {
                if (!plugin.getGlobalRegionManager().canBuild(player, hanging.getLocation())) {
                    player.sendMessage(ChatColor.DARK_RED + "You don't have permission for this area.");
                    event.setCancelled(true);
                    return;
                }
            }
        } else {
            if (event.getRemover() instanceof Creeper) {
                if (wcfg.blockCreeperBlockDamage || wcfg.blockCreeperExplosions) {
                    event.setCancelled(true);
                    return;
                }
                if (wcfg.useRegions && !plugin.getGlobalRegionManager().allows(DefaultFlag.CREEPER_EXPLOSION, hanging.getLocation())) {
                    event.setCancelled(true);
                    return;
                }
            }

            if (hanging instanceof Painting
                    && (wcfg.blockEntityPaintingDestroy
                    || (wcfg.useRegions
                    && !plugin.getGlobalRegionManager().allows(DefaultFlag.ENTITY_PAINTING_DESTROY, hanging.getLocation())))) {
                event.setCancelled(true);
            } else if (hanging instanceof ItemFrame
                    && (wcfg.blockEntityItemFrameDestroy
                    || (wcfg.useRegions
                    && !plugin.getGlobalRegionManager().allows(DefaultFlag.ENTITY_ITEM_FRAME_DESTROY, hanging.getLocation())))) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event) {
        Block placedOn = event.getBlock();
        Player player = event.getPlayer();
        World world = placedOn.getWorld();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);

        if (wcfg.getBlacklist() != null) {

            if (event.getEntity() instanceof Painting
                    && !wcfg.getBlacklist().check(
                        new ItemUseBlacklistEvent(plugin.wrapPlayer(player),
                                toVector(player.getLocation()), ItemID.PAINTING), false, false)) {
                event.setCancelled(true);
                return;
            } else if (event.getEntity() instanceof ItemFrame
                    && !wcfg.getBlacklist().check(
                        new ItemUseBlacklistEvent(plugin.wrapPlayer(player),
                                toVector(player.getLocation()), ItemID.ITEM_FRAME), false, false)) {
                event.setCancelled(true);
                return;
            }
        }

        if (wcfg.useRegions) {
            if (!plugin.getGlobalRegionManager().canBuild(player, placedOn.getLocation())) {
                player.sendMessage(ChatColor.DARK_RED + "You don't have permission for this area.");
                event.setCancelled(true);
                return;
            }
        }
    }
}
