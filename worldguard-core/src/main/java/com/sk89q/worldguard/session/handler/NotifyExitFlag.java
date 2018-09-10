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
import com.sk89q.worldedit.util.formatting.ColorCodeBuilder;
import com.sk89q.worldedit.util.formatting.Style;
import com.sk89q.worldedit.util.formatting.StyledFragment;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;

public class NotifyExitFlag extends FlagValueChangeHandler<Boolean> {

    public static final Factory FACTORY = new Factory();
    public static class Factory extends Handler.Factory<NotifyExitFlag> {
        @Override
        public NotifyExitFlag create(Session session) {
            return new NotifyExitFlag(session);
        }
    }

    private Boolean notifiedForLeave = false;

    public NotifyExitFlag(Session session) {
        super(session, Flags.NOTIFY_LEAVE);
    }

    @Override
    protected void onInitialValue(LocalPlayer player, ApplicableRegionSet set, Boolean value) {

    }

    @Override
    protected boolean onSetValue(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, Boolean currentValue, Boolean lastValue, MoveType moveType) {
        return true;
    }

    @Override
<<<<<<< HEAD:worldguard-legacy/src/main/java/com/sk89q/worldguard/session/handler/NotifyExitFlag.java
    protected boolean onAbsentValue(Player player, Location from, Location to, ApplicableRegionSet toSet, Boolean lastValue, MoveType moveType) {
        getPlugin().broadcastNotification(ChatColor.GRAY + "WG: "
                + ChatColor.LIGHT_PURPLE + player.getName()
                + ChatColor.GOLD + " вышел из региона");
=======
    protected boolean onAbsentValue(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, Boolean lastValue, MoveType moveType) {
        WorldGuard.getInstance().getPlatform().broadcastNotification(
                ColorCodeBuilder.asColorCodes(new StyledFragment().append(new StyledFragment(Style.GRAY).append("WG: "))
                        .append(new StyledFragment(Style.PURPLE).append(player.getName()))
                        .append(new StyledFragment(Style.YELLOW_DARK).append(" left NOTIFY region")))
        );
>>>>>>> 8e819f7a823e29fca68fca5f88d575ee7663aa90:worldguard-core/src/main/java/com/sk89q/worldguard/session/handler/NotifyExitFlag.java
        return true;
    }
}
