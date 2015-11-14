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

package com.sk89q.worldguard.bukkit.commands;

import com.sk89q.worldguard.bukkit.commands.region.MemberCommands;
import com.sk89q.worldguard.bukkit.commands.region.RegionCommands;
import org.bukkit.command.CommandSender;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.NestedCommand;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class ProtectionCommands {
    @SuppressWarnings("unused")
    private final WorldGuardPlugin plugin;

    public ProtectionCommands(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    @Command(aliases = {"region", "regions", "rg"}, desc = "Region management commands")
    @NestedCommand({RegionCommands.class, MemberCommands.class})
    public void region(CommandContext args, CommandSender sender) {}

    @Command(aliases = {"worldguard", "wg"}, desc = "WorldGuard commands")
    @NestedCommand({WorldGuardCommands.class})
    public void worldGuard(CommandContext args, CommandSender sender) {}
}
