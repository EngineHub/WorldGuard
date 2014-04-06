// $Id$
/*
 * WorldGuard
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldguard.bukkit.commands;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.LoggerToChatHandler;
import com.sk89q.worldguard.bukkit.ReportWriter;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.util.PastebinPoster;
import com.sk89q.worldguard.util.PastebinPoster.PasteCallback;

public class WorldGuardCommands {
    private final WorldGuardPlugin plugin;

    public WorldGuardCommands(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    @Command(aliases = {"version"}, desc = "Get the WorldGuard version", max = 0)
    public void version(CommandContext args, CommandSender sender) throws CommandException {
        sender.sendMessage(ChatColor.YELLOW
                + "WorldGuard " + plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.YELLOW
                + "http://www.sk89q.com");
    }

    @Command(aliases = {"reload"}, desc = "Reload WorldGuard configuration", max = 0)
    @CommandPermissions({"worldguard.reload"})
    public void reload(CommandContext args, CommandSender sender) throws CommandException {
        
        LoggerToChatHandler handler = null;
        Logger minecraftLogger = null;
        
        if (sender instanceof Player) {
            handler = new LoggerToChatHandler(sender);
            handler.setLevel(Level.ALL);
            minecraftLogger = Logger.getLogger("Minecraft");
            minecraftLogger.addHandler(handler);
        }

        try {
            plugin.getGlobalStateManager().unload();
            plugin.getGlobalRegionManager().unload();
            plugin.getGlobalStateManager().load();
            plugin.getGlobalRegionManager().preload();
            // WGBukkit.cleanCache();
            sender.sendMessage(BukkitUtil.replaceColorMacros(
                    plugin.getGlobalStateManager().getLocale("COMMAND_RELOAD_SUCCESS")));
        } catch (Throwable t) {
            sender.sendMessage(BukkitUtil.replaceColorMacros(
                    plugin.getGlobalStateManager().getLocale("COMMAND_RELOAD_ERROR")
                    .replace("%errorMessage%", t.getMessage())));
        } finally {
            if (minecraftLogger != null) {
                minecraftLogger.removeHandler(handler);
            }
        }
    }
    
    @Command(aliases = {"report"}, desc = "Writes a report on WorldGuard", flags = "p", max = 0)
    @CommandPermissions({"worldguard.report"})
    public void report(CommandContext args, final CommandSender sender) throws CommandException {
        
        File dest = new File(plugin.getDataFolder(), "report.txt");
        ReportWriter report = new ReportWriter(plugin);
        
        try {
            report.write(dest);
            sender.sendMessage(BukkitUtil.replaceColorMacros(
                    plugin.getGlobalStateManager().getLocale("COMMAND_REPORT_WRITTEN")
                    .replace("%path%", dest.getAbsolutePath())));
        } catch (IOException e) {
            throw new CommandException(plugin.getGlobalStateManager().getLocale("EX_REPORT_ERROR")
                    .replace("%errorMessage%", e.getMessage()));
        }
        
        if (args.hasFlag('p')) {
            plugin.checkPermission(sender, "worldguard.report.pastebin");
            
            sender.sendMessage(BukkitUtil.replaceColorMacros(
                    plugin.getGlobalStateManager().getLocale("COMMAND_REPORT_UPLOADING")));
            PastebinPoster.paste(report.toString(), new PasteCallback() {
                
                public void handleSuccess(String url) {
                    // Hope we don't have a thread safety issue here
                    sender.sendMessage(BukkitUtil.replaceColorMacros(
                            plugin.getGlobalStateManager().getLocale("COMMAND_REPORT_UPLOAD_SUCCESS"))
                            .replace("%url%", url));
                }
                
                public void handleError(String err) {
                    // Hope we don't have a thread safety issue here
                    sender.sendMessage(BukkitUtil.replaceColorMacros(
                            plugin.getGlobalStateManager().getLocale("COMMAND_REPORT_UPLOAD_ERROR"))
                            .replace("%errorMessage%", err));
                }
            });
        }

    }

    @Command(aliases = {"flushstates", "clearstates"},
            usage = "[player]", desc = "Flush the state manager", max = 1)
    @CommandPermissions("worldguard.flushstates")
    public void flushStates(CommandContext args, CommandSender sender) throws CommandException {
        if (args.argsLength() == 0) {
            plugin.getFlagStateManager().forgetAll();
            sender.sendMessage(BukkitUtil.replaceColorMacros(
                    plugin.getGlobalStateManager().getLocale("COMMAND_FLUSHSTATES_SELF")));
        } else {
            Player player = plugin.getServer().getPlayer(args.getString(0));
            if (player != null) {
                plugin.getFlagStateManager().forget(player);
                sender.sendMessage(BukkitUtil.replaceColorMacros(
                        plugin.getGlobalStateManager().getLocale("COMMAND_FLUSHSTATES_OTHERS")
                        .replace("%playerName%", player.getName())));
            }
        }
    }

}
