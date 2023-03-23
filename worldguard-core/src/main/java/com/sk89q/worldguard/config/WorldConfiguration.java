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

import com.sk89q.worldedit.util.report.Unreported;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import com.sk89q.worldedit.world.registry.LegacyMapper;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.blacklist.Blacklist;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    protected File blacklistFile;

    @Unreported
    protected Blacklist blacklist;

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
    public boolean disableConduitEffects;
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
    public boolean blockEntityVehicleEntry;
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
    public boolean safeFallOnVoid;
    public boolean disableExplosionDamage;
    public boolean disableMobDamage;
    public boolean highFreqFlags;
    public boolean checkLiquidFlow;
    public String regionWand;
    public Set<EntityType> blockCreatureSpawn;
    public boolean allowTamedSpawns;
    public int maxClaimVolume;
    public boolean claimOnlyInsideExistingRegions;
    public String setParentOnClaim;
    public int maxRegionCountPerPlayer;
    public boolean antiWolfDumbness;
    public boolean signChestProtection;
    public boolean disableSignChestProtectionCheck;
    public boolean removeInfiniteStacks;
    public boolean disableCreatureCropTrampling;
    public boolean disablePlayerCropTrampling;
    public boolean disableCreatureTurtleEggTrampling;
    public boolean disablePlayerTurtleEggTrampling;
    public boolean preventLightningFire;
    public Set<String> disallowedLightningBlocks;
    public boolean disableThunder;
    public boolean disableWeather;
    public boolean alwaysRaining;
    public boolean alwaysThundering;
    public boolean disablePigZap;
    public boolean disableVillagerZap;
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
    public boolean disableRockGrowth;
    public boolean disableSculkGrowth;
    public boolean disableCropGrowth;
    public boolean disableEndermanGriefing;
    public boolean disableSnowmanTrails;
    public boolean disableSoilDehydration;
    public boolean disableCoralBlockFade;
    public boolean disableCopperBlockFade;
    public Set<String> allowedSnowFallOver;
    public boolean regionInvinciblityRemovesMobs;
    public boolean regionCancelEmptyChatEvents;
    public boolean regionNetherPortalProtection;
    public boolean forceDefaultTitleTimes;
    public boolean fakePlayerBuildOverride;
    public boolean explosionFlagCancellation;
    public boolean disableDeathMessages;
    public boolean strictEntitySpawn;
    public boolean ignoreHopperMoveEvents;
    public boolean breakDeniedHoppers;
    public boolean useMaxPriorityAssociation;
    protected Map<String, Integer> maxRegionCounts;

    /**
     * Load the configuration.
     */
    public abstract void loadConfiguration();

    public Blacklist getBlacklist() {
        return this.blacklist;
    }

    public List<String> convertLegacyItems(List<String> legacyItems) {
        return legacyItems.stream().map(this::convertLegacyItem).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public String convertLegacyItem(String legacy) {
        String[] splitter = legacy.split(":", 2);
        try {
            int id;
            byte data;
            if (splitter.length == 1) {
                id = Integer.parseInt(splitter[0]);
                data = 0;
            } else {
                id = Integer.parseInt(splitter[0]);
                data = Byte.parseByte(splitter[1]);
            }
            ItemType legacyItem = LegacyMapper.getInstance().getItemFromLegacy(id, data);
            if (legacyItem != null) {
                return legacyItem.getId();
            }
        } catch (NumberFormatException ignored) {
        }
        final ItemType itemType = ItemTypes.get(legacy);
        if (itemType != null) {
            return itemType.getId();
        }

        return null;
    }

    public List<String> convertLegacyBlocks(List<String> legacyBlocks) {
        return legacyBlocks.stream().map(this::convertLegacyBlock).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public String convertLegacyBlock(String legacy) {
        String[] splitter = legacy.split(":", 2);
        try {
            int id;
            byte data;
            if (splitter.length == 1) {
                id = Integer.parseInt(splitter[0]);
                data = 0;
            } else {
                id = Integer.parseInt(splitter[0]);
                data = Byte.parseByte(splitter[1]);
            }
            BlockState legacyBlock = LegacyMapper.getInstance().getBlockFromLegacy(id, data);
            if (legacyBlock != null) {
                return legacyBlock.getBlockType().getId();
            }
        } catch (NumberFormatException ignored) {
        }
        final BlockType blockType = BlockTypes.get(legacy);
        if (blockType != null) {
            return blockType.getId();
        }

        return null;
    }

    public int getMaxRegionCount(LocalPlayer player) {
        int max = -1;
        for (String group : player.getGroups()) {
            if (maxRegionCounts.containsKey(group)) {
                int groupMax = maxRegionCounts.get(group);
                if (max < groupMax) {
                    max = groupMax;
                }
            }
        }
        if (max <= -1) {
            max = maxRegionCountPerPlayer;
        }
        return max;
    }
}
