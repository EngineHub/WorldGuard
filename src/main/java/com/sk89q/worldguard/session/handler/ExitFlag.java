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

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.commands.CommandUtils;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ExitFlag extends FlagValueChangeHandler<State> {

    private static final long MESSAGE_THRESHOLD = 1000 * 2;
    private String storedMessage;
    private boolean exitViaTeleport = false;
    private long lastMessage;

    public ExitFlag(Session session) {
        super(session, DefaultFlag.EXIT);
    }

    private void update(LocalPlayer localPlayer, ApplicableRegionSet set, boolean allowed) {
        if (!allowed) {
            storedMessage = set.queryValue(localPlayer, DefaultFlag.EXIT_DENY_MESSAGE);
            exitViaTeleport = set.testState(localPlayer, DefaultFlag.EXIT_VIA_TELEPORT);
        }
    }

    private void sendMessage(Player player) {
        long now = System.currentTimeMillis();

        if ((now - lastMessage) > MESSAGE_THRESHOLD && storedMessage != null && !storedMessage.isEmpty()) {
            player.sendMessage(CommandUtils.replaceColorMacros(storedMessage));
            lastMessage = now;
        }
    }

    @Override
    protected void onInitialValue(Player player, ApplicableRegionSet set, State value) {
        update(getPlugin().wrapPlayer(player), set, StateFlag.test(value));
    }

    @Override
    protected boolean onSetValue(Player player, Location from, Location to, ApplicableRegionSet toSet, State currentValue, State lastValue, MoveType moveType) {
        if (getSession().getManager().hasBypass(player, from.getWorld())) {
            return true;
        }

        boolean lastAllowed = StateFlag.test(lastValue);
        boolean allowed = StateFlag.test(currentValue);

        LocalPlayer localPlayer = getPlugin().wrapPlayer(player);

        if (allowed && !lastAllowed && !(moveType.isTeleport() && exitViaTeleport) && moveType.isCancellable()) {
            Boolean override = toSet.queryValue(localPlayer, DefaultFlag.EXIT_OVERRIDE);
            if (override == null || !override) {
                sendMessage(player);
                return false;
            }
        }

        update(localPlayer, toSet, allowed);
        return true;
    }

    @Override
    protected boolean onAbsentValue(Player player, Location from, Location to, ApplicableRegionSet toSet, State lastValue, MoveType moveType) {
        if (getSession().getManager().hasBypass(player, from.getWorld())) {
            return true;
        }

        boolean lastAllowed = StateFlag.test(lastValue);

        if (!lastAllowed && moveType.isCancellable()) {
            Boolean override = toSet.queryValue(getPlugin().wrapPlayer(player), DefaultFlag.EXIT_OVERRIDE);
            if (override == null || !override) {
                sendMessage(player);
                return false;
            }
        }

        return true;
    }

}
