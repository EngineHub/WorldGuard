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
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.util.formatting.component.CodeFormat;
import com.sk89q.worldedit.util.formatting.component.ErrorFormat;
import com.sk89q.worldedit.util.formatting.component.LabelFormat;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.event.ClickEvent;
import com.sk89q.worldedit.util.formatting.text.event.HoverEvent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldedit.util.formatting.text.format.TextDecoration;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.config.ConfigurationManager;
import com.sk89q.worldguard.config.WorldConfiguration;
import com.sk89q.worldguard.util.Entities;

public class ToggleCommands {
    private final WorldGuard worldGuard;

    public ToggleCommands(WorldGuard worldGuard) {
        this.worldGuard = worldGuard;
    }

    @Command(aliases = {"stopfire"}, usage = "[<world>]",
            desc = "Disables all fire spread temporarily", max = 1)
    @CommandPermissions({"worldguard.fire-toggle.stop"})
    public void stopFire(CommandContext args, Actor sender) throws CommandException {
        
        World world;
        
        if (args.argsLength() == 0) {
            world = worldGuard.checkPlayer(sender).getWorld();
        } else {
            world = worldGuard.getPlatform().getMatcher().matchWorld(sender, args.getString(0));
        }
        
        WorldConfiguration wcfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager().get(world);

        if (!wcfg.fireSpreadDisableToggle) {
            worldGuard.getPlatform().broadcastNotification(
                    LabelFormat.wrap("Fire spread has been globally disabled for '" + world.getName() + "' by "
                    + sender.getDisplayName() + "."));
        } else {
            sender.print("Fire spread was already globally disabled.");
        }

        wcfg.fireSpreadDisableToggle = true;
    }

    @Command(aliases = {"allowfire"}, usage = "[<world>]",
            desc = "Allows all fire spread temporarily", max = 1)
    @CommandPermissions({"worldguard.fire-toggle.stop"})
    public void allowFire(CommandContext args, Actor sender) throws CommandException {
        
        World world;
        
        if (args.argsLength() == 0) {
            world = worldGuard.checkPlayer(sender).getWorld();
        } else {
            world = worldGuard.getPlatform().getMatcher().matchWorld(sender, args.getString(0));
        }
        
        WorldConfiguration wcfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager().get(world);

        if (wcfg.fireSpreadDisableToggle) {
            worldGuard.getPlatform().broadcastNotification(LabelFormat.wrap("Fire spread has been globally for '" + world.getName() + "' re-enabled by "
                    + sender.getDisplayName() + "."));
        } else {
            sender.print("Fire spread was already globally enabled.");
        }

        wcfg.fireSpreadDisableToggle = false;
    }

    @Command(aliases = {"halt-activity", "stoplag", "haltactivity"}, usage = "[confirm]",
            desc = "Attempts to cease as much activity in order to stop lag", flags = "cis", max = 1)
    @CommandPermissions({"worldguard.halt-activity"})
    public void stopLag(CommandContext args, Actor sender) throws CommandException {

        ConfigurationManager configManager = WorldGuard.getInstance().getPlatform().getGlobalStateManager();

        if (args.hasFlag('i')) {
            if (configManager.activityHaltToggle) {
                 sender.print("ALL intensive server activity is not allowed.");
            } else {
                 sender.print("ALL intensive server activity is allowed.");
            }
        } else {
            boolean activityHaltToggle = !args.hasFlag('c');

            if (activityHaltToggle && (args.argsLength() == 0 || !args.getString(0).equalsIgnoreCase("confirm"))) {
                String confirmCommand = "/" + args.getCommand() + " confirm";

                TextComponent message = TextComponent.builder("")
                        .append(ErrorFormat.wrap("This command will "))
                        .append(ErrorFormat.wrap("PERMANENTLY")
                                .decoration(TextDecoration.BOLD, TextDecoration.State.TRUE))
                        .append(ErrorFormat.wrap(" erase ALL animals in ALL loaded chunks in ALL loaded worlds. "))
                        .append(Component.newline())
                        .append(TextComponent.of("[Click]", TextColor.GREEN)
                                .clickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, confirmCommand))
                                .hoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.of("Click to confirm /" + args.getCommand()))))
                        .append(ErrorFormat.wrap(" or type "))
                        .append(CodeFormat.wrap(confirmCommand)
                                .clickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, confirmCommand)))
                        .append(ErrorFormat.wrap(" to confirm."))
                        .build();

                sender.print(message);
                return;
            }

            configManager.activityHaltToggle = activityHaltToggle;

            if (activityHaltToggle) {
                if (!(sender instanceof LocalPlayer)) {
                    sender.print("ALL intensive server activity halted.");
                }

                if (!args.hasFlag('s')) {
                    worldGuard.getPlatform().broadcastNotification(LabelFormat.wrap("ALL intensive server activity halted by " + sender.getDisplayName() + "."));
                } else {
                    sender.print("(Silent) ALL intensive server activity halted by " + sender.getDisplayName() + ".");
                }

                for (World world : WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.GAME_HOOKS).getWorlds()) {
                    int removed = 0;

                    for (Entity entity : world.getEntities()) {
                        if (Entities.isIntensiveEntity(entity)) {
                            entity.remove();
                            removed++;
                        }
                    }

                    if (removed > 10) {
                        sender.printRaw("" + removed + " entities (>10) auto-removed from "
                                + world.getName());
                    }
                }
            } else {
                if (!args.hasFlag('s')) {
                    worldGuard.getPlatform().broadcastNotification(LabelFormat.wrap("ALL intensive server activity is now allowed."));
                    
                    if (!(sender instanceof LocalPlayer)) {
                        sender.print("ALL intensive server activity is now allowed.");
                    }
                } else {
                    sender.print("(Silent) ALL intensive server activity is now allowed.");
                }
            }
        }
    }
}
