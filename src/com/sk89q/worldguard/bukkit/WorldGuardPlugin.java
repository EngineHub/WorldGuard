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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.Server;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import com.sk89q.worldguard.protection.*;

/**
 * Plugin for Bukkit.
 * 
 * @author sk89qs
 */
public class WorldGuardPlugin extends JavaPlugin {
    private static final Logger logger = Logger.getLogger("Minecraft.WorldGuard");
    
    private final WorldGuardPlayerListener playerListener =
        new WorldGuardPlayerListener(this);
    private final WorldGuardBlockListener blockListener =
        new WorldGuardBlockListener(this);
    private final WorldGuardEntityListener entityListener =
        new WorldGuardEntityListener(this);

    RegionManager regionManager = new FlatRegionManager();
    ProtectionDatabase regionLoader;
    
    Set<String> invinciblePlayers = new HashSet<String>();
    Set<String> amphibiousPlayers = new HashSet<String>();
    boolean fireSpreadDisableToggle;
    
    // Configuration follows
    
    boolean enforceOneSession;
    boolean itemDurability;
    Set<Integer> itemDropBlacklist;

    boolean classicWater;
    boolean simulateSponge;
    int spongeRadius;

    boolean noPhysicsGravel;
    boolean noPhysicsSand;
    boolean allowPortalAnywhere;
    Set<Integer> preventWaterDamage;

    boolean blockTNT;
    boolean blockLighter;

    boolean disableFireSpread;
    Set<Integer> disableFireSpreadBlocks;
    boolean preventLavaFire;
    Set<Integer> allowedLavaSpreadOver;
    
    boolean blockCreeperExplosions;

    int loginProtection;
    int spawnProtection;
    boolean kickOnDeath;
    boolean exactRespawn;
    boolean teleportToHome;
    
    boolean disableFallDamage;
    boolean disableLavaDamage;
    boolean disableFireDamage;
    boolean disableDrowningDamage;
    boolean disableSuffocationDamage;
    boolean teleportOnSuffocation;

    boolean useRegions;
    int regionWand = 287; 
    
    public WorldGuardPlugin(PluginLoader pluginLoader, Server instance,
            PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
        super(pluginLoader, instance, desc, folder, plugin, cLoader);

        logger.info("WorldGuard " + desc.getVersion() + " loaded.");
        
        folder.mkdirs();

        regionLoader = new CSVDatabase(new File(folder, "regions.txt"));
        loadConfiguration();
        registerEvents();
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    private void registerEvents() {
        registerEvent(Event.Type.BLOCK_DAMAGED, blockListener, Priority.Normal);
        registerEvent(Event.Type.BLOCK_FLOW, blockListener, Priority.Normal);
        registerEvent(Event.Type.BLOCK_IGNITE, blockListener, Priority.Normal);
        registerEvent(Event.Type.BLOCK_PHYSICS, blockListener, Priority.Normal);
        registerEvent(Event.Type.BLOCK_PLACED, blockListener, Priority.Normal);
        registerEvent(Event.Type.BLOCK_RIGHTCLICKED, blockListener, Priority.Normal);

        registerEvent(Event.Type.ENTITY_DAMAGEDBY_BLOCK, entityListener, Priority.Normal);
        registerEvent(Event.Type.ENTITY_DAMAGEDBY_ENTITY, entityListener, Priority.Normal);

        registerEvent(Event.Type.PLAYER_COMMAND, playerListener, Priority.Normal);
        registerEvent(Event.Type.PLAYER_ITEM, playerListener, Priority.Normal);
        registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal);
        registerEvent(Event.Type.PLAYER_LOGIN, playerListener, Priority.Normal);
        registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Normal);
    }
    
    private void registerEvent(Event.Type type, Listener listener, Priority priority) {
        getServer().getPluginManager().registerEvent(type, listener, priority, this);
    }

    /**
     * Load the configuration
     */
    public void loadConfiguration() {
        Configuration config = getConfiguration();

        enforceOneSession = config.getBoolean("protection.enforce-single-session", true);
        itemDurability = config.getBoolean("protection.item-durability", true);
        itemDropBlacklist = new HashSet<Integer>(config.getIntList("protection.item-drop-blacklist", null));
        
        classicWater = config.getBoolean("simulation.classic-water", false);
        simulateSponge = config.getBoolean("simulation.sponge.enable", true);
        spongeRadius = Math.max(1, config.getInt("simulation.sponge.radius", 3)) - 1;
        
        noPhysicsGravel = config.getBoolean("physics.no-physics-gravel", false);
        noPhysicsSand = config.getBoolean("physics.no-physics-sand", false);
        allowPortalAnywhere = config.getBoolean("physics.allow-portal-anywhere", false);
        preventWaterDamage = new HashSet<Integer>(config.getIntList("physics.disable-water-damage-blocks", null));
        
        blockTNT = config.getBoolean("ignition.block-tnt", false);
        blockLighter = config.getBoolean("ignition.block-lighter", false);
        
        preventLavaFire = config.getBoolean("fire.disable-lava-fire-spread", true);
        disableFireSpread = config.getBoolean("fire.disable-all-fire-spread", false);
        disableFireSpreadBlocks = new HashSet<Integer>(config.getIntList("fire.disable-fire-spread-blocks", null));
        allowedLavaSpreadOver = new HashSet<Integer>(config.getIntList("fire.lava-spread-blocks", null));
        
        blockCreeperExplosions = config.getBoolean("mobs.block-creeper-explosions", false);
        
        loginProtection = config.getInt("spawn.login-protection", 3);
        spawnProtection = config.getInt("spawn.spawn-protection", 0);
        kickOnDeath = config.getBoolean("spawn.kick-on-death", false);
        exactRespawn = config.getBoolean("spawn.exact-respawn", false);
        teleportToHome = config.getBoolean("spawn.teleport-to-home-on-death", false);
        
        disableFallDamage = config.getBoolean("player-damage.disable-fall-damage", false);
        disableLavaDamage = config.getBoolean("player-damage.disable-lava-damage", false);
        disableFireDamage = config.getBoolean("player-damage.disable-fire-damage", false);
        disableDrowningDamage = config.getBoolean("player-damage.disable-water-damage", false);
        disableSuffocationDamage = config.getBoolean("player-damage.disable-suffocation-damage", false);
        teleportOnSuffocation = config.getBoolean("player-damage.teleport-on-suffocation", false);
        
        useRegions = config.getBoolean("regions.enable", true);
        regionWand = config.getInt("regions.wand", 287);

        try {
            regionLoader.load();
            regionManager.setRegions(regionLoader.getRegions());
        } catch (IOException e) {
            logger.warning("WorldGuard: Failed to load regions: "
                    + e.getMessage());
        }

        // Print an overview of settings
        if (config.getBoolean("summary-on-start", true)) {
            logger.log(Level.INFO, enforceOneSession ? "WorldGuard: Single session is enforced."
                    : "WorldGuard: Single session is NOT ENFORCED.");
            logger.log(Level.INFO, blockTNT ? "WorldGuard: TNT ignition is blocked."
                    : "WorldGuard: TNT ignition is PERMITTED.");
            logger.log(Level.INFO, blockLighter ? "WorldGuard: Lighters are blocked."
                    : "WorldGuard: Lighters are PERMITTED.");
            logger.log(Level.INFO, preventLavaFire ? "WorldGuard: Lava fire is blocked."
                    : "WorldGuard: Lava fire is PERMITTED.");
            if (disableFireSpread) {
                logger.log(Level.INFO, "WorldGuard: All fire spread is disabled.");
            } else {
                if (disableFireSpreadBlocks != null) {
                    logger.log(Level.INFO, "WorldGuard: Fire spread is limited to "
                            + disableFireSpreadBlocks.size() + " block types.");
                } else {
                    logger.log(Level.INFO, "WorldGuard: Fire spread is UNRESTRICTED.");
                }
            }
        }
    }
    
    boolean inGroup(Player player, String group) {
        return true;
    }
    
    boolean hasPermission(Player player, String perm) {
        return !perm.equals("/regionbypass");
    }
    
    List<String> getGroups(Player player) {
        return new ArrayList<String>();
    }

    BukkitPlayer wrapPlayer(Player player) {
        return new BukkitPlayer(this, player);
    }
}
