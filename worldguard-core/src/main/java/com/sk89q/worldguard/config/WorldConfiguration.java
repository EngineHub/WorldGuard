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

package com.sk89q.worldguard.config;

import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldedit.world.registry.LegacyMapper;
import com.sk89q.worldguard.blacklist.Blacklist;
import com.sk89q.worldedit.util.report.Unreported;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Holds the configuration for individual worlds.
 *
 * @author sk89q
 * @author Michael
 */
public abstract class WorldConfiguration {

    public static final Logger log = Logger.getLogger(WorldConfiguration.class.getCanonicalName());

    public static final String CONFIG_HEADER = "#\r\n" +
            "# WorldGuard's world configuration file\r\n" +
            "#\r\n" +
            "# This is a world configuration file. Anything placed into here will only\r\n" +
            "# affect this world. If you don't put anything in this file, then the\r\n" +
            "# settings will be inherited from the main configuration file.\r\n" +
            "#\r\n" +
            "# If you see {} below, that means that there are NO entries in this file.\r\n" +
            "# Remove the {} and add your own entries.\r\n" +
            "#\r\n";

    @Unreported private String worldName;
    protected File blacklistFile;

    @Unreported protected Blacklist blacklist;

    public boolean boundedLocationFlags;
    public boolean useRegions;
    public boolean simulateSponge;
    public int spongeRadius;
    public boolean redstoneSponges;
    public boolean summaryOnStart;
    public boolean opPermissions;
    public boolean buildPermissions;
    public String buildPermissionDenyMessage = "";
    public boolean fireSpreadDisableToggle;
    public boolean itemDurability;
    public boolean disableExpDrops;
    public boolean blockPotionsAlways;
    public boolean pumpkinScuba;
    public boolean noPhysicsGravel;
    public boolean noPhysicsSand;
    public boolean ropeLadders;
    public boolean allowPortalAnywhere;
    public Set<String> preventWaterDamage;
    public boolean blockLighter;
    public boolean disableFireSpread;
    public Set<String> disableFireSpreadBlocks;
    public boolean preventLavaFire;
    public Set<String> allowedLavaSpreadOver;
    public boolean blockTNTExplosions;
    public boolean blockTNTBlockDamage;
    public boolean blockCreeperExplosions;
    public boolean blockCreeperBlockDamage;
    public boolean blockWitherExplosions;
    public boolean blockWitherBlockDamage;
    public boolean blockWitherSkullExplosions;
    public boolean blockWitherSkullBlockDamage;
    public boolean blockEnderDragonBlockDamage;
    public boolean blockEnderDragonPortalCreation;
    public boolean blockFireballExplosions;
    public boolean blockFireballBlockDamage;
    public boolean blockOtherExplosions;
    public boolean blockEntityPaintingDestroy;
    public boolean blockEntityItemFrameDestroy;
    public boolean blockEntityArmorStandDestroy;
    public boolean blockPluginSpawning;
    public boolean blockGroundSlimes;
    public boolean blockZombieDoorDestruction;
    public boolean disableContactDamage;
    public boolean disableFallDamage;
    public boolean disableLavaDamage;
    public boolean disableFireDamage;
    public boolean disableLightningDamage;
    public boolean disableDrowningDamage;
    public boolean disableSuffocationDamage;
    public boolean teleportOnSuffocation;
    public boolean disableVoidDamage;
    public boolean teleportOnVoid;
    public boolean disableExplosionDamage;
    public boolean disableMobDamage;
    public boolean highFreqFlags;
    public boolean checkLiquidFlow;
    public String regionWand;
    public Set<EntityType> blockCreatureSpawn;
    public boolean allowTamedSpawns;
    public int maxClaimVolume;
    public boolean claimOnlyInsideExistingRegions;
    public int maxRegionCountPerPlayer;
    public boolean antiWolfDumbness;
    public boolean signChestProtection;
    public boolean disableSignChestProtectionCheck;
    public boolean removeInfiniteStacks;
    public boolean disableCreatureCropTrampling;
    public boolean disablePlayerCropTrampling;
    public boolean preventLightningFire;
    public Set<String> disallowedLightningBlocks;
    public boolean disableThunder;
    public boolean disableWeather;
    public boolean alwaysRaining;
    public boolean alwaysThundering;
    public boolean disablePigZap;
    public boolean disableCreeperPower;
    public boolean disableHealthRegain;
    public boolean disableMushroomSpread;
    public boolean disableIceMelting;
    public boolean disableSnowMelting;
    public boolean disableSnowFormation;
    public boolean disableIceFormation;
    public boolean disableLeafDecay;
    public boolean disableGrassGrowth;
    public boolean disableMyceliumSpread;
    public boolean disableVineGrowth;
    public boolean disableEndermanGriefing;
    public boolean disableSnowmanTrails;
    public boolean disableSoilDehydration;
    public Set<String> allowedSnowFallOver;
    public boolean regionInvinciblityRemovesMobs;
    public boolean regionNetherPortalProtection;
    public boolean fakePlayerBuildOverride;
    public boolean explosionFlagCancellation;
    public boolean disableDeathMessages;
    public boolean disableObsidianGenerators;
    public boolean strictEntitySpawn;
    public boolean ignoreHopperMoveEvents;
    protected Map<String, Integer> maxRegionCounts;

    /**
     * Load the configuration.
     */
    public abstract void loadConfiguration();

    public Blacklist getBlacklist() {
        return this.blacklist;
    }

    public String getWorldName() {
        return this.worldName;
    }

    public List<String> convertLegacyItems(List<String> legacyItems) {
        return legacyItems.stream().map(this::convertLegacyItem).collect(Collectors.toList());
    }

    public String convertLegacyItem(String legacy) {
        String item = legacy;
        try {
            String[] splitter = item.split(":", 2);
            int id = 0;
            byte data = 0;
            if (splitter.length == 1) {
                id = Integer.parseInt(item);
            } else {
                id = Integer.parseInt(splitter[0]);
                data = Byte.parseByte(splitter[1]);
            }
            item = LegacyMapper.getInstance().getItemFromLegacy(id, data).getId();
        } catch (Throwable e) {
        }

        return item;
    }

    public List<String> convertLegacyBlocks(List<String> legacyBlocks) {
        return legacyBlocks.stream().map(this::convertLegacyBlock).collect(Collectors.toList());
    }

    public String convertLegacyBlock(String legacy) {
        String block = legacy;
        try {
            String[] splitter = block.split(":", 2);
            int id = 0;
            byte data = 0;
            if (splitter.length == 1) {
                id = Integer.parseInt(block);
            } else {
                id = Integer.parseInt(splitter[0]);
                data = Byte.parseByte(splitter[1]);
            }
            block = LegacyMapper.getInstance().getBlockFromLegacy(id, data).getBlockType().getId();
        } catch (Throwable e) {
        }

        return block;
    }
}
