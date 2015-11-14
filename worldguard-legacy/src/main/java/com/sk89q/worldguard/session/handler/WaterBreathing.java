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

import com.sk89q.worldguard.session.Session;
import org.bukkit.entity.Player;

public class WaterBreathing extends Handler {

    public boolean waterBreathing;

    public WaterBreathing(Session session) {
        super(session);
    }

    public boolean hasWaterBreathing() {
        return waterBreathing;
    }

    public void setWaterBreathing(boolean waterBreathing) {
        this.waterBreathing = waterBreathing;
    }

    public static boolean set(Player player, Session session, boolean value) {
        WaterBreathing waterBreathing = session.getHandler(WaterBreathing.class);
        if (waterBreathing != null) {
            waterBreathing.setWaterBreathing(value);
            return true;
        } else{
            return false;
        }
    }

}
