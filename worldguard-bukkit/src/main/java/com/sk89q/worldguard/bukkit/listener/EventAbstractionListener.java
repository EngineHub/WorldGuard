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

import static com.sk89q.worldguard.bukkit.cause.Cause.create;

import com.destroystokyo.paper.event.entity.EntityZapEvent;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.cause.Cause;
import com.sk89q.worldguard.bukkit.event.DelegateEvent;
import com.sk89q.worldguard.bukkit.event.block.BreakBlockEvent;
import com.sk89q.worldguard.bukkit.event.block.PlaceBlockEvent;
import com.sk89q.worldguard.bukkit.event.block.UseBlockEvent;
import com.sk89q.worldguard.bukkit.event.entity.DamageEntityEvent;
import com.sk89q.worldguard.bukkit.event.entity.DestroyEntityEvent;
import com.sk89q.worldguard.bukkit.event.entity.SpawnEntityEvent;
import com.sk89q.worldguard.bukkit.event.entity.UseEntityEvent;
import com.sk89q.worldguard.bukkit.event.inventory.UseItemEvent;
import com.sk89q.worldguard.bukkit.listener.debounce.BlockPistonExtendKey;
import com.sk89q.worldguard.bukkit.listener.debounce.BlockPistonRetractKey;
import com.sk89q.worldguard.bukkit.listener.debounce.EventDebounce;
import com.sk89q.worldguard.bukkit.listener.debounce.legacy.AbstractEventDebounce.Entry;
import com.sk89q.worldguard.bukkit.listener.debounce.legacy.BlockEntityEventDebounce;
import com.sk89q.worldguard.bukkit.listener.debounce.legacy.EntityEntityEventDebounce;
import com.sk89q.worldguard.bukkit.listener.debounce.legacy.InventoryMoveItemEventDebounce;
import com.sk89q.worldguard.bukkit.util.Blocks;
import com.sk89q.worldguard.bukkit.util.Entities;
import com.sk89q.worldguard.bukkit.util.Events;
import com.sk89q.worldguard.bukkit.util.Materials;
import com.sk89q.worldguard.config.WorldConfiguration;
import com.sk89q.worldguard.protection.flags.Flags;
import io.papermc.lib.PaperLib;
import io.papermc.paper.event.entity.EntityPushedByEntityAttackEvent;
import io.papermc.paper.event.player.PlayerOpenSignEvent;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.ExplosionResult;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Hopper;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.BreezeWindCharge;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Firework;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.WindCharge;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockExpEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityKnockbackByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.potion.PotionEffect;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class EventAbstractionListener extends AbstractListener {

    private final BlockEntityEventDebounce interactDebounce = new BlockEntityEventDebounce(10000);
    private final EntityEntityEventDebounce pickupDebounce = new EntityEntityEventDebounce(10000);
    private final BlockEntityEventDebounce entityBreakBlockDebounce = new BlockEntityEventDebounce(10000);
    private final InventoryMoveItemEventDebounce moveItemDebounce = new InventoryMoveItemEventDebounce(30000);
    private final EventDebounce<BlockPistonRetractKey> pistonRetractDebounce = EventDebounce.create(5000);
    private final EventDebounce<BlockPistonExtendKey> pistonExtendDebounce = EventDebounce.create(5000);

    /**
     * Construct the listener.
     *
     * @param plugin an instance of WorldGuardPlugin
     */
    public EventAbstractionListener(WorldGuardPlugin plugin) {
        super(plugin);
    }

    @Override
    public void registerEvents() {
        super.registerEvents();

        PluginManager pm = getPlugin().getServer().getPluginManager();
        if (PaperLib.isPaper()) {
            pm.registerEvents(new EventAbstractionListener.PaperListener(), getPlugin());
        } else {
            pm.registerEvents(new EventAbstractionListener.SpigotListener(), getPlugin());
        }
    }


    //-------------------------------------------------------------------------
    // Block break / place
    //-------------------------------------------------------------------------

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Events.fireToCancel(event, new BreakBlockEvent(event, create(event.getPlayer()), event.getBlock()));

        if (event.isCancelled()) {
            playDenyEffect(event.getPlayer(), event.getBlock().getLocation().add(0.5, 1, 0.5));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockMultiPlace(BlockMultiPlaceEvent event) {
        List<Block> placed = event.getReplacedBlockStates().stream().map(BlockState::getBlock).collect(Collectors.toList());
        int origAmt = placed.size();
        PlaceBlockEvent delegateEvent = new PlaceBlockEvent(event, create(event.getPlayer()), event.getBlock().getWorld(),
                placed, event.getBlockPlaced().getType());
        Events.fireToCancel(event, delegateEvent);
        if (origAmt != placed.size()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event instanceof BlockMultiPlaceEvent) return;
        BlockState previousState = event.getBlockReplacedState();

        // Some blocks, like tall grass and fire, get replaced
        if (previousState.getType() != Material.AIR && previousState.getType() != event.getBlockReplacedState().getType()) {
            Events.fireToCancel(event, new BreakBlockEvent(event, create(event.getPlayer()), previousState.getLocation(), previousState.getType()));
        }

        ItemStack itemStack = event.getItemInHand();
        if (!event.isCancelled() && itemStack.getType() != Material.AIR) {
            Events.fireToCancel(event, new UseItemEvent(event, create(event.getPlayer()), event.getPlayer().getWorld(), itemStack));
        }

        if (!event.isCancelled()) {
            Events.fireToCancel(event, new PlaceBlockEvent(event, create(event.getPlayer()), event.getBlock()));
        }

        if (event.isCancelled()) {
            playDenyEffect(event.getPlayer(), event.getBlockPlaced().getLocation().add(0.5, 0.5, 0.5));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        Block target = event.getBlock();

        Block[] adjacent = {
                target.getRelative(BlockFace.NORTH),
                target.getRelative(BlockFace.SOUTH),
                target.getRelative(BlockFace.WEST),
                target.getRelative(BlockFace.EAST),
                target.getRelative(BlockFace.UP),
                target.getRelative(BlockFace.DOWN)};

        int found = 0;
        boolean allowed = false;

        for (Block source : adjacent) {
            if (Materials.isFire(source.getType())) {
                found++;
                if (Events.fireAndTestCancel(new BreakBlockEvent(event, create(source), target))) {
                    source.setType(Material.AIR);
                } else {
                    allowed = true;
                }
            }
        }

        if (found > 0 && !allowed) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onStructureGrowEvent(StructureGrowEvent event) {
        int originalCount = event.getBlocks().size();

        Player player = event.getPlayer();
        Events.fireBulkEventToCancel(event, new PlaceBlockEvent(event,
                create(player == null ? event.getLocation().getBlock() : player),
                event.getLocation().getWorld(), event.getBlocks()));

        if (!event.isCancelled() && event.getBlocks().size() != originalCount) {
            event.getLocation().getBlock().setType(Material.AIR);
        }
    }

    private void handleFallingBlock(EntityChangeBlockEvent event, boolean dropItem) {
        Entity entity = event.getEntity();
        Block block = event.getBlock();

        if (entity instanceof FallingBlock) {
            try {
                if (dropItem) {
                    FallingBlock fallingBlock = (FallingBlock) entity;
                    if (!fallingBlock.getDropItem()) return;
                    final Material material = fallingBlock.getBlockData().getMaterial();
                    if (!material.isItem()) return;
                    ItemStack itemStack = new ItemStack(material, 1);
                    Item item = block.getWorld().dropItem(fallingBlock.getLocation(), itemStack);
                    item.setVelocity(new Vector());
                    if (Events.fireAndTestCancel(new SpawnEntityEvent(event, create(block, entity), item))) {
                        item.remove();
                    }
                }
            } finally {
                Cause.untrackParentCause(entity);
            }
        }
    }

    private void setDelegateEventMaterialOptions(DelegateEvent event, Material fromType, Material toType) {
        if (fromType == Material.FARMLAND && toType == Material.DIRT) {
            event.setSilent(true);
            event.getRelevantFlags().add(Flags.TRAMPLE_BLOCKS);
        } else if (Tag.REDSTONE_ORES.isTagged(fromType)) {
            event.setSilent(true);
        } else if (fromType == Material.BIG_DRIPLEAF && toType == Material.BIG_DRIPLEAF) {
            event.setSilent(true);
            event.getRelevantFlags().add(Flags.USE_DRIPLEAF);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        Block block = event.getBlock();
        Entity entity = event.getEntity();
        Material toType = event.getTo();
        Material fromType = block.getType();
        Cause cause = create(entity);

        // Fire two events: one as BREAK and one as PLACE
        if (toType != Material.AIR && fromType != Material.AIR) {
            BreakBlockEvent breakDelegate = new BreakBlockEvent(event, cause, block);
            setDelegateEventMaterialOptions(breakDelegate, fromType, toType);
            boolean denied;
            if (!(denied = Events.fireToCancel(event, breakDelegate))) {
                PlaceBlockEvent placeDelegate = new PlaceBlockEvent(event, cause, block.getLocation(), toType);
                setDelegateEventMaterialOptions(placeDelegate, fromType, toType);
                denied = Events.fireToCancel(event, placeDelegate);
            }
            if (denied && entity instanceof Player) {
                playDenyEffect((Player) entity, block.getLocation());
            }

            handleFallingBlock(event, denied);
        } else if (toType == Material.AIR) {
            // Track the source so later we can create a proper chain of causes
            if (entity instanceof FallingBlock) {
                if (!PaperLib.isPaper()) {
                    // On paper we use FallingBlock#getOrigin to get the origin location, on spigot we store it.
                    Cause.trackParentCause(entity, block);
                }

                // Switch around the event
                Events.fireToCancel(event, new SpawnEntityEvent(event, create(block), entity));
            } else {
                entityBreakBlockDebounce.debounce(
                        block, event.getEntity(), event, new BreakBlockEvent(event, cause, block));
            }
        } else { // toType != Material.AIR && fromType == Material.AIR
            boolean denied = Events.fireToCancel(event, new PlaceBlockEvent(event, cause, block.getLocation(), toType));
            handleFallingBlock(event, denied);
        }

    }

    private static <T  extends EntityEvent & Cancellable> void handleKnockback(T event, Entity damager) {
        final DamageEntityEvent eventToFire = new DamageEntityEvent(event, create(damager), event.getEntity());
        if (damager instanceof BreezeWindCharge) {
            eventToFire.getRelevantFlags().add(Flags.BREEZE_WIND_CHARGE);
        } else if (damager instanceof WindCharge) {
            eventToFire.getRelevantFlags().add(Flags.WIND_CHARGE_BURST);
        }
        Events.fireToCancel(event, eventToFire);
    }

    @SuppressWarnings("UnstableApiUsage")
    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        Entity entity = event.getEntity();
        if (event.getExplosionResult() == ExplosionResult.TRIGGER_BLOCK) {
            UseBlockEvent useEvent = new UseBlockEvent(event, create(entity), event.getLocation().getWorld(), event.blockList(), Material.AIR);
            useEvent.getRelevantFlags().add(Entities.getExplosionFlag(entity));
            useEvent.setSilent(true);
            Events.fireBulkEventToCancel(event, useEvent);
        } else if (event.getExplosionResult() == ExplosionResult.DESTROY || event.getExplosionResult() == ExplosionResult.DESTROY_WITH_DECAY) {
            Events.fireBulkEventToCancel(event, new BreakBlockEvent(event, create(entity), event.getLocation().getWorld(), event.blockList(), Material.AIR));
        }

        if (entity instanceof Creeper) {
            Cause.untrackParentCause(entity);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        if (event.isSticky()) {
            EventDebounce.Entry entry = pistonRetractDebounce.getIfNotPresent(new BlockPistonRetractKey(event), event);
            if (entry != null) {
                Block piston = event.getBlock();
                Cause cause = create(piston);

                BlockFace direction = event.getDirection();

                ArrayList<Block> blocks = new ArrayList<>(event.getBlocks());
                int originalSize = blocks.size();
                Events.fireBulkEventToCancel(event, new BreakBlockEvent(event, cause, event.getBlock().getWorld(), blocks, Material.AIR));
                if (originalSize != blocks.size()) {
                    event.setCancelled(true);
                    return;
                }
                for (Block b : blocks) {
                    Location loc = b.getRelative(direction).getLocation();
                    Events.fireToCancel(event, new PlaceBlockEvent(event, cause, loc, b.getType()));
                }

                entry.setCancelled(event.isCancelled());

                if (event.isCancelled()) {
                    playDenyEffect(piston.getLocation().add(0.5, 1, 0.5));
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        EventDebounce.Entry entry = pistonExtendDebounce.getIfNotPresent(new BlockPistonExtendKey(event), event);
        if (entry != null) {
            Cause cause = create(event.getBlock());
            List<Block> blocks = new ArrayList<>(event.getBlocks());
            int originalLength = blocks.size();
            Events.fireBulkEventToCancel(event, new BreakBlockEvent(event, cause, event.getBlock().getWorld(), blocks, Material.AIR));
            if (originalLength != blocks.size()) {
                event.setCancelled(true);
                return;
            }
            BlockFace dir = event.getDirection();
            for (int i = 0; i < blocks.size(); i++) {
                Block existing = blocks.get(i);
                if (existing.getPistonMoveReaction() == PistonMoveReaction.MOVE
                    || existing.getPistonMoveReaction() == PistonMoveReaction.PUSH_ONLY
                    || existing.getType() == Material.PISTON || existing.getType() == Material.STICKY_PISTON) {
                    blocks.set(i, existing.getRelative(dir));
                }
            }
            Events.fireBulkEventToCancel(event, new PlaceBlockEvent(event, cause, event.getBlock().getWorld(), blocks, Material.STONE));
            if (blocks.size() != originalLength) {
                event.setCancelled(true);
            }
            entry.setCancelled(event.isCancelled());

            if (event.isCancelled()) {
                playDenyEffect(event.getBlock().getLocation().add(0.5, 1, 0.5));
            }
        }
    }

    //-------------------------------------------------------------------------
    // Block external interaction
    //-------------------------------------------------------------------------

    @EventHandler(ignoreCancelled = true)
    public void onBlockDamage(BlockDamageEvent event) {
        Block target = event.getBlock();

        // Previously, and perhaps still, the only way to catch cake eating
        // events was through here
        if (target.getType() == Material.CAKE) {
            Events.fireToCancel(event, new UseBlockEvent(event, create(event.getPlayer()), target));
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        @Nullable ItemStack item = event.getItem();
        Block clicked = event.getClickedBlock();
        Block placed;
        boolean modifiesWorld;
        Cause cause = create(player);

        switch (event.getAction()) {
            case PHYSICAL:
                if (event.useInteractedBlock() != Result.DENY) {
                    if (clicked.getType() == Material.FARMLAND ||
                            clicked.getType() == Material.TURTLE_EGG ||
                            clicked.getType() == Material.SNIFFER_EGG) {
                        BreakBlockEvent breakDelagate = new BreakBlockEvent(event, cause, clicked);
                        breakDelagate.setSilent(true);
                        breakDelagate.getRelevantFlags().add(Flags.TRAMPLE_BLOCKS);
                        boolean denied;
                        if (!(denied = Events.fireToCancel(event, breakDelagate))) {
                            PlaceBlockEvent placeDelegate = new PlaceBlockEvent(event, cause, clicked.getLocation(),
                                    clicked.getType() == Material.FARMLAND ? Material.DIRT : clicked.getType());
                            placeDelegate.setSilent(true);
                            placeDelegate.getRelevantFlags().add(Flags.TRAMPLE_BLOCKS);
                            denied = Events.fireToCancel(event, placeDelegate);
                        }
                        if (denied) {
                            playDenyEffect(player, clicked.getLocation());
                        }
                        return;
                    }
                    DelegateEvent firedEvent = new UseBlockEvent(event, cause, clicked).setAllowed(hasInteractBypass(clicked));
                    if (Tag.REDSTONE_ORES.isTagged(clicked.getType())) {
                        firedEvent.setSilent(true);
                    }
                    if (clicked.getType() == Material.BIG_DRIPLEAF) {
                        firedEvent.getRelevantFlags().add(Flags.USE_DRIPLEAF);
                        firedEvent.setSilent(true);
                    }
                    interactDebounce.debounce(clicked, event.getPlayer(), event, firedEvent);
                    if (event.useInteractedBlock() == Result.DENY && !firedEvent.isSilent()) {
                        playDenyEffect(player, clicked.getLocation().add(0, 1, 0));
                    }
                }
                break;

            case RIGHT_CLICK_BLOCK:
                if (event.useInteractedBlock() != Result.DENY) {
                    placed = clicked.getRelative(event.getBlockFace());

                    // Re-used for dispensers
                    handleBlockRightClick(event, create(event.getPlayer()), item, clicked, placed);
                }

            case LEFT_CLICK_BLOCK:
                if (event.useInteractedBlock() != Result.DENY) {
                    placed = clicked.getRelative(event.getBlockFace());

                    // Only fire events for blocks that are modified when right clicked
                    final boolean hasItemInteraction = item != null && isItemAppliedToBlock(item, clicked)
                            && event.getAction() == Action.RIGHT_CLICK_BLOCK;
                    modifiesWorld = hasItemInteraction
                            || isBlockModifiedOnClick(clicked, event.getAction() == Action.RIGHT_CLICK_BLOCK);

                    if (Events.fireAndTestCancel(new UseBlockEvent(event, cause, clicked).setAllowed(!modifiesWorld))) {
                        event.setUseInteractedBlock(Result.DENY);
                    }

                    // Handle connected blocks (i.e. beds, chests)
                    for (Block connected : Blocks.getConnected(clicked)) {
                        if (Events.fireAndTestCancel(new UseBlockEvent(event, create(event.getPlayer()), connected).setAllowed(!modifiesWorld))) {
                            event.setUseInteractedBlock(Result.DENY);
                            break;
                        }
                    }

                    if (hasItemInteraction) {
                        if (Events.fireAndTestCancel(new PlaceBlockEvent(event, cause, clicked.getLocation(), clicked.getType()))) {
                            event.setUseItemInHand(Result.DENY);
                            event.setUseInteractedBlock(Result.DENY);
                        }
                    }

                    // Special handling of putting out fires
                    if (event.getAction() == Action.LEFT_CLICK_BLOCK && Materials.isFire(placed.getType())) {
                        if (Events.fireAndTestCancel(new BreakBlockEvent(event, create(event.getPlayer()), placed))) {
                            event.setUseInteractedBlock(Result.DENY);
                            break;
                        }
                    }

                    if (event.isCancelled()) {
                        playDenyEffect(event.getPlayer(), clicked.getLocation().add(0.5, 1, 0.5));
                    }
                }

            case LEFT_CLICK_AIR:
            case RIGHT_CLICK_AIR:
                if (event.useItemInHand() != Result.DENY) {
                    if (item != null && !item.getType().isBlock() && Events.fireAndTestCancel(new UseItemEvent(event, cause, player.getWorld(), item))) {
                        event.setUseItemInHand(Result.DENY);
                    }
                }

                // Check for items that the administrator has configured to
                // emit a "use block here" event where the player is
                // standing, which is a hack to protect items that don't
                // throw events
                if (item != null && getWorldConfig(player.getWorld()).blockUseAtFeet.test(item)) {
                    if (Events.fireAndTestCancel(new UseBlockEvent(event, cause, player.getLocation().getBlock()))) {
                        event.setUseInteractedBlock(Result.DENY);
                    }
                }

                break;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityBlockForm(EntityBlockFormEvent event) {
        entityBreakBlockDebounce.debounce(event.getBlock(), event.getEntity(), event,
                new PlaceBlockEvent(event, create(event.getEntity()),
                        event.getBlock().getLocation(), event.getNewState().getType()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityInteract(EntityInteractEvent event) {
        interactDebounce.debounce(event.getBlock(), event.getEntity(), event,
                new UseBlockEvent(event, create(event.getEntity()),
                        event.getBlock()).setAllowed(hasInteractBypass(event.getBlock())));
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockFertilize(BlockFertilizeEvent event) {
        if (event.getBlocks().isEmpty()) return;
        Cause cause = create(event.getPlayer(), event.getBlock());
        Events.fireToCancel(event, new PlaceBlockEvent(event, cause, event.getBlock().getWorld(), event.getBlocks()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        Block block = event.getBlock();
        Cause cause;

        // Find the cause
        if (event.getPlayer() != null) {
            cause = create(event.getPlayer());
        } else if (event.getIgnitingEntity() != null) {
            cause = create(event.getIgnitingEntity());
        } else if (event.getIgnitingBlock() != null) {
            cause = create(event.getIgnitingBlock());
        } else {
            cause = Cause.unknown();
        }

        Events.fireToCancel(event, new PlaceBlockEvent(event, cause, block.getLocation(), Material.FIRE));
    }

    @EventHandler(ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        if (Events.fireToCancel(event, new PlaceBlockEvent(event, create(event.getPlayer()), event.getBlock()))) {
            playDenyEffect(event.getPlayer(), event.getBlock().getLocation().add(0.5, 0.5, 0.5));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBedEnter(PlayerBedEnterEvent event) {
        Events.fireToCancel(event, new UseBlockEvent(event, create(event.getPlayer()), event.getBed()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        Block blockClicked = event.getBlockClicked();
        Block blockAffected;

        if (blockClicked.getBlockData() instanceof Waterlogged) {
            blockAffected = blockClicked;
        } else {
            blockAffected = blockClicked.getRelative(event.getBlockFace());
        }

        boolean allowed = false;

        // Milk buckets can't be emptied as of writing
        if (event.getBucket() == Material.MILK_BUCKET) {
            allowed = true;
        }

        ItemStack item = new ItemStack(event.getBucket(), 1);
        Material blockMaterial = Materials.getBucketBlockMaterial(event.getBucket());
        Events.fireToCancel(event, new PlaceBlockEvent(event, create(player), blockAffected.getLocation(), blockMaterial).setAllowed(allowed));
        Events.fireToCancel(event, new UseItemEvent(event, create(player), player.getWorld(), item).setAllowed(allowed));

        if (event.isCancelled()) {
            playDenyEffect(event.getPlayer(), blockAffected.getLocation().add(0.5, 0.5, 0.5));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        Block blockAffected = event.getBlockClicked().getRelative(event.getBlockFace());
        boolean allowed = false;

        // Milk buckets can't be emptied as of writing
        if (event.getItemStack().getType() == Material.MILK_BUCKET) {
            allowed = true;
        }

        ItemStack item = new ItemStack(event.getBucket(), 1);
        Events.fireToCancel(event, new BreakBlockEvent(event, create(player), blockAffected).setAllowed(allowed));
        Events.fireToCancel(event, new UseItemEvent(event, create(player), player.getWorld(), item).setAllowed(allowed));

        if (event.isCancelled()) {
            playDenyEffect(event.getPlayer(), blockAffected.getLocation().add(0.5, 0.5, 0.5));
        }
    }

    // TODO: Handle EntityPortalEnterEvent

    //-------------------------------------------------------------------------
    // Block self-interaction
    //-------------------------------------------------------------------------

    @EventHandler(ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        WorldConfiguration config = getWorldConfig(event.getBlock().getWorld());

        // This only applies to regions but nothing else cares about high
        // frequency events at the moment
        if (!config.useRegions || (!config.highFreqFlags && !config.checkLiquidFlow)) {
            return;
        }

        Block from = event.getBlock();
        Block to = event.getToBlock();
        Material fromType = from.getType();
        Material toType = to.getType();

        // Liquids pass this event when flowing to solid blocks
        if (Materials.isLiquid(fromType) && toType.isSolid()) {
            return;
        }

        // This significantly reduces the number of events without having
        // too much effect. Unfortunately it appears that even if this
        // check didn't exist, you can raise the level of some liquid
        // flow and the from/to data may not be correct.
        if ((Materials.isWater(fromType) && Materials.isWater(toType)) || (Materials.isLava(fromType) && Materials.isLava(toType))) {
            return;
        }

        Cause cause = create(from);

        // Disable since it's probably not needed
        /*if (from.getType() != Material.AIR) {
            Events.fireToCancel(event, new BreakBlockEvent(event, cause, to));
        }*/

        Events.fireToCancel(event, new PlaceBlockEvent(event, cause, to.getLocation(), from.getType()));
    }

    //-------------------------------------------------------------------------
    // Entity break / place
    //-------------------------------------------------------------------------

    @EventHandler(ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        switch (event.getSpawnReason()) {
            case DISPENSE_EGG:
            case EGG:
            case SPAWNER_EGG:
                if (getWorldConfig(event.getEntity().getWorld()).strictEntitySpawn) {
                    Events.fireToCancel(event, new SpawnEntityEvent(event, Cause.unknown(), event.getEntity()));
                }
                break;
            default:
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event) {
        Events.fireToCancel(event, new SpawnEntityEvent(event, create(event.getPlayer()), event.getEntity()));

        if (event.isCancelled()) {
            Block effectBlock = event.getBlock().getRelative(event.getBlockFace());
            playDenyEffect(event.getPlayer(), effectBlock.getLocation().add(0.5, 0.5, 0.5));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent event) {
        if (event instanceof HangingBreakByEntityEvent) {
            Entity remover = ((HangingBreakByEntityEvent) event).getRemover();
            Events.fireToCancel(event, new DestroyEntityEvent(event, create(remover), event.getEntity()));

            if (event.isCancelled() && remover instanceof Player) {
                playDenyEffect((Player) remover, event.getEntity().getLocation());
            }
        } else if (event.getCause() == HangingBreakEvent.RemoveCause.EXPLOSION) {
            DestroyEntityEvent destroyEntityEvent = new DestroyEntityEvent(event, Cause.unknown(), event.getEntity());
            destroyEntityEvent.getRelevantFlags().add(Flags.OTHER_EXPLOSION);
            if (event.getEntity() instanceof ItemFrame) {
                destroyEntityEvent.getRelevantFlags().add(Flags.ENTITY_ITEM_FRAME_DESTROY);
            } else if (event.getEntity() instanceof Painting) {
                destroyEntityEvent.getRelevantFlags().add(Flags.ENTITY_PAINTING_DESTROY);
            }
            Events.fireToCancel(event, destroyEntityEvent);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        Events.fireToCancel(event, new DestroyEntityEvent(event, create(event.getAttacker()), event.getVehicle()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExp(BlockExpEvent event) {
        if (event.getExpToDrop() > 0) { // Event is raised even where no XP is being dropped
            if (Events.fireAndTestCancel(new SpawnEntityEvent(event, create(event.getBlock()), event.getBlock().getLocation(), EntityType.EXPERIENCE_ORB))) {
                event.setExpToDrop(0);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.FISHING) {
            if (Events.fireAndTestCancel(new UseItemEvent(event, create(event.getPlayer(), event.getHook()),
                    event.getPlayer().getWorld(),
                    event.getPlayer().getInventory().getItem(Objects.requireNonNull(event.getHand()))))) {
                event.setCancelled(true);
            }
        } else if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            if (event.getExpToDrop() > 0 && Events.fireAndTestCancel(new SpawnEntityEvent(event, create(event.getPlayer(), event.getHook()), event.getHook().getLocation(), EntityType.EXPERIENCE_ORB))) {
                event.setExpToDrop(0);
            }
        } else if (event.getState() == PlayerFishEvent.State.CAUGHT_ENTITY) {
            Entity caught = event.getCaught();
            if (caught == null) return;
            if (caught instanceof Item) {
                Events.fireToCancel(event, new DestroyEntityEvent(event, create(event.getPlayer(), event.getHook()), caught));
            } else if (Entities.isConsideredBuildingIfUsed(caught)) {
                Events.fireToCancel(event, new UseEntityEvent(event, create(event.getPlayer(), event.getHook()), caught));
            } else if (!Entities.isHostile(caught) || caught instanceof Player) {
                Events.fireToCancel(event, new DamageEntityEvent(event, create(event.getPlayer(), event.getHook()), caught));
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof FishHook hook && event.getHitEntity() instanceof Entity target) {
            Events.fireToCancel(event, new DamageEntityEvent(event, create(hook), target));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onExpBottle(ExpBottleEvent event) {
        if (Events.fireAndTestCancel(new SpawnEntityEvent(event, create(event.getEntity()), event.getEntity().getLocation(), EntityType.EXPERIENCE_ORB))) {
            event.setExperience(0);

            // Give the player back his or her XP bottle
            ProjectileSource shooter = event.getEntity().getShooter();
            if (shooter instanceof Player) {
                Player player = (Player) shooter;
                if (player.getGameMode() != GameMode.CREATIVE) {
                    player.getInventory().addItem(new ItemStack(Material.EXPERIENCE_BOTTLE, 1));
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getDroppedExp() > 0) {
            if (Events.fireAndTestCancel(new SpawnEntityEvent(event, create(event.getEntity()), event.getEntity().getLocation(), EntityType.EXPERIENCE_ORB))) {
                event.setDroppedExp(0);
            }
        }
    }

    //-------------------------------------------------------------------------
    // Entity external interaction
    //-------------------------------------------------------------------------

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        ItemStack item = event.getHand() == EquipmentSlot.OFF_HAND
                ? player.getInventory().getItemInOffHand() : player.getInventory().getItemInMainHand();
        Entity entity = event.getRightClicked();

        if (Events.fireToCancel(event, new UseItemEvent(event, create(player), world, item))) {
            return;
        }
        final UseEntityEvent useEntityEvent = new UseEntityEvent(event, create(player), entity);
        Material matchingItem = Materials.getRelatedMaterial(entity.getType());
        if (matchingItem != null && hasInteractBypass(world, matchingItem)) {
            useEntityEvent.setAllowed(true);
        }
        Events.fireToCancel(event, useEntityEvent);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event instanceof EntityDamageByBlockEvent) {
            @Nullable Block attacker = ((EntityDamageByBlockEvent) event).getDamager();

            // The attacker should NOT be null, but sometimes it is
            // See WORLDGUARD-3350
            if (attacker != null) {
                Events.fireToCancel(event, new DamageEntityEvent(event, create(attacker), event.getEntity()));
            }

        } else if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent entityEvent = (EntityDamageByEntityEvent) event;
            Entity damager = entityEvent.getDamager();
            final DamageEntityEvent eventToFire = new DamageEntityEvent(event, create(damager), event.getEntity());
            if (damager instanceof Firework) {
                eventToFire.getRelevantFlags().add(Flags.FIREWORK_DAMAGE);
            } else if (damager instanceof Creeper) {
                eventToFire.getRelevantFlags().add(Flags.CREEPER_EXPLOSION);
            }
            if (Events.fireToCancel(event, eventToFire)) {
                if (damager instanceof Tameable && damager instanceof Mob) {
                    ((Mob) damager).setTarget(null);
                }
            }

            // Item use event with the item in hand
            // Older blacklist handler code used this, although it suffers from
            // race problems
            if (damager instanceof Player) {
                // this event doesn't tell us which hand the weapon was in
                ItemStack item = ((Player) damager).getInventory().getItemInMainHand();

                if (item.getType() != Material.AIR) {
                    Events.fireToCancel(event, new UseItemEvent(event, create(damager), event.getEntity().getWorld(), item));
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityCombust(EntityCombustEvent event) {
        if (event instanceof EntityCombustByBlockEvent combustByBlockEvent) {
            // at the time of writing, spigot is throwing null for the event's combuster. this causes lots of issues downstream.
            // whenever (i mean if ever) it is fixed, use getCombuster again instead of the current block
            Block combuster = combustByBlockEvent.getCombuster();
            Events.fireToCancel(event, new DamageEntityEvent(event,
                    create(combuster == null ? event.getEntity().getLocation().getBlock() : combuster), event.getEntity()));
        } else if (event instanceof EntityCombustByEntityEvent) {
            if (event.getEntity() instanceof Arrow) {
                // this only happens from the Flame enchant. igniting arrows in other ways (eg with lava) doesn't even
                // throw the combust event, not even the CombustByBlock event... they're also very buggy and don't even
                // show as lit on the client consistently
                return;
            }
            Events.fireToCancel(event, new DamageEntityEvent(event, create(((EntityCombustByEntityEvent) event).getCombuster()), event.getEntity()));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityLeash(PlayerLeashEntityEvent event) {
        UseEntityEvent useEntityEvent = new UseEntityEvent(event, create(event.getPlayer()), event.getEntity());
        useEntityEvent.getRelevantFlags().add(Flags.RIDE);
        useEntityEvent.getRelevantFlags().add(Flags.INTERACT);
        Events.fireToCancel(event, useEntityEvent);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityUnleash(EntityUnleashEvent event) {
        if (event instanceof PlayerUnleashEntityEvent playerEvent) {
            Events.fireToCancel(playerEvent, new UseEntityEvent(playerEvent, create(playerEvent.getPlayer()), event.getEntity()));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityTame(EntityTameEvent event) {
        Events.fireToCancel(event, new UseEntityEvent(event, create(event.getOwner()), event.getEntity()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerShearEntity(PlayerShearEntityEvent event) {
        Events.fireToCancel(event, new UseEntityEvent(event, create(event.getPlayer()), event.getEntity()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Item item = event.getItem();
        pickupDebounce.debounce(event.getPlayer(), item, event, new DestroyEntityEvent(event, create(event.getPlayer()), event.getItem()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        Item item = event.getItem();
        pickupDebounce.debounce(event.getEntity(), item, event, new DestroyEntityEvent(event, create(event.getEntity()), event.getItem()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Events.fireToCancel(event, new SpawnEntityEvent(event, create(event.getPlayer()), event.getItemDrop()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onVehicleDamage(VehicleDamageEvent event) {
        Entity attacker = event.getAttacker();
        Events.fireToCancel(event, new DamageEntityEvent(event, create(attacker), event.getVehicle()));
    }

    //-------------------------------------------------------------------------
    // Composite events
    //-------------------------------------------------------------------------

    @EventHandler(ignoreCancelled = true)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        Events.fireToCancel(event, new UseItemEvent(event, create(event.getPlayer()), event.getPlayer().getWorld(), event.getItem()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        InventoryHolder holder = PaperLib.getHolder(event.getInventory(), false).getHolder();
        if (holder instanceof Entity && holder == event.getPlayer()) return;

        handleInventoryHolderUse(event, create(event.getPlayer()), holder);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        InventoryHolder causeHolder = PaperLib.getHolder(event.getInitiator(), false).getHolder();

        WorldConfiguration wcfg = null;
        if (causeHolder instanceof Hopper
                && (wcfg = getWorldConfig((((Hopper) causeHolder).getWorld()))).ignoreHopperMoveEvents) {
            return;
        } else if (causeHolder instanceof HopperMinecart
                && (wcfg = getWorldConfig((((HopperMinecart) causeHolder).getWorld()))).ignoreHopperMoveEvents) {
            return;
        }

        Entry entry;

        if ((entry = moveItemDebounce.tryDebounce(event)) != null) {
            InventoryHolder sourceHolder = PaperLib.getHolder(event.getSource(), false).getHolder();
            InventoryHolder targetHolder = PaperLib.getHolder(event.getDestination(), false).getHolder();

            Cause cause;

            if (causeHolder instanceof Entity) {
                cause = create(causeHolder);
            } else if (causeHolder instanceof BlockState) {
                cause = create(((BlockState) causeHolder).getBlock());
            } else {
                cause = Cause.unknown();
            }

            if (causeHolder != null && !causeHolder.equals(sourceHolder)) {
                handleInventoryHolderUse(event, cause, sourceHolder);
            }

            if (causeHolder != null && !causeHolder.equals(targetHolder)) {
                handleInventoryHolderUse(event, cause, targetHolder);
            }

            if (event.isCancelled() && causeHolder instanceof Hopper && wcfg.breakDeniedHoppers) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(),
                        () -> ((Hopper) causeHolder).getBlock().breakNaturally());
            } else {
                entry.setCancelled(event.isCancelled());
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent event) {
        Entity entity = event.getEntity();
        ThrownPotion potion = event.getPotion();
        World world = entity.getWorld();
        Cause cause = create(potion);

        // Fire item interaction event
        Events.fireToCancel(event, new UseItemEvent(event, cause, world, potion.getItem()));
        // Fire entity interaction event
        if (!event.isCancelled()) {
            int blocked = 0;
            int affectedSize = event.getAffectedEntities().size();
            boolean hasDamageEffect = Materials.hasDamageEffect(potion.getEffects());

            for (LivingEntity affected : event.getAffectedEntities()) {
                DelegateEvent delegate = hasDamageEffect
                        ? new DamageEntityEvent(event, cause, affected) :
                        new UseEntityEvent(event, cause, affected);

                // Consider extra relevant flags
                delegate.getRelevantFlags().add(Flags.POTION_SPLASH);
                if (potion.getShooter() instanceof LivingEntity shooter && !(shooter instanceof Player) && affected instanceof Player) {
                    delegate.getRelevantFlags().add(Flags.MOB_DAMAGE);
                }

                if (Events.fireAndTestCancel(delegate)) {
                    event.setIntensity(affected, 0);
                    blocked++;
                }
            }

            if (affectedSize > 0 && blocked == affectedSize) { // server does weird things with this if the event is modified, so use cached number
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockDispense(BlockDispenseEvent event) {
        Block dispenserBlock = event.getBlock();

        // Simulate right click event as players have it
        if (dispenserBlock.getType() == Material.DISPENSER) {
            Cause cause = create(event.getBlock());
            ItemStack item = event.getItem();
            if (Events.fireToCancel(event, new UseItemEvent(event, cause, dispenserBlock.getWorld(), item))) {
                return;
            }

            BlockData blockData = dispenserBlock.getBlockData();
            Dispenser dispenser = (Dispenser) blockData; // if this ClassCastExceptions it's a bukkit bug
            Block placed = dispenserBlock.getRelative(dispenser.getFacing());
            Block clicked = placed.getRelative(dispenser.getFacing());
            handleBlockRightClick(event, cause, item, clicked, placed);

            // handle special dispenser behavior
            if (Materials.isShulkerBox(item.getType())) {
                if (Events.fireToCancel(event, new PlaceBlockEvent(event, cause, placed.getLocation(), item.getType()))) {
                    playDenyEffect(placed.getLocation());
                }
            } else if (isItemAppliedToBlock(item, placed)) {
                if (Events.fireToCancel(event, new PlaceBlockEvent(event, cause, placed.getLocation(), placed.getType()))) {
                    playDenyEffect(placed.getLocation());
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onLingeringSplash(LingeringPotionSplashEvent event) {
        AreaEffectCloud aec = event.getAreaEffectCloud();
        ThrownPotion potion = event.getEntity();
        World world = potion.getWorld();
        Cause cause = create(event.getEntity());

        // Fire item interaction event
        Events.fireToCancel(event, new UseItemEvent(event, cause, world, potion.getItem()));

        // Fire entity spawn event
        if (!event.isCancelled()) {
            // radius unfortunately doesn't go through with this, so only a single location is tested
            Events.fireToCancel(event, new SpawnEntityEvent(event, cause, aec.getLocation().add(0.5, 0, 0.5), EntityType.AREA_EFFECT_CLOUD));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onLingeringApply(AreaEffectCloudApplyEvent event) {
        AreaEffectCloud entity = event.getEntity();
        List<PotionEffect> effects = new ArrayList<>();
        List<PotionEffect> baseEffectTypes = entity.getBasePotionType() == null ? null : entity.getBasePotionType().getPotionEffects();
        if (baseEffectTypes != null) {
            effects.addAll(baseEffectTypes);
        }
        if (entity.hasCustomEffects()) {
            effects.addAll(entity.getCustomEffects());
        }
        if (!Materials.hasDamageEffect(effects)) {
            return;
        }
        Cause cause = create(event.getEntity());
        event.getAffectedEntities()
                .removeIf(victim -> Events.fireAndTestCancel(new DamageEntityEvent(event, cause, victim)));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        onPlayerInteractEntity(event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        final BreakBlockEvent eventToFire = new BreakBlockEvent(event, create(event.getBlock()),
                event.getBlock().getLocation().getWorld(), event.blockList(), Material.AIR);
        eventToFire.getRelevantFlags().add(Flags.OTHER_EXPLOSION);
        Events.fireBulkEventToCancel(event, eventToFire);
    }

    @EventHandler(ignoreCancelled = true)
    public void onTakeLecternBook(PlayerTakeLecternBookEvent event) {
        final UseBlockEvent useEvent = new UseBlockEvent(event, create(event.getPlayer()), event.getLectern().getBlock());
        Events.fireToCancel(event, useEvent);
    }

    @EventHandler(ignoreCancelled = true)
    public void onCauldronLevelChange(CauldronLevelChangeEvent event) {
        if (event.getEntity() == null) return;
        interactDebounce.debounce(event.getBlock(), event.getEntity(), event,
                new UseBlockEvent(event, create(event.getEntity()),
                        event.getBlock()).setAllowed(hasInteractBypass(event.getBlock())));
    }

    /**
     * Handle the right click of a block while an item is held.
     *
     * @param event the original event
     * @param cause the list of cause
     * @param item the item
     * @param placed the placed block
     * @param <T> the event type
     */
    private static <T extends Event & Cancellable> void handleBlockRightClick(T event, Cause cause, @Nullable ItemStack item, Block clicked, Block placed) {
        if (item != null && item.getType() == Material.TNT) {
            // Workaround for a bug that allowed TNT to trigger instantly if placed
            // next to redstone, without plugins getting the clicked place event
            // (not sure if this actually still happens) -- note Jun 2019 - happens with dispensers still, tho not players
            Events.fireToCancel(event, new UseBlockEvent(event, cause, clicked.getLocation(), Material.TNT));

            // Workaround for http://leaky.bukkit.org/issues/1034
            Events.fireToCancel(event, new PlaceBlockEvent(event, cause, placed.getLocation(), Material.TNT));
            return;
        }

        // Handle created Minecarts
        if (item != null && Materials.isMinecart(item.getType())) {
            EntityType entityType = Materials.getRelatedEntity(item.getType());
            if (entityType == null) {
                entityType = EntityType.MINECART;
            }
            Events.fireToCancel(event, new SpawnEntityEvent(event, cause, clicked.getLocation().add(0.5, 0, 0.5), entityType));
            return;
        }

        // Handle created boats
        if (item != null && Materials.isBoat(item.getType())) {
            Events.fireToCancel(event, new SpawnEntityEvent(event, cause, placed.getLocation().add(0.5, 0, 0.5), Materials.getRelatedEntity(item.getType())));
            return;
        }

        // Handle created armor stands
        if (item != null && item.getType() == Material.ARMOR_STAND) {
            Events.fireToCancel(event, new SpawnEntityEvent(event, cause, placed.getLocation().add(0.5, 0, 0.5), EntityType.ARMOR_STAND));
            return;
        }

        if (item != null && item.getType() == Material.END_CRYSTAL) { /*&& placed.getType() == Material.BEDROCK) {*/ // in vanilla you can only place them on bedrock but who knows what plugins will add
                                                                                                                        // may be overprotective as a result, but better than being underprotective
            Events.fireToCancel(event, new SpawnEntityEvent(event, cause, placed.getLocation().add(0.5, 0, 0.5), EntityType.END_CRYSTAL));
            return;
        }

        // Handle created spawn eggs
        if (item != null) {
            EntityType possibleEntityType = Materials.getEntitySpawnEgg(item.getType());
            if (possibleEntityType != null) {
                Events.fireToCancel(event, new SpawnEntityEvent(event, cause, placed.getLocation().add(0.5, 0, 0.5), possibleEntityType));
                return;
            }
        }

        // handle water/lava placement
        if (item != null && (item.getType() == Material.WATER_BUCKET || item.getType() == Material.LAVA_BUCKET)) {
            Events.fireToCancel(event, new PlaceBlockEvent(event, cause, placed.getLocation(),
                    item.getType() == Material.WATER_BUCKET ? Material.WATER : Material.LAVA));
            return;
        }
    }

    private static <T extends Event & Cancellable> void handleInventoryHolderUse(T originalEvent, Cause cause, InventoryHolder holder) {
        if (originalEvent.isCancelled()) {
            return;
        }

        if (holder instanceof Entity entity) {
            Material mat = Materials.getRelatedMaterial((entity).getType());
            UseEntityEvent useEntityEvent = new UseEntityEvent(originalEvent, cause, entity);
            if (mat != null && hasInteractBypass((entity).getWorld(), mat)) {
                useEntityEvent.setAllowed(true);
            }
            Events.fireToCancel(originalEvent, useEntityEvent);
        } else {
            if (holder instanceof BlockState block && block.isPlaced()) {
                final UseBlockEvent useBlockEvent = new UseBlockEvent(originalEvent, cause, block.getBlock());
                if (hasInteractBypass(block.getWorld(), block.getType())) {
                    useBlockEvent.setAllowed(true);
                }
                Events.fireToCancel(originalEvent, useBlockEvent);
            } else if (holder instanceof DoubleChest doubleChest) {
                InventoryHolder left = PaperLib.isPaper() ? doubleChest.getLeftSide(false) : doubleChest.getLeftSide();
                InventoryHolder right = PaperLib.isPaper() ? doubleChest.getRightSide(false) : doubleChest.getRightSide();
                if (left instanceof Chest) {
                    Events.fireToCancel(originalEvent, new UseBlockEvent(originalEvent, cause, ((Chest) left).getBlock()));
                }
                if (right instanceof Chest) {
                    Events.fireToCancel(originalEvent, new UseBlockEvent(originalEvent, cause, ((Chest) right).getBlock()));
                }
            }
        }
    }

    private static boolean hasInteractBypass(Block block) {
        return getWorldConfig(block.getWorld()).allowAllInteract.test(block);
    }

    private static boolean hasInteractBypass(World world, Material material) {
        return getWorldConfig(world).allowAllInteract.test(material);
    }

    private static boolean hasInteractBypass(World world, ItemStack item) {
        return getWorldConfig(world).allowAllInteract.test(item);
    }

    private static boolean isBlockModifiedOnClick(Block block, boolean rightClick) {
        return Materials.isBlockModifiedOnClick(block.getType(), rightClick) && !hasInteractBypass(block);
    }

    private static boolean isItemAppliedToBlock(ItemStack item, Block clicked) {
        return Materials.isItemAppliedToBlock(item.getType(), clicked.getType())
                && !hasInteractBypass(clicked)
                && !hasInteractBypass(clicked.getWorld(), item);
    }

    private static void playDenyEffect(Player player, Location location) {
        //player.playSound(location, Sound.SUCCESSFUL_HIT, 0.2f, 0.4f);
        if (getConfig().particleEffects) {
            player.playEffect(location, Effect.SMOKE, BlockFace.UP);
        }
    }

    private static void playDenyEffect(Location location) {
        if (getConfig().particleEffects) {
            location.getWorld().playEffect(location, Effect.SMOKE, BlockFace.UP);
        }
    }

    private static class PaperListener implements Listener {
        @EventHandler(ignoreCancelled = true)
        public void onEntityTransform(EntityZapEvent event) {
            Events.fireToCancel(event, new DamageEntityEvent(event, create(event.getBolt()), event.getEntity()));
        }

        @EventHandler(ignoreCancelled = true)
        public void onSignOpen(PlayerOpenSignEvent event) {
            if (event.getCause() == PlayerOpenSignEvent.Cause.INTERACT) {
                // other cases are handled by other events
                Events.fireToCancel(event, new UseBlockEvent(event, create(event.getPlayer()), event.getSign().getBlock()));
            }
        }

        @EventHandler(ignoreCancelled = true)
        public void onEntityKnockbackByEntity(EntityPushedByEntityAttackEvent event) {
            handleKnockback(event, event.getPushedBy());
        }
    }

    @SuppressWarnings("removal")
    private static class SpigotListener implements Listener {
        @EventHandler(ignoreCancelled = true)
        public void onEntityKnockbackByEntity(EntityKnockbackByEntityEvent event) {
            handleKnockback(event, event.getSourceEntity());
        }
    }
}
