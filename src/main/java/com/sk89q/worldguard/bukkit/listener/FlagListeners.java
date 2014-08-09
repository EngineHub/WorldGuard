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

import com.sk89q.worldguard.bukkit.ConfigurationManager;
import com.sk89q.worldguard.bukkit.WorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.listener.module.BlockFadeListener;
import com.sk89q.worldguard.bukkit.listener.module.BlockFlowListener;
import com.sk89q.worldguard.bukkit.listener.module.BlockFormListener;
import com.sk89q.worldguard.bukkit.listener.module.BlockIgniteListener;
import com.sk89q.worldguard.bukkit.listener.module.BlockInteractListener;
import com.sk89q.worldguard.bukkit.listener.module.BlockPhysicsListener;
import com.sk89q.worldguard.bukkit.listener.module.BlockSpreadListener;
import com.sk89q.worldguard.bukkit.listener.module.FireSpreadListener;
import com.sk89q.worldguard.bukkit.listener.module.FlintAndSteelListener;
import com.sk89q.worldguard.bukkit.listener.module.ItemDurabilityListener;
import com.sk89q.worldguard.bukkit.listener.module.LavaSpreadLimiterListener;
import com.sk89q.worldguard.bukkit.listener.module.LeafDecayListener;
import com.sk89q.worldguard.bukkit.listener.module.ObsidianGeneratorListener;
import com.sk89q.worldguard.bukkit.listener.module.PistonMoveListener;
import com.sk89q.worldguard.bukkit.listener.module.SpongeListener;
import com.sk89q.worldguard.bukkit.listener.module.TickHaltingListener;
import com.sk89q.worldguard.bukkit.listener.module.WaterProtectionListener;
import com.sk89q.worldguard.bukkit.listener.module.XPDropListener;
import com.sk89q.worldguard.protection.flags.StateFlag;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowman;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;

import javax.annotation.Nullable;
import java.util.Set;

import static com.sk89q.worldguard.bukkit.listener.Materials.isMushroom;
import static com.sk89q.worldguard.bukkit.listener.module.FireSpreadListener.INDIRECT_IGNITE_CHECK;
import static com.sk89q.worldguard.bukkit.listener.module.FireSpreadListener.VISIT_ADJACENT;
import static com.sk89q.worldguard.protection.flags.DefaultFlag.*;

/**
 * Implements the listeners for flags and configuration options.
 */
public class FlagListeners {

    private final WorldGuardPlugin plugin;

    public FlagListeners(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    private ConfigurationManager getConfig() {
        return plugin.getGlobalStateManager();
    }

    private WorldConfiguration getConfig(World world) {
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        return cfg.get(world);
    }

    private WorldConfiguration getConfig(Location location) {
        return getConfig(location.getWorld());
    }

    private WorldConfiguration getConfig(Block block) {
        return getConfig(block.getWorld());
    }

    private WorldConfiguration getConfig(BlockState state) {
        return getConfig(state.getWorld());
    }

    private boolean testState(Location location, StateFlag flag) {
        return plugin.getGlobalRegionManager().allows(flag, location);
    }

    private boolean testState(Block block, StateFlag flag) {
        return plugin.getGlobalRegionManager().allows(flag, block.getLocation());
    }

    private boolean testState(BlockState state, StateFlag flag) {
        return plugin.getGlobalRegionManager().allows(flag, state.getLocation());
    }

    private boolean testCanBuild(Player player, Block block) {
        return plugin.getGlobalRegionManager().canBuild(player, block);
    }

    private boolean isNonEmptyAndContains(Set<Integer> blocks, Material material) {
        return !blocks.isEmpty() && blocks.contains(material.getId());
    }

    private boolean isNonEmptyOrContains(Set<Integer> blocks, Material material) {
        return !blocks.isEmpty() || blocks.contains(material.getId());
    }

    private boolean isPlayer(Entity entity) {
        return entity instanceof Player;
    }

    private boolean testPermission(@Nullable Player player, String permission, boolean nonPlayerPermitted) {
        if (player == null) {
            return nonPlayerPermitted;
        }

        return plugin.hasPermission(player, permission);
    }

    public void registerEvents() {
        // Block decay
        registerEvents(new BlockFadeListener(b -> b.getType() == Material.ICE && (getConfig(b).disableIceMelting || !testState(b, ICE_MELT))));
        registerEvents(new BlockFadeListener(b -> b.getType() == Material.SNOW && (getConfig(b).disableSnowMelting || !testState(b, SNOW_MELT))));
        registerEvents(new BlockFadeListener(b -> b.getType() == Material.SOIL && (getConfig(b).disableSoilDehydration || !testState(b, SOIL_DRY))));
        registerEvents(new LeafDecayListener(b -> getConfig(b).disableLeafDecay || !testState(b, LEAF_DECAY)));

        // Block spread
        registerEvents(new BlockSpreadListener(b -> isMushroom(b.getType()) && (getConfig(b).disableMushroomSpread || !testState(b, MUSHROOMS))));
        registerEvents(new BlockSpreadListener(b -> b.getType() == Material.GRASS && (getConfig(b).disableGrassGrowth || !testState(b, GRASS_SPREAD))));
        registerEvents(new BlockSpreadListener(b -> b.getType() == Material.MYCEL && (getConfig(b).disableMyceliumSpread || !testState(b, MYCELIUM_SPREAD))));
        registerEvents(new BlockSpreadListener(b -> b.getType() == Material.VINE && (getConfig(b).disableVineGrowth || !testState(b, VINE_GROWTH))));

        // Block form
        registerEvents(new BlockFormListener((b, e) -> e instanceof Snowman && getConfig(b).disableSnowmanTrails));
        registerEvents(new BlockFormListener((b, e) -> b.getType() == Material.ICE && (getConfig(b).disableIceFormation || !testState(b, ICE_FORM))));
        registerEvents(new BlockFormListener((b, e) -> b.getType() == Material.SNOW && (getConfig(b).disableSnowFormation || !testState(b, SNOW_FALL))));
        registerEvents(new BlockFormListener((b, e) -> isNonEmptyAndContains(getConfig(b).allowedSnowFallOver, b.getBlock().getRelative(0, -1, 0).getType())));

        // Block flow
        registerEvents(new BlockFlowListener(b -> Materials.isWater(b.getType()) && getConfig(b).highFreqFlags && !testState(b, WATER_FLOW)));
        registerEvents(new BlockFlowListener(b -> Materials.isLava(b.getType()) && getConfig(b).highFreqFlags && !testState(b, LAVA_FLOW)));

        // Ignite
        registerEvents(new BlockIgniteListener(b -> getConfig(b).preventLightningFire || (getConfig(b).highFreqFlags && !testState(b, LIGHTNING)), IgniteCause.LIGHTNING));
        registerEvents(new BlockIgniteListener(b -> getConfig(b).preventLavaFire || (getConfig(b).highFreqFlags && !testState(b, LAVA_FIRE)), IgniteCause.LAVA));
        registerEvents(new BlockIgniteListener(b -> getConfig(b).highFreqFlags && !testState(b, GHAST_FIREBALL), IgniteCause.FIREBALL));

        // Fire spread
        registerEvents(new FireSpreadListener(b -> getConfig(b).disableFireSpread, 0));
        registerEvents(new FireSpreadListener(b -> getConfig(b).fireSpreadDisableToggle || (getConfig(b).highFreqFlags && !testState(b, FIRE_SPREAD)), VISIT_ADJACENT));
        registerEvents(new FireSpreadListener(b -> isNonEmptyAndContains(getConfig(b).disableFireSpreadBlocks, b.getType()), VISIT_ADJACENT | INDIRECT_IGNITE_CHECK));

        // Physics
        registerEvents(new BlockPhysicsListener((b, m) -> m == Material.GRAVEL && getConfig(b).noPhysicsGravel));
        registerEvents(new BlockPhysicsListener((b, m) -> m == Material.SAND && getConfig(b).noPhysicsSand));
        registerEvents(new BlockPhysicsListener((b, m) -> Materials.isPortal(m) && getConfig(b).allowPortalAnywhere));
        registerEvents(new BlockPhysicsListener((b, m) -> m == Material.LADDER && getConfig(b).ropeLadders && b.getRelative(0, 1, 0).getType() == Material.LADDER));

        // Flint and steel
        registerEvents(new FlintAndSteelListener((p, b) -> (
                getConfig(b).blockLighter || (!testState(b, LIGHTER) && !testCanBuild(p, b)))
                && !testPermission(p, "worldguard.override.lighter", true)));

        // Miscellaneous
        registerEvents(new PistonMoveListener((b, sticky) -> sticky && testState(b, PISTONS)));
        registerEvents(new XPDropListener((l, amt) -> getConfig(l).disableExpDrops || !testState(l, EXP_DROPS)));
        registerEvents(new BlockInteractListener((b, e) -> b.getType() == Material.SOIL && !isPlayer(e) && getConfig(b).disableCreatureCropTrampling));
        registerEvents(new BlockInteractListener((b, e) -> b.getType() == Material.SOIL && isPlayer(e) && getConfig(b).disablePlayerCropTrampling));

        // Other options
        registerEvents(new TickHaltingListener(c -> getConfig().activityHaltToggle));
        registerEvents(new SpongeListener(w -> getConfig(w).spongeBehavior));
        registerEvents(new ItemDurabilityListener(w -> getConfig(w).itemDurability));
        registerEvents(new WaterProtectionListener(b -> isNonEmptyAndContains(getConfig(b).preventWaterDamage, b.getType())));
        registerEvents(new LavaSpreadLimiterListener(b -> isNonEmptyOrContains(getConfig(b).allowedLavaSpreadOver, b.getType())));
        registerEvents(new ObsidianGeneratorListener(b -> getConfig(b).disableObsidianGenerators));
    }

    private void registerEvents(Listener listener) {
        Bukkit.getServer().getPluginManager().registerEvents(listener, plugin);
    }

}
