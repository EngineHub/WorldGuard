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

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.util.Blocks;
import com.sk89q.worldguard.bukkit.util.Materials;
import com.sk89q.worldguard.bukkit.util.Events;
import com.sk89q.worldguard.util.cause.Cause;
import com.sk89q.worldguard.util.cause.Causes;
import com.sk89q.worldguard.bukkit.event.block.BreakBlockEvent;
import com.sk89q.worldguard.bukkit.event.block.PlaceBlockEvent;
import com.sk89q.worldguard.bukkit.event.block.UseBlockEvent;
import com.sk89q.worldguard.bukkit.event.entity.DestroyEntityEvent;
import com.sk89q.worldguard.bukkit.event.entity.SpawnEntityEvent;
import com.sk89q.worldguard.bukkit.event.entity.UseEntityEvent;
import com.sk89q.worldguard.bukkit.event.inventory.UseItemEvent;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import static com.sk89q.worldguard.bukkit.util.Materials.isBlockModifiedOnClick;
import static com.sk89q.worldguard.bukkit.util.Materials.isItemAppliedToBlock;
import static com.sk89q.worldguard.util.cause.Causes.create;

public class EventAbstractionListener implements Listener {

    private final WorldGuardPlugin plugin;

    public EventAbstractionListener(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerEvents() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    //-------------------------------------------------------------------------
    // Block break / place
    //-------------------------------------------------------------------------

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Events.fireToCancel(event, new BreakBlockEvent(event, create(event.getPlayer()), event.getBlock()));
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Events.fireToCancel(event, new UseBlockEvent(event, create(event.getPlayer()), event.getBlock()));
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        Events.fireToCancel(event, new UseBlockEvent(event, Collections.<Cause<?>>emptyList(), event.getBlock()));
    }

    // TODO: Handle EntityCreatePortalEvent?

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        // Fire two events: one as BREAK and one as PLACE
        if (event.getTo() != Material.AIR && event.getBlock().getType() != Material.AIR) {
            Events.fireToCancel(event, new BreakBlockEvent(event, create(event.getEntity()), event.getBlock()));
            Events.fireToCancel(event, new PlaceBlockEvent(event, create(event.getEntity()), event.getBlock()));
        } else {
            if (event.getTo() == Material.AIR) {
                Events.fireToCancel(event, new BreakBlockEvent(event, create(event.getEntity()), event.getBlock()));
            } else {
                Events.fireToCancel(event, new PlaceBlockEvent(event, create(event.getEntity()), event.getBlock()));
            }
        }
    }

    // TODO: Handle pistons
    // TODO: Handle EntityExplodeEvent

    //-------------------------------------------------------------------------
    // Block external interaction
    //-------------------------------------------------------------------------

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        Block target = event.getBlock();

        // Previously, and perhaps still, the only way to catch cake eating
        // events was through here
        if (target.getType() == Material.CAKE_BLOCK) {
            Events.fireToCancel(event, new UseBlockEvent(event, create(event.getPlayer()), target));
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        @Nullable ItemStack item = player.getItemInHand();
        Block block = event.getClickedBlock();
        List<? extends Cause<?>> causes = create(player);

        switch (event.getAction()) {
            case PHYSICAL:
                // TODO: Don't fire events for blocks that can't be interacted with using PHYSICAL
                if (Events.fireAndTestCancel(new UseBlockEvent(event, causes, block))) {
                    event.setUseInteractedBlock(Result.DENY);
                    event.setCancelled(true);
                }
                break;

            case RIGHT_CLICK_BLOCK:
                if (item != null && item.getType() == Material.TNT) {
                    // Workaround for a bug that allowed tnt to trigger instantly if placed
                    // next to redstone, without plugins getting the block place event
                    // (not sure if this actually still happens)
                    Events.fireToCancel(event, new UseBlockEvent(event, create(event.getPlayer()), block.getLocation(), Material.TNT));
                }

                // Handle created Minecarts
                if (item != null && Materials.isMinecart(item.getType())) {
                    // TODO: Give a more specific minecart type
                    Block placedBlock = block.getRelative(event.getBlockFace());
                    Events.fireToCancel(event, new SpawnEntityEvent(event, create(event.getPlayer()), placedBlock.getLocation().add(0.5, 0, 0.5), EntityType.MINECART));
                }

                // Handle cocoa beans
                if (item != null && item.getType() == Material.INK_SACK && Materials.isDyeColor(item.getData(), DyeColor.BROWN)) {
                    // CraftBukkit doesn't or didn't throw a block place for this
                    if (!(event.getBlockFace() == BlockFace.DOWN || event.getBlockFace() == BlockFace.UP)) {
                        Block placedBlock = block.getRelative(event.getBlockFace());
                        Events.fireToCancel(event, new PlaceBlockEvent(event, create(event.getPlayer()), placedBlock.getLocation(), Material.COCOA));
                    }
                }

                // Workaround for http://leaky.bukkit.org/issues/1034
                if (item != null && item.getType() == Material.TNT) {
                    Block placedBlock = block.getRelative(event.getBlockFace());
                    Events.fireToCancel(event, new PlaceBlockEvent(event, create(event.getPlayer()), placedBlock.getLocation(), Material.TNT));
                }

                // Handle flint and steel and fire charge as fire place
                if (item != null && (item.getType() == Material.FIREBALL || item.getType() == Material.FLINT_AND_STEEL)) {
                    Block placedBlock = block.getRelative(event.getBlockFace());
                    if (!Events.fireAndTestCancel(new PlaceBlockEvent(event, create(event.getPlayer()), placedBlock.getLocation(), Material.FIRE))) {
                        event.setUseItemInHand(Result.DENY);
                    }
                }

            case LEFT_CLICK_BLOCK:
                // TODO: Don't fire events for blocks that can't be interacted with using clicks

                // As of MC ~1.6, sneaking blocks the use of blocks with right click
                if (!player.isSneaking() || event.getAction() == Action.LEFT_CLICK_BLOCK) {
                    // Only fire events for blocks that are modified when right clicked
                    if (isBlockModifiedOnClick(block.getType()) || (item != null && isItemAppliedToBlock(item.getType(), block.getType()))) {
                        if (Events.fireAndTestCancel(new UseBlockEvent(event, causes, block))) {
                            event.setUseInteractedBlock(Result.DENY);
                        }

                        // Handle connected blocks (i.e. beds, chests)
                        for (Block connected : Blocks.getConnected(block)) {
                            if (Events.fireAndTestCancel(new UseBlockEvent(event, create(event.getPlayer()), connected))) {
                                event.setUseInteractedBlock(Result.DENY);
                                break;
                            }
                        }
                    }

                    // Special handling of flint and steel on TNT
                    if (block.getType() == Material.TNT && item != null && item.getType() == Material.FLINT_AND_STEEL) {
                        if (Events.fireAndTestCancel(new BreakBlockEvent(event, create(event.getPlayer()), block))) {
                            event.setUseInteractedBlock(Result.DENY);
                            break;
                        }
                    }
                }

                // Special handling of putting out fires
                if (event.getAction() == Action.LEFT_CLICK_BLOCK && block.getType() == Material.FIRE) {
                    if (Events.fireAndTestCancel(new BreakBlockEvent(event, create(event.getPlayer()), block))) {
                        event.setUseInteractedBlock(Result.DENY);
                        break;
                    }
                }

            case LEFT_CLICK_AIR:
            case RIGHT_CLICK_AIR:
                if (item != null && Events.fireAndTestCancel(new UseItemEvent(event, causes, player.getWorld(), item))) {
                    event.setUseItemInHand(Result.DENY);
                }

                break;
        }
    }

    @EventHandler
    public void onEntityInteract(org.bukkit.event.entity.EntityInteractEvent event) {
        Events.fireToCancel(event, new UseBlockEvent(event, create(event.getEntity()), event.getBlock()));
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        List<? extends Cause<?>> causes;

        // Find the cause
        if (event.getPlayer() != null) {
            causes = create(event.getPlayer());
        } else if (event.getIgnitingEntity() != null) {
            causes = create(event.getIgnitingEntity());
        } else if (event.getIgnitingBlock() != null) {
            causes = create(event.getIgnitingBlock());
        } else {
            causes = Collections.emptyList();
        }

        Events.fireToCancel(event, new BreakBlockEvent(event, causes, event.getBlock()));

        // This is also handled in the PlayerInteractEvent listener
        if (event.getCause() == IgniteCause.FLINT_AND_STEEL || event.getCause() == IgniteCause.FIREBALL) {
            // TODO: Test location of block
            Events.fireToCancel(event, new PlaceBlockEvent(event, causes, event.getBlock().getLocation(), Material.FIRE));
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Events.fireToCancel(event, new UseBlockEvent(event, create(event.getPlayer()), event.getBlock()));
    }

    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent event) {
        Events.fireToCancel(event, new UseBlockEvent(event, create(event.getPlayer()), event.getBed()));
    }

    @EventHandler
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        Block blockAffected = event.getBlockClicked().getRelative(event.getBlockFace());

        // Milk buckets can't be emptied as of writing
        if (event.getBucket() != Material.MILK_BUCKET) {
            ItemStack item = new ItemStack(event.getBucket(), 1);
            Events.fireToCancel(event, new PlaceBlockEvent(event, create(player), blockAffected));
            Events.fireToCancel(event, new UseItemEvent(event, create(player), player.getWorld(), item));
        }
    }

    @EventHandler
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        Block blockAffected = event.getBlockClicked().getRelative(event.getBlockFace());

        // Milk buckets can't be filled by right clicking the ground as of writing
        if (event.getBucket() != Material.MILK_BUCKET) {
            ItemStack item = new ItemStack(event.getBucket(), 1);
            Events.fireToCancel(event, new BreakBlockEvent(event, create(player), blockAffected));
            Events.fireToCancel(event, new UseItemEvent(event, create(player), player.getWorld(), item));
        }
    }

    // TODO: Handle EntityPortalEnterEvent

    //-------------------------------------------------------------------------
    // Block self-interaction
    //-------------------------------------------------------------------------

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        Events.fireToCancel(event, new PlaceBlockEvent(event, create(event.getBlock()), event.getToBlock()));
    }

    //-------------------------------------------------------------------------
    // Entity break / place
    //-------------------------------------------------------------------------

    @EventHandler
    public void onHangingPlace(HangingPlaceEvent event) {
        Events.fireToCancel(event, new SpawnEntityEvent(event, create(event.getPlayer()), event.getEntity()));
    }

    @EventHandler
    public void onHangingBreak(HangingBreakEvent event) {
        if (event instanceof HangingBreakByEntityEvent) {
            Events.fireToCancel(event,  new DestroyEntityEvent(event, create(((HangingBreakByEntityEvent) event).getRemover()), event.getEntity()));
        } else {
            Events.fireToCancel(event, new DestroyEntityEvent(event, Collections.<Cause<?>>emptyList(), event.getEntity()));
        }
    }

    @EventHandler
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        Events.fireToCancel(event, new DestroyEntityEvent(event, create(event.getAttacker()), event.getVehicle()));
    }

    //-------------------------------------------------------------------------
    // Entity external interaction
    //-------------------------------------------------------------------------

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        ItemStack item = player.getItemInHand();
        Entity entity = event.getRightClicked();

        Events.fireToCancel(event, new UseItemEvent(event, create(player), world, item));
        Events.fireToCancel(event, new UseEntityEvent(event, create(player), entity));
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event instanceof EntityDamageByBlockEvent) {
            Events.fireToCancel(event, new UseEntityEvent(event, create(((EntityDamageByBlockEvent) event).getDamager()), event.getEntity()));

        } else if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent entityEvent = (EntityDamageByEntityEvent) event;
            Entity damager = entityEvent.getDamager();
            Events.fireToCancel(event, new UseEntityEvent(event, create(damager), event.getEntity()));

            // Item use event with the item in hand
            // Older blacklist handler code used this, although it suffers from
            // race problems
            if (damager instanceof Player) {
                ItemStack item = ((Player) damager).getItemInHand();

                if (item != null) {
                    Events.fireToCancel(event, new UseItemEvent(event, create(damager), event.getEntity().getWorld(), item));
                }
            }

        } else {
            Events.fireToCancel(event, new UseEntityEvent(event, Collections.<Cause<?>>emptyList(), event.getEntity()));
        }
    }

    @EventHandler
    public void onEntityCombust(EntityCombustEvent event) {
        if (event instanceof EntityCombustByBlockEvent) {
            Events.fireToCancel(event, new UseEntityEvent(event, create(((EntityCombustByBlockEvent) event).getCombuster()), event.getEntity()));

        } else if (event instanceof EntityCombustByEntityEvent) {
            Events.fireToCancel(event, new UseEntityEvent(event, create(((EntityCombustByEntityEvent) event).getCombuster()), event.getEntity()));

        } else {
            Events.fireToCancel(event, new UseEntityEvent(event, Collections.<Cause<?>>emptyList(), event.getEntity()));
        }
    }

    @EventHandler
    public void onEntityUnleash(EntityUnleashEvent event) {
        if (event instanceof PlayerUnleashEntityEvent) {
            PlayerUnleashEntityEvent playerEvent = (PlayerUnleashEntityEvent) event;
            Events.fireToCancel(playerEvent, new UseEntityEvent(playerEvent, create(playerEvent.getPlayer()), event.getEntity()));
        } else {
            // TODO: Raise anyway?
        }
    }

    @EventHandler
    public void onEntityTame(EntityTameEvent event) {
        Events.fireToCancel(event, new UseEntityEvent(event, create(event.getOwner()), event.getEntity()));
    }

    @EventHandler
    public void onPlayerShearEntity(PlayerShearEntityEvent event) {
        Events.fireToCancel(event, new UseEntityEvent(event, create(event.getPlayer()), event.getEntity()));
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Events.fireToCancel(event, new DestroyEntityEvent(event, create(event.getPlayer()), event.getItem()));
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Events.fireToCancel(event, new SpawnEntityEvent(event, create(event.getPlayer()), event.getItemDrop()));
    }

    @EventHandler
    public void onVehicleDamage(VehicleDamageEvent event) {
        Events.fireToCancel(event, new DestroyEntityEvent(event, create(event.getAttacker()), event.getVehicle()));
    }

    @EventHandler
    public void onVehicleEnter(VehicleDamageEvent event) {
        Events.fireToCancel(event, new UseEntityEvent(event, create(event.getAttacker()), event.getVehicle()));
    }

    //-------------------------------------------------------------------------
    // Composite events
    //-------------------------------------------------------------------------

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        Entity entity = event.getEntity();
        ThrownPotion potion = event.getPotion();
        World world = entity.getWorld();
        List<? extends Cause<?>> causes = Causes.create(potion.getShooter());

        // Fire item interaction event
        Events.fireToCancel(event, new UseItemEvent(event, causes, world, potion.getItem()));

        // Fire entity interaction event
        if (!event.isCancelled()) {
            int blocked = 0;

            for (LivingEntity affected : event.getAffectedEntities()) {
                if (Events.fireAndTestCancel(new UseEntityEvent(event, causes, affected))) {
                    event.setIntensity(affected, 0);
                    blocked++;
                }
            }

            if (blocked == event.getAffectedEntities().size()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockDispense(BlockDispenseEvent event) {
        Events.fireToCancel(event, new UseItemEvent(event, create(event.getBlock()), event.getBlock().getWorld(), event.getItem()));
    }

    // TODO: Inventory events?

}
