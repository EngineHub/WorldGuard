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

package com.sk89q.worldguard.commands;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.auth.AuthorizationException;
import com.sk89q.worldguard.WorldGuard;

public class GeneralCompatibleCommands {
    private final GeneralCommands generalCommands;

    public GeneralCompatibleCommands(WorldGuard worldGuard) {
        this.generalCommands = new GeneralCommands(worldGuard);
    }

    @Command(aliases = {"wggod"}, usage = "[player]",
            desc = "Enable godmode on a player", flags = "s", max = 1)
    public void god(CommandContext args, Actor sender) throws CommandException, AuthorizationException {
        generalCommands.god(args, sender);
    }

    @Command(aliases = {"wgungod"}, usage = "[player]",
            desc = "Disable godmode on a player", flags = "s", max = 1)
    public void ungod(CommandContext args, Actor sender) throws CommandException, AuthorizationException {
        generalCommands.ungod(args, sender);
    }

    @Command(aliases = {"wgheal"}, usage = "[player]", desc = "Heal a player", flags = "s", max = 1)
    public void heal(CommandContext args, Actor sender) throws CommandException, AuthorizationException {
        generalCommands.heal(args, sender);
    }

    @Command(aliases = {"wgslay"}, usage = "[player]", desc = "Slay a player", flags = "s", max = 1)
    public void slay(CommandContext args, Actor sender) throws CommandException, AuthorizationException {
        generalCommands.slay(args, sender);
    }

    @Command(aliases = {"wglocate"}, usage = "[player]", desc = "Locate a player", max = 1)
    @CommandPermissions({"worldguard.locate"})
    public void locate(CommandContext args, Actor sender) throws CommandException {
        generalCommands.locate(args, sender);
    }

    @Command(aliases = {"wgstack", ";"}, usage = "", desc = "Stack items", max = 0)
    @CommandPermissions({"worldguard.stack"})
    public void stack(CommandContext args, Actor sender) throws CommandException {
        generalCommands.stack(args, sender);
    }
}
