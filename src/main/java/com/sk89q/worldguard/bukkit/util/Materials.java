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

package com.sk89q.worldguard.bukkit.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.material.Dye;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Material utility class.
 */
public final class Materials {

    private static final int MODIFIED_ON_RIGHT = 1;
    private static final int MODIFIED_ON_LEFT = 2;
    private static final int MODIFIES_BLOCKS = 4;

    private static final BiMap<EntityType, Material> ENTITY_ITEMS = HashBiMap.create();
    private static final Map<Material, Integer> MATERIAL_FLAGS = new HashMap<Material, Integer>();
    private static final Set<PotionEffectType> DAMAGE_EFFECTS = new HashSet<PotionEffectType>();

    public static Material ARMOR_STAND;

    static {
        try {
            ARMOR_STAND = Material.ARMOR_STAND;
        } catch (NoSuchFieldError ignored) {
            ARMOR_STAND = null;
        }

        ENTITY_ITEMS.put(EntityType.PAINTING, Material.PAINTING);
        ENTITY_ITEMS.put(EntityType.ARROW, Material.ARROW);
        ENTITY_ITEMS.put(EntityType.SNOWBALL, Material.SNOW_BALL);
        ENTITY_ITEMS.put(EntityType.FIREBALL, Material.FIREBALL);
        ENTITY_ITEMS.put(EntityType.SMALL_FIREBALL, Material.FIREWORK_CHARGE);
        ENTITY_ITEMS.put(EntityType.ENDER_PEARL, Material.ENDER_PEARL);
        ENTITY_ITEMS.put(EntityType.THROWN_EXP_BOTTLE, Material.EXP_BOTTLE);
        ENTITY_ITEMS.put(EntityType.ITEM_FRAME, Material.ITEM_FRAME);
        ENTITY_ITEMS.put(EntityType.PRIMED_TNT, Material.TNT);
        ENTITY_ITEMS.put(EntityType.FIREWORK, Material.FIREWORK);
        ENTITY_ITEMS.put(EntityType.MINECART_COMMAND, Material.COMMAND_MINECART);
        ENTITY_ITEMS.put(EntityType.BOAT, Material.BOAT);
        ENTITY_ITEMS.put(EntityType.MINECART, Material.MINECART);
        ENTITY_ITEMS.put(EntityType.MINECART_CHEST, Material.STORAGE_MINECART);
        ENTITY_ITEMS.put(EntityType.MINECART_FURNACE, Material.POWERED_MINECART);
        ENTITY_ITEMS.put(EntityType.MINECART_TNT, Material.EXPLOSIVE_MINECART);
        ENTITY_ITEMS.put(EntityType.MINECART_HOPPER, Material.HOPPER_MINECART);
        ENTITY_ITEMS.put(EntityType.SPLASH_POTION, Material.POTION);
        ENTITY_ITEMS.put(EntityType.EGG, Material.EGG);
        try {
            ENTITY_ITEMS.put(EntityType.ARMOR_STAND, Material.ARMOR_STAND);
        } catch (NoSuchFieldError ignored) {}

        MATERIAL_FLAGS.put(Material.AIR, 0);
        MATERIAL_FLAGS.put(Material.STONE, 0);
        MATERIAL_FLAGS.put(Material.GRASS, 0);
        MATERIAL_FLAGS.put(Material.DIRT, 0);
        MATERIAL_FLAGS.put(Material.COBBLESTONE, 0);
        MATERIAL_FLAGS.put(Material.WOOD, 0);
        MATERIAL_FLAGS.put(Material.SAPLING, 0);
        MATERIAL_FLAGS.put(Material.BEDROCK, 0);
        MATERIAL_FLAGS.put(Material.WATER, 0);
        MATERIAL_FLAGS.put(Material.STATIONARY_WATER, 0);
        MATERIAL_FLAGS.put(Material.LAVA, 0);
        MATERIAL_FLAGS.put(Material.STATIONARY_LAVA, 0);
        MATERIAL_FLAGS.put(Material.SAND, 0);
        MATERIAL_FLAGS.put(Material.GRAVEL, 0);
        MATERIAL_FLAGS.put(Material.GOLD_ORE, 0);
        MATERIAL_FLAGS.put(Material.IRON_ORE, 0);
        MATERIAL_FLAGS.put(Material.COAL_ORE, 0);
        MATERIAL_FLAGS.put(Material.LOG, 0);
        MATERIAL_FLAGS.put(Material.LEAVES, 0);
        MATERIAL_FLAGS.put(Material.SPONGE, 0);
        MATERIAL_FLAGS.put(Material.GLASS, 0);
        MATERIAL_FLAGS.put(Material.LAPIS_ORE, 0);
        MATERIAL_FLAGS.put(Material.LAPIS_BLOCK, 0);
        MATERIAL_FLAGS.put(Material.DISPENSER, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.SANDSTONE, 0);
        MATERIAL_FLAGS.put(Material.NOTE_BLOCK, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.BED_BLOCK, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.POWERED_RAIL, 0);
        MATERIAL_FLAGS.put(Material.DETECTOR_RAIL, 0);
        MATERIAL_FLAGS.put(Material.PISTON_STICKY_BASE, 0);
        MATERIAL_FLAGS.put(Material.WEB, 0);
        MATERIAL_FLAGS.put(Material.LONG_GRASS, 0);
        MATERIAL_FLAGS.put(Material.DEAD_BUSH, 0);
        MATERIAL_FLAGS.put(Material.PISTON_BASE, 0);
        MATERIAL_FLAGS.put(Material.PISTON_EXTENSION, 0);
        MATERIAL_FLAGS.put(Material.WOOL, 0);
        MATERIAL_FLAGS.put(Material.PISTON_MOVING_PIECE, 0);
        MATERIAL_FLAGS.put(Material.YELLOW_FLOWER, 0);
        MATERIAL_FLAGS.put(Material.RED_ROSE, 0);
        MATERIAL_FLAGS.put(Material.BROWN_MUSHROOM, 0);
        MATERIAL_FLAGS.put(Material.RED_MUSHROOM, 0);
        MATERIAL_FLAGS.put(Material.GOLD_BLOCK, 0);
        MATERIAL_FLAGS.put(Material.IRON_BLOCK, 0);
        MATERIAL_FLAGS.put(Material.DOUBLE_STEP, 0);
        MATERIAL_FLAGS.put(Material.STEP, 0);
        MATERIAL_FLAGS.put(Material.BRICK, 0);
        MATERIAL_FLAGS.put(Material.TNT, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.BOOKSHELF, 0);
        MATERIAL_FLAGS.put(Material.MOSSY_COBBLESTONE, 0);
        MATERIAL_FLAGS.put(Material.OBSIDIAN, 0);
        MATERIAL_FLAGS.put(Material.TORCH, 0);
        MATERIAL_FLAGS.put(Material.FIRE, 0);
        MATERIAL_FLAGS.put(Material.MOB_SPAWNER, 0);
        MATERIAL_FLAGS.put(Material.WOOD_STAIRS, 0);
        MATERIAL_FLAGS.put(Material.CHEST, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.REDSTONE_WIRE, 0);
        MATERIAL_FLAGS.put(Material.DIAMOND_ORE, 0);
        MATERIAL_FLAGS.put(Material.DIAMOND_BLOCK, 0);
        MATERIAL_FLAGS.put(Material.WORKBENCH, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.CROPS, 0);
        MATERIAL_FLAGS.put(Material.SOIL, 0);
        MATERIAL_FLAGS.put(Material.FURNACE, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.BURNING_FURNACE, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.SIGN_POST, 0);
        MATERIAL_FLAGS.put(Material.WOODEN_DOOR, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.LADDER, 0);
        MATERIAL_FLAGS.put(Material.RAILS, 0);
        MATERIAL_FLAGS.put(Material.COBBLESTONE_STAIRS, 0);
        MATERIAL_FLAGS.put(Material.WALL_SIGN, 0);
        MATERIAL_FLAGS.put(Material.LEVER, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.STONE_PLATE, 0);
        MATERIAL_FLAGS.put(Material.IRON_DOOR_BLOCK, 0);
        MATERIAL_FLAGS.put(Material.WOOD_PLATE, 0);
        MATERIAL_FLAGS.put(Material.REDSTONE_ORE, 0);
        MATERIAL_FLAGS.put(Material.GLOWING_REDSTONE_ORE, 0);
        MATERIAL_FLAGS.put(Material.REDSTONE_TORCH_OFF, 0);
        MATERIAL_FLAGS.put(Material.REDSTONE_TORCH_ON, 0);
        MATERIAL_FLAGS.put(Material.STONE_BUTTON, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.SNOW, 0);
        MATERIAL_FLAGS.put(Material.ICE, 0);
        MATERIAL_FLAGS.put(Material.SNOW_BLOCK, 0);
        MATERIAL_FLAGS.put(Material.CACTUS, 0);
        MATERIAL_FLAGS.put(Material.CLAY, 0);
        MATERIAL_FLAGS.put(Material.SUGAR_CANE_BLOCK, 0);
        MATERIAL_FLAGS.put(Material.JUKEBOX, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.FENCE, 0);
        MATERIAL_FLAGS.put(Material.PUMPKIN, 0);
        MATERIAL_FLAGS.put(Material.NETHERRACK, 0);
        MATERIAL_FLAGS.put(Material.SOUL_SAND, 0);
        MATERIAL_FLAGS.put(Material.GLOWSTONE, 0);
        MATERIAL_FLAGS.put(Material.PORTAL, 0);
        MATERIAL_FLAGS.put(Material.JACK_O_LANTERN, 0);
        MATERIAL_FLAGS.put(Material.CAKE_BLOCK, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.DIODE_BLOCK_OFF, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.DIODE_BLOCK_ON, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.STAINED_GLASS, 0);
        MATERIAL_FLAGS.put(Material.TRAP_DOOR, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.MONSTER_EGGS, 0);
        MATERIAL_FLAGS.put(Material.SMOOTH_BRICK, 0);
        MATERIAL_FLAGS.put(Material.HUGE_MUSHROOM_1, 0);
        MATERIAL_FLAGS.put(Material.HUGE_MUSHROOM_2, 0);
        MATERIAL_FLAGS.put(Material.IRON_FENCE, 0);
        MATERIAL_FLAGS.put(Material.THIN_GLASS, 0);
        MATERIAL_FLAGS.put(Material.MELON_BLOCK, 0);
        MATERIAL_FLAGS.put(Material.PUMPKIN_STEM, 0);
        MATERIAL_FLAGS.put(Material.MELON_STEM, 0);
        MATERIAL_FLAGS.put(Material.VINE, 0);
        MATERIAL_FLAGS.put(Material.FENCE_GATE, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.BRICK_STAIRS, 0);
        MATERIAL_FLAGS.put(Material.SMOOTH_STAIRS, 0);
        MATERIAL_FLAGS.put(Material.MYCEL, 0);
        MATERIAL_FLAGS.put(Material.WATER_LILY, 0);
        MATERIAL_FLAGS.put(Material.NETHER_BRICK, 0);
        MATERIAL_FLAGS.put(Material.NETHER_FENCE, 0);
        MATERIAL_FLAGS.put(Material.NETHER_BRICK_STAIRS, 0);
        MATERIAL_FLAGS.put(Material.NETHER_WARTS, 0);
        MATERIAL_FLAGS.put(Material.ENCHANTMENT_TABLE, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.BREWING_STAND, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.CAULDRON, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.ENDER_PORTAL, 0);
        MATERIAL_FLAGS.put(Material.ENDER_PORTAL_FRAME, 0);
        MATERIAL_FLAGS.put(Material.ENDER_STONE, 0);
        MATERIAL_FLAGS.put(Material.DRAGON_EGG, MODIFIED_ON_LEFT | MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.REDSTONE_LAMP_OFF, 0);
        MATERIAL_FLAGS.put(Material.REDSTONE_LAMP_ON, 0);
        MATERIAL_FLAGS.put(Material.WOOD_DOUBLE_STEP, 0);
        MATERIAL_FLAGS.put(Material.WOOD_STEP, 0);
        MATERIAL_FLAGS.put(Material.COCOA, 0);
        MATERIAL_FLAGS.put(Material.SANDSTONE_STAIRS, 0);
        MATERIAL_FLAGS.put(Material.EMERALD_ORE, 0);
        MATERIAL_FLAGS.put(Material.ENDER_CHEST, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.TRIPWIRE_HOOK, 0);
        MATERIAL_FLAGS.put(Material.TRIPWIRE, 0);
        MATERIAL_FLAGS.put(Material.EMERALD_BLOCK, 0);
        MATERIAL_FLAGS.put(Material.SPRUCE_WOOD_STAIRS, 0);
        MATERIAL_FLAGS.put(Material.BIRCH_WOOD_STAIRS, 0);
        MATERIAL_FLAGS.put(Material.JUNGLE_WOOD_STAIRS, 0);
        MATERIAL_FLAGS.put(Material.COMMAND, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.BEACON, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.COBBLE_WALL, 0);
        MATERIAL_FLAGS.put(Material.FLOWER_POT, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.CARROT, 0);
        MATERIAL_FLAGS.put(Material.POTATO, 0);
        MATERIAL_FLAGS.put(Material.WOOD_BUTTON, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.SKULL, 0);
        MATERIAL_FLAGS.put(Material.ANVIL, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.TRAPPED_CHEST, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.GOLD_PLATE, 0);
        MATERIAL_FLAGS.put(Material.IRON_PLATE, 0);
        MATERIAL_FLAGS.put(Material.REDSTONE_COMPARATOR_OFF, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.REDSTONE_COMPARATOR_ON, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.DAYLIGHT_DETECTOR, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.REDSTONE_BLOCK, 0);
        MATERIAL_FLAGS.put(Material.QUARTZ_ORE, 0);
        MATERIAL_FLAGS.put(Material.HOPPER, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.QUARTZ_BLOCK, 0);
        MATERIAL_FLAGS.put(Material.QUARTZ_STAIRS, 0);
        MATERIAL_FLAGS.put(Material.ACTIVATOR_RAIL, 0);
        MATERIAL_FLAGS.put(Material.DROPPER, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.STAINED_CLAY, 0);
        MATERIAL_FLAGS.put(Material.STAINED_GLASS_PANE, 0);
        MATERIAL_FLAGS.put(Material.LEAVES_2, 0);
        MATERIAL_FLAGS.put(Material.LOG_2, 0);
        MATERIAL_FLAGS.put(Material.ACACIA_STAIRS, 0);
        MATERIAL_FLAGS.put(Material.DARK_OAK_STAIRS, 0);
        MATERIAL_FLAGS.put(Material.HAY_BLOCK, 0);
        MATERIAL_FLAGS.put(Material.CARPET, 0);
        MATERIAL_FLAGS.put(Material.HARD_CLAY, 0);
        MATERIAL_FLAGS.put(Material.COAL_BLOCK, 0);
        MATERIAL_FLAGS.put(Material.PACKED_ICE, 0);
        MATERIAL_FLAGS.put(Material.DOUBLE_PLANT, 0);
        try {
            MATERIAL_FLAGS.put(Material.STANDING_BANNER, 0);
            MATERIAL_FLAGS.put(Material.WALL_BANNER, 0);
            MATERIAL_FLAGS.put(Material.DAYLIGHT_DETECTOR_INVERTED, MODIFIED_ON_RIGHT);
            MATERIAL_FLAGS.put(Material.RED_SANDSTONE, 0);
            MATERIAL_FLAGS.put(Material.RED_SANDSTONE_STAIRS, 0);
            MATERIAL_FLAGS.put(Material.DOUBLE_STONE_SLAB2, 0);
            MATERIAL_FLAGS.put(Material.STONE_SLAB2, 0);
            MATERIAL_FLAGS.put(Material.SPRUCE_FENCE_GATE, MODIFIED_ON_RIGHT);
            MATERIAL_FLAGS.put(Material.BIRCH_FENCE_GATE, MODIFIED_ON_RIGHT);
            MATERIAL_FLAGS.put(Material.JUNGLE_FENCE_GATE, MODIFIED_ON_RIGHT);
            MATERIAL_FLAGS.put(Material.DARK_OAK_FENCE_GATE, MODIFIED_ON_RIGHT);
            MATERIAL_FLAGS.put(Material.ACACIA_FENCE_GATE, MODIFIED_ON_RIGHT);
            MATERIAL_FLAGS.put(Material.SPRUCE_FENCE, 0);
            MATERIAL_FLAGS.put(Material.BIRCH_FENCE, 0);
            MATERIAL_FLAGS.put(Material.JUNGLE_FENCE, 0);
            MATERIAL_FLAGS.put(Material.DARK_OAK_FENCE, 0);
            MATERIAL_FLAGS.put(Material.ACACIA_FENCE, 0);
            MATERIAL_FLAGS.put(Material.SPRUCE_DOOR, MODIFIED_ON_RIGHT);
            MATERIAL_FLAGS.put(Material.BIRCH_DOOR, MODIFIED_ON_RIGHT);
            MATERIAL_FLAGS.put(Material.JUNGLE_DOOR, MODIFIED_ON_RIGHT);
            MATERIAL_FLAGS.put(Material.ACACIA_DOOR, MODIFIED_ON_RIGHT);
            MATERIAL_FLAGS.put(Material.DARK_OAK_DOOR, MODIFIED_ON_RIGHT);
        } catch (NoSuchFieldError ignored) {
        }

        MATERIAL_FLAGS.put(Material.IRON_SPADE, 0);
        MATERIAL_FLAGS.put(Material.IRON_PICKAXE, 0);
        MATERIAL_FLAGS.put(Material.IRON_AXE, 0);
        MATERIAL_FLAGS.put(Material.FLINT_AND_STEEL, 0);
        MATERIAL_FLAGS.put(Material.APPLE, 0);
        MATERIAL_FLAGS.put(Material.BOW, 0);
        MATERIAL_FLAGS.put(Material.ARROW, 0);
        MATERIAL_FLAGS.put(Material.COAL, 0);
        MATERIAL_FLAGS.put(Material.DIAMOND, 0);
        MATERIAL_FLAGS.put(Material.IRON_INGOT, 0);
        MATERIAL_FLAGS.put(Material.GOLD_INGOT, 0);
        MATERIAL_FLAGS.put(Material.IRON_SWORD, 0);
        MATERIAL_FLAGS.put(Material.WOOD_SWORD, 0);
        MATERIAL_FLAGS.put(Material.WOOD_SPADE, 0);
        MATERIAL_FLAGS.put(Material.WOOD_PICKAXE, 0);
        MATERIAL_FLAGS.put(Material.WOOD_AXE, 0);
        MATERIAL_FLAGS.put(Material.STONE_SWORD, 0);
        MATERIAL_FLAGS.put(Material.STONE_SPADE, 0);
        MATERIAL_FLAGS.put(Material.STONE_PICKAXE, 0);
        MATERIAL_FLAGS.put(Material.STONE_AXE, 0);
        MATERIAL_FLAGS.put(Material.DIAMOND_SWORD, 0);
        MATERIAL_FLAGS.put(Material.DIAMOND_SPADE, 0);
        MATERIAL_FLAGS.put(Material.DIAMOND_PICKAXE, 0);
        MATERIAL_FLAGS.put(Material.DIAMOND_AXE, 0);
        MATERIAL_FLAGS.put(Material.STICK, 0);
        MATERIAL_FLAGS.put(Material.BOWL, 0);
        MATERIAL_FLAGS.put(Material.MUSHROOM_SOUP, 0);
        MATERIAL_FLAGS.put(Material.GOLD_SWORD, 0);
        MATERIAL_FLAGS.put(Material.GOLD_SPADE, 0);
        MATERIAL_FLAGS.put(Material.GOLD_PICKAXE, 0);
        MATERIAL_FLAGS.put(Material.GOLD_AXE, 0);
        MATERIAL_FLAGS.put(Material.STRING, 0);
        MATERIAL_FLAGS.put(Material.FEATHER, 0);
        MATERIAL_FLAGS.put(Material.SULPHUR, 0);
        MATERIAL_FLAGS.put(Material.WOOD_HOE, MODIFIES_BLOCKS);
        MATERIAL_FLAGS.put(Material.STONE_HOE, MODIFIES_BLOCKS);
        MATERIAL_FLAGS.put(Material.IRON_HOE, MODIFIES_BLOCKS);
        MATERIAL_FLAGS.put(Material.DIAMOND_HOE, MODIFIES_BLOCKS);
        MATERIAL_FLAGS.put(Material.GOLD_HOE, MODIFIES_BLOCKS);
        MATERIAL_FLAGS.put(Material.SEEDS, MODIFIES_BLOCKS);
        MATERIAL_FLAGS.put(Material.WHEAT, 0);
        MATERIAL_FLAGS.put(Material.BREAD, 0);
        MATERIAL_FLAGS.put(Material.LEATHER_HELMET, 0);
        MATERIAL_FLAGS.put(Material.LEATHER_CHESTPLATE, 0);
        MATERIAL_FLAGS.put(Material.LEATHER_LEGGINGS, 0);
        MATERIAL_FLAGS.put(Material.LEATHER_BOOTS, 0);
        MATERIAL_FLAGS.put(Material.CHAINMAIL_HELMET, 0);
        MATERIAL_FLAGS.put(Material.CHAINMAIL_CHESTPLATE, 0);
        MATERIAL_FLAGS.put(Material.CHAINMAIL_LEGGINGS, 0);
        MATERIAL_FLAGS.put(Material.CHAINMAIL_BOOTS, 0);
        MATERIAL_FLAGS.put(Material.IRON_HELMET, 0);
        MATERIAL_FLAGS.put(Material.IRON_CHESTPLATE, 0);
        MATERIAL_FLAGS.put(Material.IRON_LEGGINGS, 0);
        MATERIAL_FLAGS.put(Material.IRON_BOOTS, 0);
        MATERIAL_FLAGS.put(Material.DIAMOND_HELMET, 0);
        MATERIAL_FLAGS.put(Material.DIAMOND_CHESTPLATE, 0);
        MATERIAL_FLAGS.put(Material.DIAMOND_LEGGINGS, 0);
        MATERIAL_FLAGS.put(Material.DIAMOND_BOOTS, 0);
        MATERIAL_FLAGS.put(Material.GOLD_HELMET, 0);
        MATERIAL_FLAGS.put(Material.GOLD_CHESTPLATE, 0);
        MATERIAL_FLAGS.put(Material.GOLD_LEGGINGS, 0);
        MATERIAL_FLAGS.put(Material.GOLD_BOOTS, 0);
        MATERIAL_FLAGS.put(Material.FLINT, 0);
        MATERIAL_FLAGS.put(Material.PORK, 0);
        MATERIAL_FLAGS.put(Material.GRILLED_PORK, 0);
        MATERIAL_FLAGS.put(Material.PAINTING, 0);
        MATERIAL_FLAGS.put(Material.GOLDEN_APPLE, 0);
        MATERIAL_FLAGS.put(Material.SIGN, 0);
        MATERIAL_FLAGS.put(Material.WOOD_DOOR, 0);
        MATERIAL_FLAGS.put(Material.BUCKET, 0);
        MATERIAL_FLAGS.put(Material.WATER_BUCKET, 0);
        MATERIAL_FLAGS.put(Material.LAVA_BUCKET, 0);
        MATERIAL_FLAGS.put(Material.MINECART, 0);
        MATERIAL_FLAGS.put(Material.SADDLE, 0);
        MATERIAL_FLAGS.put(Material.IRON_DOOR, 0);
        MATERIAL_FLAGS.put(Material.REDSTONE, 0);
        MATERIAL_FLAGS.put(Material.SNOW_BALL, 0);
        MATERIAL_FLAGS.put(Material.BOAT, 0);
        MATERIAL_FLAGS.put(Material.LEATHER, 0);
        MATERIAL_FLAGS.put(Material.MILK_BUCKET, 0);
        MATERIAL_FLAGS.put(Material.CLAY_BRICK, 0);
        MATERIAL_FLAGS.put(Material.CLAY_BALL, 0);
        MATERIAL_FLAGS.put(Material.SUGAR_CANE, 0);
        MATERIAL_FLAGS.put(Material.PAPER, 0);
        MATERIAL_FLAGS.put(Material.BOOK, 0);
        MATERIAL_FLAGS.put(Material.SLIME_BALL, 0);
        MATERIAL_FLAGS.put(Material.STORAGE_MINECART, 0);
        MATERIAL_FLAGS.put(Material.POWERED_MINECART, 0);
        MATERIAL_FLAGS.put(Material.EGG, 0);
        MATERIAL_FLAGS.put(Material.COMPASS, 0);
        MATERIAL_FLAGS.put(Material.FISHING_ROD, 0);
        MATERIAL_FLAGS.put(Material.WATCH, 0);
        MATERIAL_FLAGS.put(Material.GLOWSTONE_DUST, 0);
        MATERIAL_FLAGS.put(Material.RAW_FISH, 0);
        MATERIAL_FLAGS.put(Material.COOKED_FISH, 0);
        MATERIAL_FLAGS.put(Material.INK_SACK, MODIFIES_BLOCKS);
        MATERIAL_FLAGS.put(Material.BONE, 0);
        MATERIAL_FLAGS.put(Material.SUGAR, 0);
        MATERIAL_FLAGS.put(Material.CAKE, 0);
        MATERIAL_FLAGS.put(Material.BED, 0);
        MATERIAL_FLAGS.put(Material.DIODE, 0);
        MATERIAL_FLAGS.put(Material.COOKIE, 0);
        MATERIAL_FLAGS.put(Material.MAP, 0);
        MATERIAL_FLAGS.put(Material.SHEARS, MODIFIES_BLOCKS);
        MATERIAL_FLAGS.put(Material.MELON, 0);
        MATERIAL_FLAGS.put(Material.PUMPKIN_SEEDS, 0);
        MATERIAL_FLAGS.put(Material.MELON_SEEDS, 0);
        MATERIAL_FLAGS.put(Material.RAW_BEEF, 0);
        MATERIAL_FLAGS.put(Material.COOKED_BEEF, 0);
        MATERIAL_FLAGS.put(Material.RAW_CHICKEN, 0);
        MATERIAL_FLAGS.put(Material.COOKED_CHICKEN, 0);
        MATERIAL_FLAGS.put(Material.ROTTEN_FLESH, 0);
        MATERIAL_FLAGS.put(Material.ENDER_PEARL, 0);
        MATERIAL_FLAGS.put(Material.BLAZE_ROD, 0);
        MATERIAL_FLAGS.put(Material.GHAST_TEAR, 0);
        MATERIAL_FLAGS.put(Material.GOLD_NUGGET, 0);
        MATERIAL_FLAGS.put(Material.NETHER_STALK, 0);
        MATERIAL_FLAGS.put(Material.POTION, 0);
        MATERIAL_FLAGS.put(Material.GLASS_BOTTLE, 0);
        MATERIAL_FLAGS.put(Material.SPIDER_EYE, 0);
        MATERIAL_FLAGS.put(Material.FERMENTED_SPIDER_EYE, 0);
        MATERIAL_FLAGS.put(Material.BLAZE_POWDER, 0);
        MATERIAL_FLAGS.put(Material.MAGMA_CREAM, 0);
        MATERIAL_FLAGS.put(Material.BREWING_STAND_ITEM, 0);
        MATERIAL_FLAGS.put(Material.CAULDRON_ITEM, 0);
        MATERIAL_FLAGS.put(Material.EYE_OF_ENDER, 0);
        MATERIAL_FLAGS.put(Material.SPECKLED_MELON, 0);
        MATERIAL_FLAGS.put(Material.MONSTER_EGG, MODIFIES_BLOCKS);
        MATERIAL_FLAGS.put(Material.EXP_BOTTLE, 0);
        MATERIAL_FLAGS.put(Material.FIREBALL, 0);
        MATERIAL_FLAGS.put(Material.BOOK_AND_QUILL, 0);
        MATERIAL_FLAGS.put(Material.WRITTEN_BOOK, 0);
        MATERIAL_FLAGS.put(Material.EMERALD, 0);
        MATERIAL_FLAGS.put(Material.ITEM_FRAME, 0);
        MATERIAL_FLAGS.put(Material.FLOWER_POT_ITEM, 0);
        MATERIAL_FLAGS.put(Material.CARROT_ITEM, 0);
        MATERIAL_FLAGS.put(Material.POTATO_ITEM, 0);
        MATERIAL_FLAGS.put(Material.BAKED_POTATO, 0);
        MATERIAL_FLAGS.put(Material.POISONOUS_POTATO, 0);
        MATERIAL_FLAGS.put(Material.EMPTY_MAP, 0);
        MATERIAL_FLAGS.put(Material.GOLDEN_CARROT, 0);
        MATERIAL_FLAGS.put(Material.SKULL_ITEM, 0);
        MATERIAL_FLAGS.put(Material.CARROT_STICK, 0);
        MATERIAL_FLAGS.put(Material.NETHER_STAR, 0);
        MATERIAL_FLAGS.put(Material.PUMPKIN_PIE, 0);
        MATERIAL_FLAGS.put(Material.FIREWORK, 0);
        MATERIAL_FLAGS.put(Material.FIREWORK_CHARGE, 0);
        MATERIAL_FLAGS.put(Material.ENCHANTED_BOOK, 0);
        MATERIAL_FLAGS.put(Material.REDSTONE_COMPARATOR, 0);
        MATERIAL_FLAGS.put(Material.NETHER_BRICK_ITEM, 0);
        MATERIAL_FLAGS.put(Material.QUARTZ, 0);
        MATERIAL_FLAGS.put(Material.EXPLOSIVE_MINECART, 0);
        MATERIAL_FLAGS.put(Material.HOPPER_MINECART, 0);
        MATERIAL_FLAGS.put(Material.IRON_BARDING, 0);
        MATERIAL_FLAGS.put(Material.GOLD_BARDING, 0);
        MATERIAL_FLAGS.put(Material.DIAMOND_BARDING, 0);
        MATERIAL_FLAGS.put(Material.LEASH, 0);
        MATERIAL_FLAGS.put(Material.NAME_TAG, 0);
        MATERIAL_FLAGS.put(Material.COMMAND_MINECART, 0);
        try {
            MATERIAL_FLAGS.put(Material.PRISMARINE_SHARD, 0);
            MATERIAL_FLAGS.put(Material.PRISMARINE_CRYSTALS, 0);
            MATERIAL_FLAGS.put(Material.RABBIT, 0);
            MATERIAL_FLAGS.put(Material.COOKED_RABBIT, 0);
            MATERIAL_FLAGS.put(Material.RABBIT_STEW, 0);
            MATERIAL_FLAGS.put(Material.RABBIT_FOOT, 0);
            MATERIAL_FLAGS.put(Material.RABBIT_HIDE, 0);
            MATERIAL_FLAGS.put(Material.ARMOR_STAND, 0);
            MATERIAL_FLAGS.put(Material.IRON_BARDING, 0);
            MATERIAL_FLAGS.put(Material.GOLD_BARDING, 0);
            MATERIAL_FLAGS.put(Material.DIAMOND_BARDING, 0);;
            MATERIAL_FLAGS.put(Material.MUTTON, 0);
            MATERIAL_FLAGS.put(Material.COOKED_MUTTON, 0);
            MATERIAL_FLAGS.put(Material.BANNER, 0);
            MATERIAL_FLAGS.put(Material.SPRUCE_DOOR_ITEM, 0);
            MATERIAL_FLAGS.put(Material.BIRCH_DOOR_ITEM, 0);
            MATERIAL_FLAGS.put(Material.JUNGLE_DOOR_ITEM, 0);
            MATERIAL_FLAGS.put(Material.ACACIA_DOOR_ITEM, 0);
            MATERIAL_FLAGS.put(Material.DARK_OAK_DOOR_ITEM, 0);
        } catch (NoSuchFieldError ignored) {
        }
        MATERIAL_FLAGS.put(Material.GOLD_RECORD, 0);
        MATERIAL_FLAGS.put(Material.GREEN_RECORD, 0);
        MATERIAL_FLAGS.put(Material.RECORD_3, 0);
        MATERIAL_FLAGS.put(Material.RECORD_4, 0);
        MATERIAL_FLAGS.put(Material.RECORD_5, 0);
        MATERIAL_FLAGS.put(Material.RECORD_6, 0);
        MATERIAL_FLAGS.put(Material.RECORD_7, 0);
        MATERIAL_FLAGS.put(Material.RECORD_8, 0);
        MATERIAL_FLAGS.put(Material.RECORD_9, 0);
        MATERIAL_FLAGS.put(Material.RECORD_10, 0);
        MATERIAL_FLAGS.put(Material.RECORD_11, 0);
        MATERIAL_FLAGS.put(Material.RECORD_12, 0);

        //DAMAGE_EFFECTS.add(PotionEffectType.ABSORPTION);
        DAMAGE_EFFECTS.add(PotionEffectType.BLINDNESS);
        DAMAGE_EFFECTS.add(PotionEffectType.CONFUSION);
        //DAMAGE_EFFECTS.add(PotionEffectType.DAMAGE_RESISTANCE);
        //DAMAGE_EFFECTS.add(PotionEffectType.FAST_DIGGING);
        //DAMAGE_EFFECTS.add(PotionEffectType.FIRE_RESISTANCE);
        DAMAGE_EFFECTS.add(PotionEffectType.HARM);
        //DAMAGE_EFFECTS.add(PotionEffectType.HEAL);
        //DAMAGE_EFFECTS.add(PotionEffectType.HEALTH_BOOST);
        DAMAGE_EFFECTS.add(PotionEffectType.HUNGER);
        //DAMAGE_EFFECTS.add(PotionEffectType.INCREASE_DAMAGE);
        //DAMAGE_EFFECTS.add(PotionEffectType.INVISIBILITY);
        //DAMAGE_EFFECTS.add(PotionEffectType.JUMP);
        //DAMAGE_EFFECTS.add(PotionEffectType.NIGHT_VISION);
        DAMAGE_EFFECTS.add(PotionEffectType.POISON);
        //DAMAGE_EFFECTS.add(PotionEffectType.REGENERATION);
        //DAMAGE_EFFECTS.add(PotionEffectType.SATURATION);
        DAMAGE_EFFECTS.add(PotionEffectType.SLOW);
        DAMAGE_EFFECTS.add(PotionEffectType.SLOW_DIGGING);
        //DAMAGE_EFFECTS.add(PotionEffectType.SPEED);
        //DAMAGE_EFFECTS.add(PotionEffectType.WATER_BREATHING);
        DAMAGE_EFFECTS.add(PotionEffectType.WEAKNESS);
        DAMAGE_EFFECTS.add(PotionEffectType.WITHER);
    }

    private Materials() {
    }

    /**
     * Get the related material for an entity type.
     *
     * @param type the entity type
     * @return the related material or {@code null} if one is not known or exists
     */
    @Nullable
    public static Material getRelatedMaterial(EntityType type) {
        return ENTITY_ITEMS.get(type);
    }

    /**
     * Get the material of the block placed by the given bucket, defaulting
     * to water if the bucket type is not known.
     *
     * <p>If a non-bucket material is given, it will be assumed to be
     * an unknown bucket type. If the given bucket doesn't have a block form
     * (it can't be placed), then water will be returned (i.e. for milk).
     * Be aware that either the stationary or non-stationary material may be
     * returned.</p>
     *
     * @param type the bucket material
     * @return the block material
     */
    public static Material getBucketBlockMaterial(Material type) {
        switch (type) {
            case LAVA_BUCKET:
                return Material.LAVA;
            case MILK_BUCKET:
                return Material.WATER;
            default:
                return Material.WATER;
        }
    }

    /**
     * Test whether the given material is a mushroom.
     *
     * @param material the material
     * @return true if a mushroom block
     */
    public static boolean isMushroom(Material material) {
        return material == Material.RED_MUSHROOM || material == Material.BROWN_MUSHROOM;
    }

    /**
     * Test whether the given material is a leaf block.
     *
     * @param material the material
     * @return true if a leaf block
     */
    public static boolean isLeaf(Material material) {
        return material == Material.LEAVES || material == Material.LEAVES_2;
    }

    /**
     * Test whether the given material is a liquid block.
     *
     * @param material the material
     * @return true if a liquid block
     */
    public static boolean isLiquid(Material material) {
        return isWater(material) || isLava(material);
    }

    /**
     * Test whether the given material is water.
     *
     * @param material the material
     * @return true if a water block
     */
    public static boolean isWater(Material material) {
        return material == Material.WATER || material == Material.STATIONARY_WATER;
    }

    /**
     * Test whether the given material is lava.
     *
     * @param material the material
     * @return true if a lava block
     */
    public static boolean isLava(Material material) {
        return material == Material.LAVA || material == Material.STATIONARY_LAVA;
    }

    /**
     * Test whether the given material is a portal material.
     *
     * @param material the material
     * @return true if a portal block
     */
    public static boolean isPortal(Material material) {
        return material == Material.PORTAL || material == Material.ENDER_PORTAL;
    }

    /**
     * Test whether the given material data is of the given dye color.
     *
     * <p>Returns false for non-dyed items.</p>
     *
     * @param data the data
     * @return true if it is the provided dye color
     */
    public static boolean isDyeColor(MaterialData data, DyeColor color) {
        return data instanceof Dye && ((Dye) data).getColor() == color;
    }

    /**
     * Test whether the given material is a rail block.
     *
     * @param material the material
     * @return true if a rail block
     */
    public static boolean isRailBlock(Material material) {
        return material == Material.RAILS
                || material == Material.ACTIVATOR_RAIL
                || material == Material.DETECTOR_RAIL
                || material == Material.POWERED_RAIL;
    }

    /**
     * Test whether the given material is a piston block, not including
     * the "technical blocks" such as the piston extension block.
     *
     * @param material the material
     * @return true if a piston block
     */
    public static boolean isPistonBlock(Material material) {
        return material == Material.PISTON_BASE
                || material == Material.PISTON_STICKY_BASE;
    }

    /**
     * Test whether the given material is a Minecart.
     *
     * @param material the material
     * @return true if a Minecart item
     */
    public static boolean isMinecart(Material material) {
        return material == Material.MINECART
                || material == Material.COMMAND_MINECART
                || material == Material.EXPLOSIVE_MINECART
                || material == Material.HOPPER_MINECART
                || material == Material.POWERED_MINECART
                || material == Material.STORAGE_MINECART;
    }

    /**
     * Test whether the given material is an inventory block.
     *
     * @param material the material
     * @return true if an inventory block
     */
    public static boolean isInventoryBlock(Material material) {
        return material == Material.CHEST
                || material == Material.JUKEBOX
                || material == Material.DISPENSER
                || material == Material.FURNACE
                || material == Material.BURNING_FURNACE
                || material == Material.BREWING_STAND
                || material == Material.TRAPPED_CHEST
                || material == Material.HOPPER
                || material == Material.DROPPER;
    }

    /**
     * Test whether the given material is affected by
     * {@link DefaultFlag#USE}.
     *
     * <p>Generally, materials that are considered by this method are those
     * that are not inventories but can be used.</p>
     *
     * @param material the material
     * @return true if covered by the use flag
     */
    public static boolean isUseFlagApplicable(Material material) {
        switch (material) {
            case LEVER: return true;
            case STONE_BUTTON: return true;
            case WOOD_BUTTON: return true;
            case TRIPWIRE: return true;
            case WOODEN_DOOR: return true;
            case TRAP_DOOR: return true;
            case FENCE_GATE: return true;
            case WORKBENCH: return true;
            case ENCHANTMENT_TABLE: return true;
            case ENDER_CHEST: return true;
            case BEACON: return true;
            case ANVIL: return true;
            case WOOD_PLATE: return true;
            case STONE_PLATE: return true;
            case IRON_PLATE: return true;
            case GOLD_PLATE: return true;
            case SPRUCE_FENCE_GATE: return true;
            case BIRCH_FENCE_GATE: return true;
            case JUNGLE_FENCE_GATE: return true;
            case DARK_OAK_FENCE_GATE: return true;
            case ACACIA_FENCE_GATE: return true;
            case SPRUCE_DOOR: return true;
            case BIRCH_DOOR: return true;
            case JUNGLE_DOOR: return true;
            case ACACIA_DOOR: return true;
            case DARK_OAK_DOOR: return true;
            default: return false;
        }
    }

    /**
     * Test whether the given type is Redstone ore.
     *
     * @param type the material
     * @return true if Redstone ore
     */
    public static boolean isRedstoneOre(Material type) {
        return type == Material.GLOWING_REDSTONE_ORE || type == Material.REDSTONE_ORE;
    }

    /**
     * Test whether the given material is a block that is modified when it is
     * left or right clicked.
     *
     * <p>This test is conservative, returning true for blocks that it is not
     * aware of.</p>
     *
     * @param material the material
     * @param rightClick whether it is a right click
     * @return true if the block is modified
     */
    public static boolean isBlockModifiedOnClick(Material material, boolean rightClick) {
        Integer flags = MATERIAL_FLAGS.get(material);
        return flags == null
                || (rightClick && (flags & MODIFIED_ON_RIGHT) == MODIFIED_ON_RIGHT)
                || (!rightClick && (flags & MODIFIED_ON_LEFT) == MODIFIED_ON_LEFT);
    }

    /**
     * Test whether the given item modifies a given block when right clicked.
     *
     * <p>This test is conservative, returning true for items that it is not
     * aware of or does not have the details for.</p>
     *
     * @param item the item
     * @param block the block
     * @return true if the item is applied to the block
     */
    public static boolean isItemAppliedToBlock(Material item, Material block) {
        Integer flags = MATERIAL_FLAGS.get(item);
        return flags == null || (flags & MODIFIES_BLOCKS) == MODIFIES_BLOCKS;
    }

    /**
     * Test whether the given material should be tested as "building" when
     * it is used.
     *
     * @param type the type
     * @return true to be considered as used
     */
    public static boolean isConsideredBuildingIfUsed(Material type) {
        return type == Material.SAPLING
            || type == Material.DIODE_BLOCK_OFF
            || type == Material.DIODE_BLOCK_ON
            || type == Material.REDSTONE_COMPARATOR_OFF
            || type == Material.REDSTONE_COMPARATOR_ON;
    }

    /**
     * Test whether a list of potion effects contains one or more potion
     * effects used for doing damage.
     *
     * @param effects A collection of effects
     * @return True if at least one damage effect exists
     */
    public static boolean hasDamageEffect(Collection<PotionEffect> effects) {
        for (PotionEffect effect : effects) {
            if (DAMAGE_EFFECTS.contains(effect.getType())) {
                return true;
            }
        }

        return false;
    }

}
