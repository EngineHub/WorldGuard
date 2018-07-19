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

package com.sk89q.worldguard.session.handler;

import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;

public class ExitFlag extends FlagValueChangeHandler<State> {

    public static final Factory FACTORY = new Factory();
    public static class Factory extends Handler.Factory<ExitFlag> {
        @Override
        public ExitFlag create(Session session) {
            return new ExitFlag(session);
        }
    }

    private static final long MESSAGE_THRESHOLD = 1000 * 2;
    private String storedMessage;
    private boolean exitViaTeleport = false;
    private long lastMessage;

    public ExitFlag(Session session) {
        super(session, Flags.EXIT);
    }

    private void update(LocalPlayer localPlayer, ApplicableRegionSet set, boolean allowed) {
        if (!allowed) {
            storedMessage = set.queryValue(localPlayer, Flags.EXIT_DENY_MESSAGE);
            exitViaTeleport = set.testState(localPlayer, Flags.EXIT_VIA_TELEPORT);
        }
    }

    private void sendMessage(LocalPlayer player) {
        long now = System.currentTimeMillis();

        if ((now - lastMessage) > MESSAGE_THRESHOLD && storedMessage != null && !storedMessage.isEmpty()) {
            player.printRaw(WorldGuard.getInstance().getPlatform().replaceColorMacros(storedMessage));
            lastMessage = now;
        }
    }

    @Override
    protected void onInitialValue(LocalPlayer player, ApplicableRegionSet set, State value) {
        update(player, set, StateFlag.test(value));
    }

    @Override
    protected boolean onSetValue(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, State currentValue, State lastValue, MoveType moveType) {
        if (getSession().getManager().hasBypass(player, (World) from.getExtent())) {
            return true;
        }

        boolean lastAllowed = StateFlag.test(lastValue);
        boolean allowed = StateFlag.test(currentValue);

        if (allowed && !lastAllowed && !(moveType.isTeleport() && exitViaTeleport) && moveType.isCancellable()) {
            Boolean override = toSet.queryValue(player, Flags.EXIT_OVERRIDE);
            if (override == null || !override) {
                sendMessage(player);
                return false;
            }
        }

        update(player, toSet, allowed);
        return true;
    }

    @Override
    protected boolean onAbsentValue(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, State lastValue, MoveType moveType) {
        if (getSession().getManager().hasBypass(player, (World) from.getExtent())) {
            return true;
        }

        boolean lastAllowed = StateFlag.test(lastValue);

        if (!lastAllowed && moveType.isCancellable()) {
            Boolean override = toSet.queryValue(player, Flags.EXIT_OVERRIDE);
            if (override == null || !override) {
                sendMessage(player);
                return false;
            }
        }

        return true;
    }

}
