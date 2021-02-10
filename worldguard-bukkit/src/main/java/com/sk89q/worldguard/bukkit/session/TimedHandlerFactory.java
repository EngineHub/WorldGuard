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

package com.sk89q.worldguard.bukkit.session;

import co.aikar.timings.lib.MCTiming;
import co.aikar.timings.lib.TimingManager;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.Handler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.security.CodeSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

class TimedHandlerFactory extends Handler.Factory<Handler> {

    private static final TimingManager TIMINGS = TimingManager.of(WorldGuardPlugin.inst());
    private static final MCTiming UNKNOWN_SOURCE = TIMINGS.of("Third-Party Session Handlers");
    private static final Map<CodeSource, TimingManager> PLUGIN_SOURCES = new HashMap<>();

    private final Handler.Factory<?> factory;
    private final MCTiming timing;

    TimedHandlerFactory(Handler.Factory<?> factory) {
        this.factory = factory;
        this.timing = makeTiming();
    }

    private MCTiming makeTiming() {
        CodeSource codeSource = factory.getClass().getProtectionDomain().getCodeSource();
        TimingManager owner = PLUGIN_SOURCES.computeIfAbsent(codeSource, source -> {
            for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                CodeSource pluginSource = plugin.getClass().getProtectionDomain().getCodeSource();
                if (Objects.equals(pluginSource, source)) {
                    return TimingManager.of(plugin);
                }
            }
            return null;
        });
        String handlerName = factory.getClass().getEnclosingClass().getSimpleName();
        return owner == null
            ? TIMINGS.of(handlerName, UNKNOWN_SOURCE)
            : owner.of(handlerName, owner.of("Session Handlers"));
    }

    @Override
    public Handler create(Session session) {
        return new TimedHandler(factory.create(session), session, timing);
    }

    static class TimedHandler extends Handler {
        private final Handler handler;
        private final MCTiming timing;

        TimedHandler(Handler innerHandler, Session session, MCTiming timing) {
            super(session);
            this.handler = innerHandler;
            this.timing = timing;
        }

        @Override
        public void initialize(LocalPlayer player, Location current, ApplicableRegionSet set) {
            try (MCTiming ignored = timing.startTiming()) {
                handler.initialize(player, current, set);
            }
        }

        @Override
        public boolean testMoveTo(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, MoveType moveType) {
            try (MCTiming ignored = timing.startTiming()) {
                return handler.testMoveTo(player, from, to, toSet, moveType);
            }
        }

        @Override
        public boolean onCrossBoundary(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, Set<ProtectedRegion> entered, Set<ProtectedRegion> exited, MoveType moveType) {
            try (MCTiming ignored = timing.startTiming()) {
                return handler.onCrossBoundary(player, from, to, toSet, entered, exited, moveType);
            }
        }

        @Override
        public void tick(LocalPlayer player, ApplicableRegionSet set) {
            try (MCTiming ignored = timing.startTiming()) {
                handler.tick(player, set);
            }
        }

        @Nullable
        @Override
        public StateFlag.State getInvincibility(LocalPlayer player) {
            try (MCTiming ignored = timing.startTiming()) {
                return handler.getInvincibility(player);
            }
        }

        @Override
        public Handler getWrappedHandler() {
            return handler;
        }
    }

}
