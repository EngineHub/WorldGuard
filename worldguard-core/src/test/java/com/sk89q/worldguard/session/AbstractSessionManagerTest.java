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

package com.sk89q.worldguard.session;

import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.TestPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.config.ConfigurationManager;
import com.sk89q.worldguard.config.WorldConfiguration;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.PermissiveRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AbstractSessionManagerTest {

    @Test
    public void testCachedBypassExpiresWhileBeingChecked() throws Exception {
        ConfigurationManager config = new TestConfigurationManager();
        WorldGuard.getInstance().setPlatform(stub(WorldGuardPlatform.class, Map.of(
                "getGlobalStateManager", config,
                "getRegionContainer", new TestRegionContainer())));

        World world = stub(World.class, Map.of("getName", "world"));
        TestBypassPlayer player = new TestBypassPlayer(world);
        SessionManager manager = new TestSessionManager();
        manager.get(player);
        assertTrue(manager.hasBypass(player, world));

        player.bypass = false;

        // Moving players are checked on every block crossing, which is far more
        // often than the two second expiry of the cached result.
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        boolean bypass = true;
        while (bypass && System.nanoTime() < deadline) {
            Thread.sleep(50);
            bypass = manager.hasBypass(player, world);
        }
        assertFalse(bypass, "Repeated checks kept a revoked bypass cached");
    }

    private static <T> T stub(Class<T> type, Map<String, Object> answers) {
        return type.cast(Proxy.newProxyInstance(
                type.getClassLoader(), new Class<?>[] {type},
                (proxy, method, args) -> {
                    Object answer = answers.get(method.getName());
                    if (answer == null) {
                        throw new UnsupportedOperationException(method.toString());
                    }
                    return answer;
                }));
    }

    private static final class TestSessionManager extends AbstractSessionManager {
        @Override
        public void resetAllStates() {
        }
    }

    private static final class TestBypassPlayer extends TestPlayer {
        private final World world;
        private volatile boolean bypass = true;

        private TestBypassPlayer(World world) {
            super("test");
            this.world = world;
        }

        @Override
        public boolean hasPermission(String perm) {
            return bypass;
        }

        @Override
        public World getWorld() {
            return world;
        }

        @Override
        public Location getLocation() {
            return new Location(world);
        }
    }

    private static final class TestConfigurationManager extends ConfigurationManager {
        @Override
        public File getDataFolder() {
            return null;
        }

        @Override
        public void load() {
        }

        @Override
        public void unload() {
        }

        @Override
        public WorldConfiguration get(World world) {
            return null;
        }

        @Override
        public void disableUuidMigration() {
        }
    }

    private static final class TestRegionContainer extends RegionContainer {
        @Override
        protected RegionManager load(World world) {
            return null;
        }

        @Override
        public RegionQuery createQuery() {
            return new RegionQuery(cache) {
                @Override
                public ApplicableRegionSet getApplicableRegions(Location location) {
                    return PermissiveRegionSet.getInstance();
                }
            };
        }
    }
}
