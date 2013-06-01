// $Id$
/*
 * This file is a part of WorldGuard.
 * Copyright (c) sk89q <http://www.sk89q.com>
 * Copyright (c) the WorldGuard team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY), without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldguard.bukkit;

import org.bukkit.command.CommandSender;
import org.enginehub.util.PermissionModel;

abstract class AbstractPermissionModel implements PermissionModel {
    
    private final WorldGuardPlugin plugin;
    private final CommandSender sender;

    public AbstractPermissionModel(WorldGuardPlugin plugin, CommandSender sender) {
        this.plugin = plugin;
        this.sender = sender;
    }
    
    protected WorldGuardPlugin getPlugin() {
        return plugin;
    }

    public CommandSender getSender() {
        return sender;
    }

    protected boolean hasPluginPermission(String permission) {
        return plugin.hasPermission(getSender(), "worldguard." + permission);
    }

}
