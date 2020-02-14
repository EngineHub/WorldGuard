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
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;

public class DebuggingCommands {

    private final WorldGuard worldGuard;

    /**
     * Create a new instance.
     *
     * @param worldGuard The worldGuard instance
     */
    public DebuggingCommands(WorldGuard worldGuard) {
        this.worldGuard = worldGuard;
    }

    @Command(aliases = {"testbreak"}, usage = "[игрок]", desc = "Имитировать разрушение блока", min = 1, max = 1, flags = "ts")
    @CommandPermissions("worldguard.debug.event")
    public void fireBreakEvent(CommandContext args, final Actor sender) throws CommandException {
        LocalPlayer target = worldGuard.getPlatform().getMatcher().matchSinglePlayer(sender, args.getString(0));
        worldGuard.getPlatform().getDebugHandler().testBreak(sender, target, args.hasFlag('t'), args.hasFlag('s'));
    }


    @Command(aliases = {"testplace"}, usage = "[игрок]", desc = "Имитировать размещение блока", min = 1, max = 1, flags = "ts")
    @CommandPermissions("worldguard.debug.event")
    public void firePlaceEvent(CommandContext args, final Actor sender) throws CommandException {
        LocalPlayer target = worldGuard.getPlatform().getMatcher().matchSinglePlayer(sender, args.getString(0));
        worldGuard.getPlatform().getDebugHandler().testPlace(sender, target, args.hasFlag('t'), args.hasFlag('s'));
    }

    @Command(aliases = {"testinteract"}, usage = "[игрок]", desc = "Имитировать взаимодействие с блоком", min = 1, max = 1, flags = "ts")
    @CommandPermissions("worldguard.debug.event")
    public void fireInteractEvent(CommandContext args, final Actor sender) throws CommandException {
        LocalPlayer target = worldGuard.getPlatform().getMatcher().matchSinglePlayer(sender, args.getString(0));
        worldGuard.getPlatform().getDebugHandler().testInteract(sender, target, args.hasFlag('t'), args.hasFlag('s'));
    }

    @Command(aliases = {"testdamage"}, usage = "[игрок]", desc = "Имитация повреждения объекта", min = 1, max = 1, flags = "ts")
    @CommandPermissions("worldguard.debug.event")
    public void fireDamageEvent(CommandContext args, final Actor sender) throws CommandException {
        LocalPlayer target = worldGuard.getPlatform().getMatcher().matchSinglePlayer(sender, args.getString(0));
        worldGuard.getPlatform().getDebugHandler().testDamage(sender, target, args.hasFlag('t'), args.hasFlag('s'));
    }
}
