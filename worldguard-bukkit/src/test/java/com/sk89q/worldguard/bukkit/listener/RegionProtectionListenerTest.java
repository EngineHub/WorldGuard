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

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.cause.Cause;
import com.sk89q.worldguard.bukkit.event.entity.UseEntityEvent;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import com.sk89q.worldguard.protection.RegionResultSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class RegionProtectionListenerTest {

    @Test
    void chestAccessAllowsInitialInventoryMinecartClick() {
        assertFalse(checkUse(StorageMinecart.class, EntityType.CHEST_MINECART,
                PlayerInteractEntityEvent.class, State.ALLOW));
        assertFalse(checkUse(HopperMinecart.class, EntityType.HOPPER_MINECART,
                PlayerInteractEntityEvent.class, State.ALLOW));
    }

    @Test
    void chestAccessStillDeniesInventoryMinecartClick() {
        assertTrue(checkUse(StorageMinecart.class, EntityType.CHEST_MINECART,
                PlayerInteractEntityEvent.class, State.DENY));
    }

    @Test
    void inventoryOpenAndTransferStillUseChestAccess() {
        assertFalse(checkUse(StorageMinecart.class, EntityType.CHEST_MINECART,
                InventoryOpenEvent.class, State.ALLOW));
        assertTrue(checkUse(StorageMinecart.class, EntityType.CHEST_MINECART,
                InventoryMoveItemEvent.class, State.DENY));
    }

    @Test
    void chestAccessDoesNotAllowOtherEntityChanges() {
        assertTrue(checkUse(ArmorStand.class, EntityType.ARMOR_STAND,
                PlayerInteractEntityEvent.class, State.ALLOW));
    }

    @Test
    void ordinaryMinecartStillDefersToMountProtection() {
        assertFalse(checkUse(Minecart.class, EntityType.MINECART,
                PlayerInteractEntityEvent.class, State.DENY));
    }

    private <T extends Entity> boolean checkUse(
            Class<T> entityClass, EntityType type,
            Class<? extends Event> eventClass, State chestAccess) {
        ProtectedCuboidRegion region = new ProtectedCuboidRegion("test",
                BlockVector3.at(-1, 0, -1), BlockVector3.at(1, 100, 1));
        region.setFlag(Flags.CHEST_ACCESS, chestAccess);
        RegionQuery query = mock(RegionQuery.class, CALLS_REAL_METHODS);
        doReturn(new RegionResultSet(new ArrayList<>(List.of(region)), null)).when(query)
                .getApplicableRegions(any(com.sk89q.worldedit.util.Location.class));

        WorldGuard worldGuard = mock(WorldGuard.class);
        WorldGuardPlatform platform = mock(WorldGuardPlatform.class);
        RegionContainer container = mock(RegionContainer.class);
        when(worldGuard.getPlatform()).thenReturn(platform);
        when(platform.getRegionContainer()).thenReturn(container);
        when(container.createQuery()).thenReturn(query);

        World world = mock(World.class);
        T entity = mock(entityClass);
        when(entity.getType()).thenReturn(type);
        when(entity.getLocation()).thenReturn(new Location(world, 0, 64, 0));
        UseEntityEvent event = new UseEntityEvent(mock(eventClass), Cause.unknown(), entity);
        event.setSilent(true);

        var worldEditWorld = mock(com.sk89q.worldedit.world.World.class);
        var worldEditLocation = new com.sk89q.worldedit.util.Location(
                worldEditWorld, 0, 64, 0);
        try (MockedStatic<WorldGuard> guard = mockStatic(WorldGuard.class);
             MockedStatic<AbstractListener> listener = mockStatic(AbstractListener.class);
             MockedStatic<BukkitAdapter> adapter = mockStatic(BukkitAdapter.class)) {
            guard.when(WorldGuard::getInstance).thenReturn(worldGuard);
            listener.when(() -> AbstractListener.isRegionSupportEnabled(world)).thenReturn(true);
            adapter.when(() -> BukkitAdapter.adapt(any(Location.class)))
                    .thenReturn(worldEditLocation);
            new RegionProtectionListener(mock(WorldGuardPlugin.class)).onUseEntity(event);
        }
        return event.isCancelled();
    }
}
