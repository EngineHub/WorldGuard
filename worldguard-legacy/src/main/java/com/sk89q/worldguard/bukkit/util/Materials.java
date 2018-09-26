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
import com.sk89q.worldguard.protection.flags.Flags;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.Nullable;

/**
 * Material utility class.
 */
public final class Materials {

    private static final Logger logger = Logger.getLogger(Materials.class.getSimpleName());

    private static final int MODIFIED_ON_RIGHT = 1;
    private static final int MODIFIED_ON_LEFT = 2;
    private static final int MODIFIES_BLOCKS = 4;

    private static final BiMap<EntityType, Material> ENTITY_ITEMS = HashBiMap.create();
    private static final Map<Material, Integer> MATERIAL_FLAGS = new HashMap<>();
    private static final Set<PotionEffectType> DAMAGE_EFFECTS = new HashSet<>();


    private static Set<Material> shulkerBoxes = new HashSet<>();

    static {
        shulkerBoxes.add(Material.SHULKER_BOX);
        shulkerBoxes.add(Material.WHITE_SHULKER_BOX);
        shulkerBoxes.add(Material.ORANGE_SHULKER_BOX);
        shulkerBoxes.add(Material.MAGENTA_SHULKER_BOX);
        shulkerBoxes.add(Material.LIGHT_BLUE_SHULKER_BOX);
        shulkerBoxes.add(Material.YELLOW_SHULKER_BOX);
        shulkerBoxes.add(Material.LIME_SHULKER_BOX);
        shulkerBoxes.add(Material.PINK_SHULKER_BOX);
        shulkerBoxes.add(Material.GRAY_SHULKER_BOX);
        shulkerBoxes.add(Material.LIGHT_GRAY_SHULKER_BOX);
        shulkerBoxes.add(Material.CYAN_SHULKER_BOX);
        shulkerBoxes.add(Material.PURPLE_SHULKER_BOX);
        shulkerBoxes.add(Material.BLUE_SHULKER_BOX);
        shulkerBoxes.add(Material.BROWN_SHULKER_BOX);
        shulkerBoxes.add(Material.GREEN_SHULKER_BOX);
        shulkerBoxes.add(Material.RED_SHULKER_BOX);
        shulkerBoxes.add(Material.BLACK_SHULKER_BOX);

        ENTITY_ITEMS.put(EntityType.PAINTING, Material.PAINTING);
        ENTITY_ITEMS.put(EntityType.ARROW, Material.ARROW);
        ENTITY_ITEMS.put(EntityType.SNOWBALL, Material.SNOWBALL);
        ENTITY_ITEMS.put(EntityType.FIREBALL, Material.FIRE_CHARGE);
        ENTITY_ITEMS.put(EntityType.ENDER_PEARL, Material.ENDER_PEARL);
        ENTITY_ITEMS.put(EntityType.THROWN_EXP_BOTTLE, Material.EXPERIENCE_BOTTLE);
        ENTITY_ITEMS.put(EntityType.ITEM_FRAME, Material.ITEM_FRAME);
        ENTITY_ITEMS.put(EntityType.PRIMED_TNT, Material.TNT);
        ENTITY_ITEMS.put(EntityType.FIREWORK, Material.FIREWORK_ROCKET);
        ENTITY_ITEMS.put(EntityType.MINECART_COMMAND, Material.COMMAND_BLOCK_MINECART);
        ENTITY_ITEMS.put(EntityType.BOAT, Material.OAK_BOAT);
        ENTITY_ITEMS.put(EntityType.MINECART, Material.MINECART);
        ENTITY_ITEMS.put(EntityType.MINECART_CHEST, Material.CHEST_MINECART);
        ENTITY_ITEMS.put(EntityType.MINECART_FURNACE, Material.FURNACE_MINECART);
        ENTITY_ITEMS.put(EntityType.MINECART_TNT, Material.TNT_MINECART);
        ENTITY_ITEMS.put(EntityType.MINECART_HOPPER, Material.HOPPER_MINECART);
        ENTITY_ITEMS.put(EntityType.SPLASH_POTION, Material.POTION);
        ENTITY_ITEMS.put(EntityType.EGG, Material.EGG);
        ENTITY_ITEMS.put(EntityType.ARMOR_STAND, Material.ARMOR_STAND);

        MATERIAL_FLAGS.put(Material.AIR, 0);
        MATERIAL_FLAGS.put(Material.STONE, 0);
        MATERIAL_FLAGS.put(Material.GRASS_BLOCK, 0);
        MATERIAL_FLAGS.put(Material.DIRT, 0);
        MATERIAL_FLAGS.put(Material.COBBLESTONE, 0);
        MATERIAL_FLAGS.put(Material.BEDROCK, 0);
        MATERIAL_FLAGS.put(Material.WATER, 0);
        MATERIAL_FLAGS.put(Material.LAVA, 0);
        MATERIAL_FLAGS.put(Material.SAND, 0);
        MATERIAL_FLAGS.put(Material.GRAVEL, 0);
        MATERIAL_FLAGS.put(Material.GOLD_ORE, 0);
        MATERIAL_FLAGS.put(Material.IRON_ORE, 0);
        MATERIAL_FLAGS.put(Material.COAL_ORE, 0);
        MATERIAL_FLAGS.put(Material.SPONGE, 0);
        MATERIAL_FLAGS.put(Material.GLASS, 0);
        MATERIAL_FLAGS.put(Material.LAPIS_ORE, 0);
        MATERIAL_FLAGS.put(Material.LAPIS_BLOCK, 0);
        MATERIAL_FLAGS.put(Material.DISPENSER, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.SANDSTONE, 0);
        MATERIAL_FLAGS.put(Material.NOTE_BLOCK, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.POWERED_RAIL, 0);
        MATERIAL_FLAGS.put(Material.DETECTOR_RAIL, 0);
        MATERIAL_FLAGS.put(Material.STICKY_PISTON, 0);
        MATERIAL_FLAGS.put(Material.COBWEB, 0);
        MATERIAL_FLAGS.put(Material.GRASS, 0);
        MATERIAL_FLAGS.put(Material.DEAD_BUSH, 0);
        MATERIAL_FLAGS.put(Material.PISTON, 0);
        MATERIAL_FLAGS.put(Material.PISTON_HEAD, 0);
        MATERIAL_FLAGS.put(Material.MOVING_PISTON, 0);
        MATERIAL_FLAGS.put(Material.DANDELION, 0);
        MATERIAL_FLAGS.put(Material.BLUE_ORCHID, 0);
        MATERIAL_FLAGS.put(Material.ALLIUM, 0);
        MATERIAL_FLAGS.put(Material.AZURE_BLUET, 0);
        MATERIAL_FLAGS.put(Material.ORANGE_TULIP, 0);
        MATERIAL_FLAGS.put(Material.PINK_TULIP, 0);
        MATERIAL_FLAGS.put(Material.RED_TULIP, 0);
        MATERIAL_FLAGS.put(Material.WHITE_TULIP, 0);
        MATERIAL_FLAGS.put(Material.OXEYE_DAISY, 0);
        MATERIAL_FLAGS.put(Material.SUNFLOWER, 0);
        MATERIAL_FLAGS.put(Material.LILAC, 0);
        MATERIAL_FLAGS.put(Material.PEONY, 0);
        MATERIAL_FLAGS.put(Material.ROSE_BUSH, 0);
        MATERIAL_FLAGS.put(Material.POPPY, 0);
        MATERIAL_FLAGS.put(Material.BROWN_MUSHROOM, 0);
        MATERIAL_FLAGS.put(Material.RED_MUSHROOM, 0);
        MATERIAL_FLAGS.put(Material.GOLD_BLOCK, 0);
        MATERIAL_FLAGS.put(Material.IRON_BLOCK, 0);
        MATERIAL_FLAGS.put(Material.BRICK, 0);
        MATERIAL_FLAGS.put(Material.TNT, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.BOOKSHELF, 0);
        MATERIAL_FLAGS.put(Material.MOSSY_COBBLESTONE, 0);
        MATERIAL_FLAGS.put(Material.OBSIDIAN, 0);
        MATERIAL_FLAGS.put(Material.TORCH, 0);
        MATERIAL_FLAGS.put(Material.FIRE, 0);
        MATERIAL_FLAGS.put(Material.SPAWNER, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.CHEST, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.REDSTONE_WIRE, 0);
        MATERIAL_FLAGS.put(Material.DIAMOND_ORE, 0);
        MATERIAL_FLAGS.put(Material.DIAMOND_BLOCK, 0);
        MATERIAL_FLAGS.put(Material.CRAFTING_TABLE, 0);
        MATERIAL_FLAGS.put(Material.WHEAT, 0);
        MATERIAL_FLAGS.put(Material.FARMLAND, 0);
        MATERIAL_FLAGS.put(Material.FURNACE, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.SIGN, 0);
        MATERIAL_FLAGS.put(Material.LADDER, 0);
        MATERIAL_FLAGS.put(Material.RAIL, 0);
        MATERIAL_FLAGS.put(Material.COBBLESTONE_STAIRS, 0);
        MATERIAL_FLAGS.put(Material.WALL_SIGN, 0);
        MATERIAL_FLAGS.put(Material.LEVER, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.STONE_PRESSURE_PLATE, 0);
        MATERIAL_FLAGS.put(Material.REDSTONE_ORE, 0);
        MATERIAL_FLAGS.put(Material.REDSTONE_WALL_TORCH, 0);
        MATERIAL_FLAGS.put(Material.REDSTONE_TORCH, 0);
        MATERIAL_FLAGS.put(Material.SNOW, 0);
        MATERIAL_FLAGS.put(Material.ICE, 0);
        MATERIAL_FLAGS.put(Material.SNOW_BLOCK, 0);
        MATERIAL_FLAGS.put(Material.CACTUS, 0);
        MATERIAL_FLAGS.put(Material.CLAY, 0);
        MATERIAL_FLAGS.put(Material.JUKEBOX, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.OAK_FENCE, 0);
        MATERIAL_FLAGS.put(Material.PUMPKIN, 0);
        MATERIAL_FLAGS.put(Material.NETHERRACK, 0);
        MATERIAL_FLAGS.put(Material.SOUL_SAND, 0);
        MATERIAL_FLAGS.put(Material.GLOWSTONE, 0);
        MATERIAL_FLAGS.put(Material.NETHER_PORTAL, 0);
        MATERIAL_FLAGS.put(Material.JACK_O_LANTERN, 0);
        MATERIAL_FLAGS.put(Material.CAKE, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.REPEATER, MODIFIED_ON_RIGHT);
//        MATERIAL_FLAGS.put(Material.STAINED_GLASS, 0);
        MATERIAL_FLAGS.put(Material.ACACIA_TRAPDOOR, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.BIRCH_TRAPDOOR, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.DARK_OAK_TRAPDOOR, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.JUNGLE_TRAPDOOR, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.OAK_TRAPDOOR, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.SPRUCE_TRAPDOOR, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.INFESTED_STONE, 0);
        MATERIAL_FLAGS.put(Material.INFESTED_STONE_BRICKS, 0);
        MATERIAL_FLAGS.put(Material.INFESTED_MOSSY_STONE_BRICKS, 0);
        MATERIAL_FLAGS.put(Material.INFESTED_CRACKED_STONE_BRICKS, 0);
        MATERIAL_FLAGS.put(Material.INFESTED_CHISELED_STONE_BRICKS, 0);
        MATERIAL_FLAGS.put(Material.INFESTED_COBBLESTONE, 0);
        MATERIAL_FLAGS.put(Material.STONE_BRICKS, 0);
        MATERIAL_FLAGS.put(Material.MOSSY_STONE_BRICKS, 0);
        MATERIAL_FLAGS.put(Material.CRACKED_STONE_BRICKS, 0);
        MATERIAL_FLAGS.put(Material.CHISELED_STONE_BRICKS, 0);
        MATERIAL_FLAGS.put(Material.BROWN_MUSHROOM_BLOCK, 0);
        MATERIAL_FLAGS.put(Material.RED_MUSHROOM_BLOCK, 0);
        MATERIAL_FLAGS.put(Material.IRON_BARS, 0);
        MATERIAL_FLAGS.put(Material.GLASS_PANE, 0);
        MATERIAL_FLAGS.put(Material.MELON, 0);
        MATERIAL_FLAGS.put(Material.PUMPKIN_STEM, 0);
        MATERIAL_FLAGS.put(Material.MELON_STEM, 0);
        MATERIAL_FLAGS.put(Material.VINE, 0);
        MATERIAL_FLAGS.put(Material.SPRUCE_FENCE_GATE, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.ACACIA_FENCE_GATE, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.BIRCH_FENCE_GATE, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.DARK_OAK_FENCE_GATE, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.JUNGLE_FENCE_GATE, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.OAK_FENCE_GATE, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.BRICK_STAIRS, 0);
        MATERIAL_FLAGS.put(Material.MYCELIUM, 0);
        MATERIAL_FLAGS.put(Material.LILY_PAD, 0);
        MATERIAL_FLAGS.put(Material.NETHER_BRICK, 0);
        MATERIAL_FLAGS.put(Material.NETHER_BRICK_FENCE, 0);
        MATERIAL_FLAGS.put(Material.NETHER_BRICK_STAIRS, 0);
        MATERIAL_FLAGS.put(Material.ENCHANTING_TABLE, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.BREWING_STAND, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.CAULDRON, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.END_PORTAL, 0);
        MATERIAL_FLAGS.put(Material.END_PORTAL_FRAME, 0);
        MATERIAL_FLAGS.put(Material.END_STONE, 0);
        MATERIAL_FLAGS.put(Material.DRAGON_EGG, MODIFIED_ON_LEFT | MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.REDSTONE_LAMP, 0);
        MATERIAL_FLAGS.put(Material.COCOA, 0);
        MATERIAL_FLAGS.put(Material.SANDSTONE_STAIRS, 0);
        MATERIAL_FLAGS.put(Material.EMERALD_ORE, 0);
        MATERIAL_FLAGS.put(Material.ENDER_CHEST, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.TRIPWIRE_HOOK, 0);
        MATERIAL_FLAGS.put(Material.TRIPWIRE, 0);
        MATERIAL_FLAGS.put(Material.EMERALD_BLOCK, 0);
        MATERIAL_FLAGS.put(Material.COMMAND_BLOCK, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.BEACON, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.COBBLESTONE_WALL, 0);
        MATERIAL_FLAGS.put(Material.ANVIL, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.TRAPPED_CHEST, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.HEAVY_WEIGHTED_PRESSURE_PLATE, 0);
        MATERIAL_FLAGS.put(Material.LIGHT_WEIGHTED_PRESSURE_PLATE, 0);
        MATERIAL_FLAGS.put(Material.COMPARATOR, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.DAYLIGHT_DETECTOR, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.REDSTONE_BLOCK, 0);
        MATERIAL_FLAGS.put(Material.NETHER_QUARTZ_ORE, 0);
        MATERIAL_FLAGS.put(Material.HOPPER, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.QUARTZ_BLOCK, 0);
        MATERIAL_FLAGS.put(Material.QUARTZ_STAIRS, 0);
        MATERIAL_FLAGS.put(Material.ACTIVATOR_RAIL, 0);
        MATERIAL_FLAGS.put(Material.DROPPER, MODIFIED_ON_RIGHT);
//        MATERIAL_FLAGS.put(Material.STAINED_CLAY, 0);
//        MATERIAL_FLAGS.put(Material.STAINED_GLASS_PANE, 0);
        MATERIAL_FLAGS.put(Material.ACACIA_STAIRS, 0);
        MATERIAL_FLAGS.put(Material.DARK_OAK_STAIRS, 0);
        MATERIAL_FLAGS.put(Material.HAY_BLOCK, 0);
//        MATERIAL_FLAGS.put(Material.HARD_CLAY, 0);
        MATERIAL_FLAGS.put(Material.COAL_BLOCK, 0);
        MATERIAL_FLAGS.put(Material.PACKED_ICE, 0);
        MATERIAL_FLAGS.put(Material.TALL_GRASS, 0);
        MATERIAL_FLAGS.put(Material.TALL_SEAGRASS, 0);
        MATERIAL_FLAGS.put(Material.LARGE_FERN, 0);

        MATERIAL_FLAGS.put(Material.PRISMARINE, 0);
        MATERIAL_FLAGS.put(Material.SEA_LANTERN, 0);
        MATERIAL_FLAGS.put(Material.SLIME_BLOCK, 0);
        MATERIAL_FLAGS.put(Material.IRON_TRAPDOOR, 0);
        MATERIAL_FLAGS.put(Material.RED_SANDSTONE, 0);
        MATERIAL_FLAGS.put(Material.RED_SANDSTONE_STAIRS, 0);
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

        MATERIAL_FLAGS.put(Material.GRASS_PATH, 0);
        MATERIAL_FLAGS.put(Material.CHORUS_PLANT, 0);
        MATERIAL_FLAGS.put(Material.CHORUS_FLOWER, 0);
        MATERIAL_FLAGS.put(Material.BEETROOTS, 0);
        MATERIAL_FLAGS.put(Material.END_ROD, 0);
        MATERIAL_FLAGS.put(Material.END_STONE_BRICKS, 0);
        MATERIAL_FLAGS.put(Material.END_GATEWAY, 0);
        MATERIAL_FLAGS.put(Material.FROSTED_ICE, 0);
        MATERIAL_FLAGS.put(Material.PURPUR_BLOCK, 0);
        MATERIAL_FLAGS.put(Material.PURPUR_STAIRS, 0);
        MATERIAL_FLAGS.put(Material.PURPUR_PILLAR, 0);
        MATERIAL_FLAGS.put(Material.PURPUR_SLAB, 0);
        MATERIAL_FLAGS.put(Material.STRUCTURE_BLOCK, MODIFIED_ON_LEFT | MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.REPEATING_COMMAND_BLOCK, MODIFIED_ON_RIGHT);
        MATERIAL_FLAGS.put(Material.CHAIN_COMMAND_BLOCK , MODIFIED_ON_RIGHT);

        MATERIAL_FLAGS.put(Material.MAGMA_BLOCK, 0);
        MATERIAL_FLAGS.put(Material.NETHER_WART_BLOCK, 0);
        MATERIAL_FLAGS.put(Material.RED_NETHER_BRICKS, 0);
        MATERIAL_FLAGS.put(Material.BONE_BLOCK, 0);
        MATERIAL_FLAGS.put(Material.STRUCTURE_VOID, 0);
        // 1.12
        MATERIAL_FLAGS.put(Material.BLACK_CONCRETE, 0);
        MATERIAL_FLAGS.put(Material.BLUE_CONCRETE, 0);
        MATERIAL_FLAGS.put(Material.BROWN_CONCRETE, 0);
        MATERIAL_FLAGS.put(Material.CYAN_CONCRETE, 0);
        MATERIAL_FLAGS.put(Material.GRAY_CONCRETE, 0);
        MATERIAL_FLAGS.put(Material.GREEN_CONCRETE, 0);
        MATERIAL_FLAGS.put(Material.LIGHT_BLUE_CONCRETE, 0);
        MATERIAL_FLAGS.put(Material.LIGHT_GRAY_CONCRETE, 0);
        MATERIAL_FLAGS.put(Material.LIME_CONCRETE, 0);
        MATERIAL_FLAGS.put(Material.MAGENTA_CONCRETE, 0);
        MATERIAL_FLAGS.put(Material.ORANGE_CONCRETE, 0);
        MATERIAL_FLAGS.put(Material.PINK_CONCRETE, 0);
        MATERIAL_FLAGS.put(Material.PURPLE_CONCRETE, 0);
        MATERIAL_FLAGS.put(Material.RED_CONCRETE, 0);
        MATERIAL_FLAGS.put(Material.WHITE_CONCRETE, 0);
        MATERIAL_FLAGS.put(Material.BLACK_CONCRETE_POWDER, 0);
        MATERIAL_FLAGS.put(Material.BLUE_CONCRETE_POWDER, 0);
        MATERIAL_FLAGS.put(Material.BROWN_CONCRETE_POWDER, 0);
        MATERIAL_FLAGS.put(Material.CYAN_CONCRETE_POWDER, 0);
        MATERIAL_FLAGS.put(Material.GRAY_CONCRETE_POWDER, 0);
        MATERIAL_FLAGS.put(Material.GREEN_CONCRETE_POWDER, 0);
        MATERIAL_FLAGS.put(Material.LIGHT_BLUE_CONCRETE_POWDER, 0);
        MATERIAL_FLAGS.put(Material.LIGHT_GRAY_CONCRETE_POWDER, 0);
        MATERIAL_FLAGS.put(Material.LIME_CONCRETE_POWDER, 0);
        MATERIAL_FLAGS.put(Material.MAGENTA_CONCRETE_POWDER, 0);
        MATERIAL_FLAGS.put(Material.ORANGE_CONCRETE_POWDER, 0);
        MATERIAL_FLAGS.put(Material.PINK_CONCRETE_POWDER, 0);
        MATERIAL_FLAGS.put(Material.PURPLE_CONCRETE_POWDER, 0);
        MATERIAL_FLAGS.put(Material.RED_CONCRETE_POWDER, 0);
        MATERIAL_FLAGS.put(Material.WHITE_CONCRETE_POWDER, 0);

        MATERIAL_FLAGS.put(Material.WHITE_GLAZED_TERRACOTTA, 0);
        MATERIAL_FLAGS.put(Material.ORANGE_GLAZED_TERRACOTTA, 0);
        MATERIAL_FLAGS.put(Material.MAGENTA_GLAZED_TERRACOTTA, 0);
        MATERIAL_FLAGS.put(Material.LIGHT_BLUE_GLAZED_TERRACOTTA, 0);
        MATERIAL_FLAGS.put(Material.YELLOW_GLAZED_TERRACOTTA, 0);
        MATERIAL_FLAGS.put(Material.LIME_GLAZED_TERRACOTTA, 0);
        MATERIAL_FLAGS.put(Material.PINK_GLAZED_TERRACOTTA, 0);
        MATERIAL_FLAGS.put(Material.GRAY_GLAZED_TERRACOTTA, 0);
        MATERIAL_FLAGS.put(Material.LIGHT_GRAY_GLAZED_TERRACOTTA, 0);
        MATERIAL_FLAGS.put(Material.CYAN_GLAZED_TERRACOTTA, 0);
        MATERIAL_FLAGS.put(Material.PURPLE_GLAZED_TERRACOTTA, 0);
        MATERIAL_FLAGS.put(Material.BLUE_GLAZED_TERRACOTTA, 0);
        MATERIAL_FLAGS.put(Material.BROWN_GLAZED_TERRACOTTA, 0);
        MATERIAL_FLAGS.put(Material.GREEN_GLAZED_TERRACOTTA, 0);
        MATERIAL_FLAGS.put(Material.RED_GLAZED_TERRACOTTA, 0);
        MATERIAL_FLAGS.put(Material.BLACK_GLAZED_TERRACOTTA, 0);

        MATERIAL_FLAGS.put(Material.IRON_SHOVEL, MODIFIES_BLOCKS);
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
        MATERIAL_FLAGS.put(Material.WOODEN_SWORD, 0);
        MATERIAL_FLAGS.put(Material.WOODEN_SHOVEL, MODIFIES_BLOCKS);
        MATERIAL_FLAGS.put(Material.WOODEN_PICKAXE, 0);
        MATERIAL_FLAGS.put(Material.WOODEN_AXE, 0);
        MATERIAL_FLAGS.put(Material.STONE_SWORD, 0);
        MATERIAL_FLAGS.put(Material.STONE_SHOVEL, MODIFIES_BLOCKS);
        MATERIAL_FLAGS.put(Material.STONE_PICKAXE, 0);
        MATERIAL_FLAGS.put(Material.STONE_AXE, 0);
        MATERIAL_FLAGS.put(Material.DIAMOND_SWORD, 0);
        MATERIAL_FLAGS.put(Material.DIAMOND_SHOVEL, MODIFIES_BLOCKS);
        MATERIAL_FLAGS.put(Material.DIAMOND_PICKAXE, 0);
        MATERIAL_FLAGS.put(Material.DIAMOND_AXE, 0);
        MATERIAL_FLAGS.put(Material.STICK, 0);
        MATERIAL_FLAGS.put(Material.BOWL, 0);
        MATERIAL_FLAGS.put(Material.MUSHROOM_STEW, 0);
        MATERIAL_FLAGS.put(Material.GOLDEN_SWORD, 0);
        MATERIAL_FLAGS.put(Material.GOLDEN_SHOVEL, MODIFIES_BLOCKS);
        MATERIAL_FLAGS.put(Material.GOLDEN_PICKAXE, 0);
        MATERIAL_FLAGS.put(Material.GOLDEN_AXE, 0);
        MATERIAL_FLAGS.put(Material.STRING, 0);
        MATERIAL_FLAGS.put(Material.FEATHER, 0);
        MATERIAL_FLAGS.put(Material.GUNPOWDER, 0);
        MATERIAL_FLAGS.put(Material.WOODEN_HOE, MODIFIES_BLOCKS);
        MATERIAL_FLAGS.put(Material.STONE_HOE, MODIFIES_BLOCKS);
        MATERIAL_FLAGS.put(Material.IRON_HOE, MODIFIES_BLOCKS);
        MATERIAL_FLAGS.put(Material.DIAMOND_HOE, MODIFIES_BLOCKS);
        MATERIAL_FLAGS.put(Material.GOLDEN_HOE, MODIFIES_BLOCKS);
        MATERIAL_FLAGS.put(Material.WHEAT_SEEDS, MODIFIES_BLOCKS);
//        MATERIAL_FLAGS.put(Material.WHEAT_CROPS, 0); // Where is this?
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
        MATERIAL_FLAGS.put(Material.GOLDEN_HELMET, 0);
        MATERIAL_FLAGS.put(Material.GOLDEN_CHESTPLATE, 0);
        MATERIAL_FLAGS.put(Material.GOLDEN_LEGGINGS, 0);
        MATERIAL_FLAGS.put(Material.GOLDEN_BOOTS, 0);
        MATERIAL_FLAGS.put(Material.FLINT, 0);
        MATERIAL_FLAGS.put(Material.PORKCHOP, 0);
        MATERIAL_FLAGS.put(Material.COOKED_PORKCHOP, 0);
        MATERIAL_FLAGS.put(Material.PAINTING, 0);
        MATERIAL_FLAGS.put(Material.GOLDEN_APPLE, 0);
        MATERIAL_FLAGS.put(Material.BUCKET, 0);
        MATERIAL_FLAGS.put(Material.WATER_BUCKET, 0);
        MATERIAL_FLAGS.put(Material.LAVA_BUCKET, 0);
        MATERIAL_FLAGS.put(Material.MINECART, 0);
        MATERIAL_FLAGS.put(Material.SADDLE, 0);
        MATERIAL_FLAGS.put(Material.IRON_DOOR, 0);
        MATERIAL_FLAGS.put(Material.REDSTONE, 0);
        MATERIAL_FLAGS.put(Material.SNOWBALL, 0);

        MATERIAL_FLAGS.put(Material.LEATHER, 0);
        MATERIAL_FLAGS.put(Material.MILK_BUCKET, 0);
        MATERIAL_FLAGS.put(Material.BRICKS, 0);
        MATERIAL_FLAGS.put(Material.CLAY_BALL, 0);
        MATERIAL_FLAGS.put(Material.SUGAR_CANE, 0);
        MATERIAL_FLAGS.put(Material.PAPER, 0);
        MATERIAL_FLAGS.put(Material.BOOK, 0);
        MATERIAL_FLAGS.put(Material.SLIME_BALL, 0);
        MATERIAL_FLAGS.put(Material.CHEST_MINECART, 0);
        MATERIAL_FLAGS.put(Material.FURNACE_MINECART, 0);
        MATERIAL_FLAGS.put(Material.EGG, 0);
        MATERIAL_FLAGS.put(Material.COMPASS, 0);
        MATERIAL_FLAGS.put(Material.FISHING_ROD, 0);
        MATERIAL_FLAGS.put(Material.CLOCK, 0);
        MATERIAL_FLAGS.put(Material.GLOWSTONE_DUST, 0);
        MATERIAL_FLAGS.put(Material.COD, 0);
        MATERIAL_FLAGS.put(Material.COOKED_COD, 0);
        MATERIAL_FLAGS.put(Material.INK_SAC, MODIFIES_BLOCKS);
        MATERIAL_FLAGS.put(Material.DANDELION_YELLOW, MODIFIES_BLOCKS);
        MATERIAL_FLAGS.put(Material.CYAN_DYE, MODIFIES_BLOCKS);
        MATERIAL_FLAGS.put(Material.GRAY_DYE, MODIFIES_BLOCKS);
        MATERIAL_FLAGS.put(Material.LIGHT_BLUE_DYE, MODIFIES_BLOCKS);
        MATERIAL_FLAGS.put(Material.LIGHT_GRAY_DYE, MODIFIES_BLOCKS);
        MATERIAL_FLAGS.put(Material.LIME_DYE, MODIFIES_BLOCKS);
        MATERIAL_FLAGS.put(Material.MAGENTA_DYE, MODIFIES_BLOCKS);
        MATERIAL_FLAGS.put(Material.ORANGE_DYE, MODIFIES_BLOCKS);
        MATERIAL_FLAGS.put(Material.PINK_DYE, MODIFIES_BLOCKS);
        MATERIAL_FLAGS.put(Material.PURPLE_DYE, MODIFIES_BLOCKS);
        MATERIAL_FLAGS.put(Material.COCOA_BEANS, MODIFIES_BLOCKS);
        MATERIAL_FLAGS.put(Material.BONE, 0);
        MATERIAL_FLAGS.put(Material.SUGAR, 0);
        MATERIAL_FLAGS.put(Material.COOKIE, 0);
        MATERIAL_FLAGS.put(Material.MAP, 0);
        MATERIAL_FLAGS.put(Material.SHEARS, MODIFIES_BLOCKS);
        MATERIAL_FLAGS.put(Material.MELON_SLICE, 0);
        MATERIAL_FLAGS.put(Material.PUMPKIN_SEEDS, 0);
        MATERIAL_FLAGS.put(Material.MELON_SEEDS, 0);
        MATERIAL_FLAGS.put(Material.BEEF, 0);
        MATERIAL_FLAGS.put(Material.COOKED_BEEF, 0);
        MATERIAL_FLAGS.put(Material.CHICKEN, 0);
        MATERIAL_FLAGS.put(Material.COOKED_CHICKEN, 0);
        MATERIAL_FLAGS.put(Material.ROTTEN_FLESH, 0);
        MATERIAL_FLAGS.put(Material.ENDER_PEARL, 0);
        MATERIAL_FLAGS.put(Material.BLAZE_ROD, 0);
        MATERIAL_FLAGS.put(Material.GHAST_TEAR, 0);
        MATERIAL_FLAGS.put(Material.GOLD_NUGGET, 0);
        MATERIAL_FLAGS.put(Material.NETHER_WART, 0);
        MATERIAL_FLAGS.put(Material.POTION, 0);
        MATERIAL_FLAGS.put(Material.GLASS_BOTTLE, 0);
        MATERIAL_FLAGS.put(Material.SPIDER_EYE, 0);
        MATERIAL_FLAGS.put(Material.FERMENTED_SPIDER_EYE, 0);
        MATERIAL_FLAGS.put(Material.BLAZE_POWDER, 0);
        MATERIAL_FLAGS.put(Material.MAGMA_CREAM, 0);
        MATERIAL_FLAGS.put(Material.ENDER_EYE, 0);
        MATERIAL_FLAGS.put(Material.GLISTERING_MELON_SLICE, 0);
        MATERIAL_FLAGS.put(Material.EXPERIENCE_BOTTLE, 0);
        MATERIAL_FLAGS.put(Material.FIRE_CHARGE, 0);
        MATERIAL_FLAGS.put(Material.WRITABLE_BOOK, 0);
        MATERIAL_FLAGS.put(Material.WRITTEN_BOOK, 0);
        MATERIAL_FLAGS.put(Material.EMERALD, 0);
        MATERIAL_FLAGS.put(Material.ITEM_FRAME, 0);
        MATERIAL_FLAGS.put(Material.CARROT, 0);
        MATERIAL_FLAGS.put(Material.POTATO, 0);
        MATERIAL_FLAGS.put(Material.BAKED_POTATO, 0);
        MATERIAL_FLAGS.put(Material.POISONOUS_POTATO, 0);
        MATERIAL_FLAGS.put(Material.FILLED_MAP, 0);
        MATERIAL_FLAGS.put(Material.GOLDEN_CARROT, 0);
        MATERIAL_FLAGS.put(Material.CREEPER_HEAD, 0);
        MATERIAL_FLAGS.put(Material.CREEPER_WALL_HEAD, 0);
        MATERIAL_FLAGS.put(Material.DRAGON_HEAD, 0);
        MATERIAL_FLAGS.put(Material.DRAGON_WALL_HEAD, 0);
        MATERIAL_FLAGS.put(Material.PLAYER_HEAD, 0);
        MATERIAL_FLAGS.put(Material.PLAYER_WALL_HEAD, 0);
        MATERIAL_FLAGS.put(Material.ZOMBIE_HEAD, 0);
        MATERIAL_FLAGS.put(Material.ZOMBIE_WALL_HEAD, 0);
        MATERIAL_FLAGS.put(Material.SKELETON_SKULL, 0);
        MATERIAL_FLAGS.put(Material.SKELETON_WALL_SKULL, 0);
        MATERIAL_FLAGS.put(Material.WITHER_SKELETON_SKULL, 0);
        MATERIAL_FLAGS.put(Material.WITHER_SKELETON_WALL_SKULL, 0);
        MATERIAL_FLAGS.put(Material.CARROT_ON_A_STICK, 0);
        MATERIAL_FLAGS.put(Material.NETHER_STAR, 0);
        MATERIAL_FLAGS.put(Material.PUMPKIN_PIE, 0);
        MATERIAL_FLAGS.put(Material.FIREWORK_ROCKET, 0);
        MATERIAL_FLAGS.put(Material.FIREWORK_STAR, 0);
        MATERIAL_FLAGS.put(Material.ENCHANTED_BOOK, 0);
        MATERIAL_FLAGS.put(Material.NETHER_BRICKS, 0);
        MATERIAL_FLAGS.put(Material.QUARTZ, 0);
        MATERIAL_FLAGS.put(Material.TNT_MINECART, 0);
        MATERIAL_FLAGS.put(Material.HOPPER_MINECART, 0);
        MATERIAL_FLAGS.put(Material.LEAD, 0);
        MATERIAL_FLAGS.put(Material.NAME_TAG, 0);
        MATERIAL_FLAGS.put(Material.COMMAND_BLOCK_MINECART, 0);

        MATERIAL_FLAGS.put(Material.PRISMARINE_SHARD, 0);
        MATERIAL_FLAGS.put(Material.PRISMARINE_CRYSTALS, 0);
        MATERIAL_FLAGS.put(Material.RABBIT, 0);
        MATERIAL_FLAGS.put(Material.COOKED_RABBIT, 0);
        MATERIAL_FLAGS.put(Material.RABBIT_STEW, 0);
        MATERIAL_FLAGS.put(Material.RABBIT_FOOT, 0);
        MATERIAL_FLAGS.put(Material.RABBIT_HIDE, 0);
        MATERIAL_FLAGS.put(Material.ARMOR_STAND, 0);
        MATERIAL_FLAGS.put(Material.IRON_HORSE_ARMOR, 0);
        MATERIAL_FLAGS.put(Material.GOLDEN_HORSE_ARMOR, 0);
        MATERIAL_FLAGS.put(Material.DIAMOND_HORSE_ARMOR, 0);
        MATERIAL_FLAGS.put(Material.MUTTON, 0);
        MATERIAL_FLAGS.put(Material.COOKED_MUTTON, 0);

        MATERIAL_FLAGS.put(Material.BEETROOT, 0);
        MATERIAL_FLAGS.put(Material.BEETROOT_SOUP, 0);
        MATERIAL_FLAGS.put(Material.BEETROOT_SEEDS, MODIFIES_BLOCKS);
        MATERIAL_FLAGS.put(Material.CHORUS_FRUIT, 0);
        MATERIAL_FLAGS.put(Material.POPPED_CHORUS_FRUIT, 0);
        MATERIAL_FLAGS.put(Material.SHIELD, 0);
        MATERIAL_FLAGS.put(Material.SPECTRAL_ARROW, 0);
        MATERIAL_FLAGS.put(Material.TIPPED_ARROW, 0);
        MATERIAL_FLAGS.put(Material.DRAGON_BREATH, 0);
        MATERIAL_FLAGS.put(Material.LINGERING_POTION, 0);
        MATERIAL_FLAGS.put(Material.ELYTRA, 0);
        MATERIAL_FLAGS.put(Material.END_CRYSTAL, 0);

        MATERIAL_FLAGS.put(Material.TOTEM_OF_UNDYING, 0);
        MATERIAL_FLAGS.put(Material.SHULKER_SHELL, 0);
        MATERIAL_FLAGS.put(Material.KNOWLEDGE_BOOK, 0);

        MATERIAL_FLAGS.put(Material.MUSIC_DISC_11, 0);
        MATERIAL_FLAGS.put(Material.MUSIC_DISC_13, 0);
        MATERIAL_FLAGS.put(Material.MUSIC_DISC_BLOCKS, 0);
        MATERIAL_FLAGS.put(Material.MUSIC_DISC_CAT, 0);
        MATERIAL_FLAGS.put(Material.MUSIC_DISC_CHIRP, 0);
        MATERIAL_FLAGS.put(Material.MUSIC_DISC_FAR, 0);
        MATERIAL_FLAGS.put(Material.MUSIC_DISC_MALL, 0);
        MATERIAL_FLAGS.put(Material.MUSIC_DISC_MELLOHI, 0);
        MATERIAL_FLAGS.put(Material.MUSIC_DISC_STAL, 0);
        MATERIAL_FLAGS.put(Material.MUSIC_DISC_STRAD, 0);
        MATERIAL_FLAGS.put(Material.MUSIC_DISC_WAIT, 0);
        MATERIAL_FLAGS.put(Material.MUSIC_DISC_WARD, 0);

        // Fake tags
        for (Material m : shulkerBoxes) {
            MATERIAL_FLAGS.put(m, MODIFIED_ON_RIGHT);
        }

        // Generated via tag
        for (Material door : Tag.DOORS.getValues()) {
            MATERIAL_FLAGS.put(door, MODIFIED_ON_RIGHT);
        }
        for (Material boat : Tag.ITEMS_BOATS.getValues()) {
            MATERIAL_FLAGS.put(boat, 0);
        }
        for (Material banner : Tag.BANNERS.getValues()) {
            MATERIAL_FLAGS.put(banner, 0);
        }
        for (Material slab : Tag.SLABS.getValues()) {
            MATERIAL_FLAGS.put(slab, 0);
        }
        for (Material plank : Tag.PLANKS.getValues()) {
            MATERIAL_FLAGS.put(plank, 0);
        }
        for (Material carpet : Tag.CARPETS.getValues()) {
            MATERIAL_FLAGS.put(carpet, 0);
        }
        for (Material sapling : Tag.SAPLINGS.getValues()) {
            MATERIAL_FLAGS.put(sapling, 0);
        }
        for (Material log : Tag.LOGS.getValues()) {
            MATERIAL_FLAGS.put(log, 0);
        }
        for (Material leaves : Tag.LEAVES.getValues()) {
            MATERIAL_FLAGS.put(leaves, 0);
        }
        for (Material stair : Tag.STAIRS.getValues()) {
            MATERIAL_FLAGS.put(stair, 0);
        }
        for (Material wool : Tag.WOOL.getValues()) {
           MATERIAL_FLAGS.put(wool, 0);
        }
        for (Material plate : Tag.WOODEN_PRESSURE_PLATES.getValues()) {
            MATERIAL_FLAGS.put(plate, 0);
        }
        for (Material button : Tag.BUTTONS.getValues()) {
            MATERIAL_FLAGS.put(button, MODIFIED_ON_RIGHT);
        }
        for (Material pot : Tag.FLOWER_POTS.getValues()) {
            MATERIAL_FLAGS.put(pot, MODIFIED_ON_RIGHT);
        }

        // Check for missing items/blocks
        for (Material material : Material.values()) {
            // Add spawn eggs
            if (isSpawnEgg(material)) {
                MATERIAL_FLAGS.put(material, 0);
            } else if (isBed(material)) {
                MATERIAL_FLAGS.put(material, MODIFIED_ON_RIGHT);
            }
            if (!MATERIAL_FLAGS.containsKey(material)) {
                logger.fine("Missing item definition for " + material.getKey().toString());
                MATERIAL_FLAGS.put(material, 0);
            }
        }

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

        DAMAGE_EFFECTS.add(PotionEffectType.LEVITATION); // considered by game so I guess
        DAMAGE_EFFECTS.add(PotionEffectType.UNLUCK);

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
            case WATER_BUCKET:
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
        return Tag.LEAVES.getValues().contains(material);
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
        return material == Material.WATER;
    }

    /**
     * Test whether the given material is lava.
     *
     * @param material the material
     * @return true if a lava block
     */
    public static boolean isLava(Material material) {
        return material == Material.LAVA;
    }

    /**
     * Test whether the given material is a portal material.
     *
     * @param material the material
     * @return true if a portal block
     */
    public static boolean isPortal(Material material) {
        return material == Material.NETHER_PORTAL || material == Material.END_PORTAL;
    }

    /**
     * Test whether the given material is a rail block.
     *
     * @param material the material
     * @return true if a rail block
     */
    public static boolean isRailBlock(Material material) {
        return Tag.RAILS.getValues().contains(material);
    }

    /**
     * Test whether the given material is a piston block, not including
     * the "technical blocks" such as the piston extension block.
     *
     * @param material the material
     * @return true if a piston block
     */
    public static boolean isPistonBlock(Material material) {
        return material == Material.PISTON
                || material == Material.STICKY_PISTON;
    }

    /**
     * Test whether the given material is a Minecart.
     *
     * @param material the material
     * @return true if a Minecart item
     */
    public static boolean isMinecart(Material material) {
        return material == Material.MINECART
                || material == Material.COMMAND_BLOCK_MINECART
                || material == Material.TNT_MINECART
                || material == Material.HOPPER_MINECART
                || material == Material.FURNACE_MINECART
                || material == Material.CHEST_MINECART;
    }
    /**
     * Test whether the given material is a Boat.
     *
     * @param material the material
     * @return true if a Boat item
     */
    public static boolean isBoat(Material material) {
        return Tag.ITEMS_BOATS.getValues().contains(material);
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
                || material == Material.BREWING_STAND
                || material == Material.TRAPPED_CHEST
                || material == Material.HOPPER
                || material == Material.DROPPER
                || shulkerBoxes.contains(material);
    }

    public static boolean isSpawnEgg(Material material) {
        switch(material) {
            case SPIDER_SPAWN_EGG:
            case BAT_SPAWN_EGG:
            case BLAZE_SPAWN_EGG:
            case CAVE_SPIDER_SPAWN_EGG:
            case CHICKEN_SPAWN_EGG:
            case COD_SPAWN_EGG:
            case COW_SPAWN_EGG:
            case CREEPER_SPAWN_EGG:
            case DOLPHIN_SPAWN_EGG:
            case DONKEY_SPAWN_EGG:
            case DROWNED_SPAWN_EGG:
            case ELDER_GUARDIAN_SPAWN_EGG:
            case ENDERMAN_SPAWN_EGG:
            case ENDERMITE_SPAWN_EGG:
            case EVOKER_SPAWN_EGG:
            case GHAST_SPAWN_EGG:
            case GUARDIAN_SPAWN_EGG:
            case HORSE_SPAWN_EGG:
            case HUSK_SPAWN_EGG:
            case LLAMA_SPAWN_EGG:
            case MAGMA_CUBE_SPAWN_EGG:
            case MOOSHROOM_SPAWN_EGG:
            case MULE_SPAWN_EGG:
            case OCELOT_SPAWN_EGG:
            case PARROT_SPAWN_EGG:
            case PHANTOM_SPAWN_EGG:
            case PIG_SPAWN_EGG:
            case POLAR_BEAR_SPAWN_EGG:
            case PUFFERFISH_SPAWN_EGG:
            case RABBIT_SPAWN_EGG:
            case SALMON_SPAWN_EGG:
            case SHEEP_SPAWN_EGG:
            case SHULKER_SPAWN_EGG:
            case SILVERFISH_SPAWN_EGG:
            case SKELETON_HORSE_SPAWN_EGG:
            case SKELETON_SPAWN_EGG:
            case SLIME_SPAWN_EGG:
            case SQUID_SPAWN_EGG:
            case STRAY_SPAWN_EGG:
            case TROPICAL_FISH_SPAWN_EGG:
            case TURTLE_SPAWN_EGG:
            case VEX_SPAWN_EGG:
            case VILLAGER_SPAWN_EGG:
            case VINDICATOR_SPAWN_EGG:
            case WITCH_SPAWN_EGG:
            case WITHER_SKELETON_SPAWN_EGG:
            case WOLF_SPAWN_EGG:
            case ZOMBIE_HORSE_SPAWN_EGG:
            case ZOMBIE_PIGMAN_SPAWN_EGG:
            case ZOMBIE_SPAWN_EGG:
            case ZOMBIE_VILLAGER_SPAWN_EGG:
                return true;
            default:
                return false;
        }
    }

    public static EntityType getEntitySpawnEgg(Material material) {
        switch (material) {
            case SPIDER_SPAWN_EGG:
                return EntityType.SPIDER;
            case BAT_SPAWN_EGG:
                return EntityType.BAT;
            case BLAZE_SPAWN_EGG:
                return EntityType.BLAZE;
            case CAVE_SPIDER_SPAWN_EGG:
                return EntityType.CAVE_SPIDER;
            case CHICKEN_SPAWN_EGG:
                return EntityType.CHICKEN;
            case COD_SPAWN_EGG:
                return EntityType.COD;
            case COW_SPAWN_EGG:
                return EntityType.COW;
            case CREEPER_SPAWN_EGG:
                return EntityType.CREEPER;
            case DOLPHIN_SPAWN_EGG:
                return EntityType.DOLPHIN;
            case DONKEY_SPAWN_EGG:
                return EntityType.DONKEY;
            case DROWNED_SPAWN_EGG:
                return EntityType.DROWNED;
            case ELDER_GUARDIAN_SPAWN_EGG:
                return EntityType.ELDER_GUARDIAN;
            case ENDERMAN_SPAWN_EGG:
                return EntityType.ENDERMAN;
            case ENDERMITE_SPAWN_EGG:
                return EntityType.ENDERMITE;
            case EVOKER_SPAWN_EGG:
                return EntityType.EVOKER;
            case GHAST_SPAWN_EGG:
                return EntityType.GHAST;
            case GUARDIAN_SPAWN_EGG:
                return EntityType.GUARDIAN;
            case HORSE_SPAWN_EGG:
                return EntityType.HORSE;
            case HUSK_SPAWN_EGG:
                return EntityType.HUSK;
            case LLAMA_SPAWN_EGG:
                return EntityType.LLAMA;
            case MAGMA_CUBE_SPAWN_EGG:
                return EntityType.MAGMA_CUBE;
            case MOOSHROOM_SPAWN_EGG:
                return EntityType.MUSHROOM_COW;
            case MULE_SPAWN_EGG:
                return EntityType.MULE;
            case OCELOT_SPAWN_EGG:
                return EntityType.OCELOT;
            case PARROT_SPAWN_EGG:
                return EntityType.PARROT;
            case PHANTOM_SPAWN_EGG:
                return EntityType.PHANTOM;
            case PIG_SPAWN_EGG:
                return EntityType.PIG;
            case POLAR_BEAR_SPAWN_EGG:
                return EntityType.POLAR_BEAR;
            case PUFFERFISH_SPAWN_EGG:
                return EntityType.PUFFERFISH;
            case RABBIT_SPAWN_EGG:
                return EntityType.RABBIT;
            case SALMON_SPAWN_EGG:
                return EntityType.SALMON;
            case SHEEP_SPAWN_EGG:
                return EntityType.SHEEP;
            case SHULKER_SPAWN_EGG:
                return EntityType.SHULKER;
            case SILVERFISH_SPAWN_EGG:
                return EntityType.SILVERFISH;
            case SKELETON_HORSE_SPAWN_EGG:
                return EntityType.SKELETON_HORSE;
            case SKELETON_SPAWN_EGG:
                return EntityType.SKELETON;
            case SLIME_SPAWN_EGG:
                return EntityType.SLIME;
            case SQUID_SPAWN_EGG:
                return EntityType.SQUID;
            case STRAY_SPAWN_EGG:
                return EntityType.STRAY;
            case TROPICAL_FISH_SPAWN_EGG:
                return EntityType.TROPICAL_FISH;
            case TURTLE_SPAWN_EGG:
                return EntityType.TURTLE;
            case VEX_SPAWN_EGG:
                return EntityType.VEX;
            case VILLAGER_SPAWN_EGG:
                return EntityType.VILLAGER;
            case VINDICATOR_SPAWN_EGG:
                return EntityType.VINDICATOR;
            case WITCH_SPAWN_EGG:
                return EntityType.WITCH;
            case WITHER_SKELETON_SPAWN_EGG:
                return EntityType.WITHER_SKELETON;
            case WOLF_SPAWN_EGG:
                return EntityType.WOLF;
            case ZOMBIE_HORSE_SPAWN_EGG:
                return EntityType.ZOMBIE_HORSE;
            case ZOMBIE_PIGMAN_SPAWN_EGG:
                return EntityType.PIG_ZOMBIE;
            case ZOMBIE_SPAWN_EGG:
                return EntityType.ZOMBIE;
            case ZOMBIE_VILLAGER_SPAWN_EGG:
                return EntityType.ZOMBIE_VILLAGER;
            default:
                return EntityType.PIG; // Uhhh
        }
    }

    public static boolean isBed(Material material) {
        switch (material) {
            case BLACK_BED:
            case BLUE_BED:
            case BROWN_BED:
            case CYAN_BED:
            case GRAY_BED:
            case GREEN_BED:
            case LIGHT_BLUE_BED:
            case LIGHT_GRAY_BED:
            case LIME_BED:
            case MAGENTA_BED:
            case ORANGE_BED:
            case PINK_BED:
            case PURPLE_BED:
            case RED_BED:
            case WHITE_BED:
                return true;
            default:
                return false;
        }
    }

    /**
     * Test whether the given material is affected by
     * {@link Flags#USE}.
     *
     * <p>Generally, materials that are considered by this method are those
     * that are not inventories but can be used.</p>
     *
     * @param material the material
     * @return true if covered by the use flag
     */
    public static boolean isUseFlagApplicable(Material material) {
        if (Tag.BUTTONS.getValues().contains(material)
            || Tag.DOORS.getValues().contains(material)
            || Tag.WOODEN_PRESSURE_PLATES.getValues().contains(material)) {
            return true;
        }
        switch (material) {
            case LEVER: return true;
            case TRIPWIRE: return true;
            case OAK_TRAPDOOR: return true;
            case ACACIA_TRAPDOOR: return true;
            case BIRCH_TRAPDOOR: return true;
            case DARK_OAK_TRAPDOOR: return true;
            case IRON_TRAPDOOR: return true;
            case JUNGLE_TRAPDOOR: return true;
            case SPRUCE_TRAPDOOR: return true;
            case ENCHANTING_TABLE: return true;
            case ENDER_CHEST: return true;
            case BEACON: return true;
            case ANVIL: return true;
            case STONE_PRESSURE_PLATE: return true;
            case HEAVY_WEIGHTED_PRESSURE_PLATE: return true;
            case LIGHT_WEIGHTED_PRESSURE_PLATE: return true;
            case OAK_FENCE_GATE: return true;
            case SPRUCE_FENCE_GATE: return true;
            case BIRCH_FENCE_GATE: return true;
            case JUNGLE_FENCE_GATE: return true;
            case DARK_OAK_FENCE_GATE: return true;
            case ACACIA_FENCE_GATE: return true;
            default: return false;
        }
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
        return type == Material.REPEATER
            || type == Material.COMPARATOR
            || Tag.FLOWER_POTS.getValues().contains(type);
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
