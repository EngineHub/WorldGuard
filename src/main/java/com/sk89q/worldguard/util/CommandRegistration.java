/*
 * WorldGuard
 * Copyright (C) 2011 sk89q <http://www.sk89q.com>
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

package com.sk89q.worldguard.util;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandsManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;

import java.util.*;

/**
 * A helper class for dynamic command registration, including support for fallback methods.
 */
public class CommandRegistration {
	private final Plugin plugin;
	private final CommandsManager<?> commands;
    private CommandMap fallbackCommands;
	
	public CommandRegistration(Plugin plugin, CommandsManager<?> commands) {
		this.plugin = plugin;
		this.commands = commands;
	}

    public boolean register(Class<?> clazz) {
        List<Command> registered = commands.registerAndReturn(clazz);
        CommandMap commandMap = getCommandMap();
        if (registered == null || commandMap == null) {
            return false;
        }
        for (Command command : registered) {
            commandMap.register(plugin.getDescription().getName(), new DynamicPluginCommand(command, plugin));
        }
        return true;
    }

    private CommandMap getCommandMap() {
        CommandMap commandMap = ReflectionUtil.getField(plugin.getServer().getPluginManager(), "commandMap");
        if (commandMap == null) {
            if (fallbackCommands != null) {
                commandMap = fallbackCommands;
            } else {
                Bukkit.getServer().getLogger().warning(plugin.getDescription().getName() +
                        ": Could not retrieve server CommandMap! Please report to http://redmine.sk89q.com");
                fallbackCommands = commandMap = new SimpleCommandMap(Bukkit.getServer());
                Bukkit.getServer().getPluginManager().registerEvents(new FallbackRegistrationListener(fallbackCommands), plugin);
            }
        }
        return commandMap;
    }

    public boolean unregisterCommands() {
        CommandMap commandMap = getCommandMap();
        List<String> toRemove = new ArrayList<String>();
        Map<String, org.bukkit.command.Command> knownCommands = ReflectionUtil.getField(commandMap, "knownCommands");
        Set<String> aliases = ReflectionUtil.getField(commandMap, "aliases");
        if (knownCommands == null || aliases == null) {
            return false;
        }
        for (Iterator<org.bukkit.command.Command> i = knownCommands.values().iterator(); i.hasNext();) {
            org.bukkit.command.Command cmd = i.next();
            if (cmd instanceof DynamicPluginCommand && ((DynamicPluginCommand) cmd).getPlugin().equals(plugin)) {
                i.remove();
                for (String alias : cmd.getAliases()) {
                    org.bukkit.command.Command aliasCmd = knownCommands.get(alias);
                    if (cmd.equals(aliasCmd)) {
                        aliases.remove(alias);
                        toRemove.add(alias);
                    }
                }
            }
        }
        for (String string : toRemove) {
            knownCommands.remove(string);
        }
        return true;
    }

    public static class DynamicPluginCommand extends org.bukkit.command.Command {

		protected final Plugin plugin;

        public DynamicPluginCommand(Command command, Plugin plugin) {
            super(command.aliases()[0], command.desc(), command.usage(), Arrays.asList(command.aliases()));
			this.plugin = plugin;
        }

        @Override
        public boolean execute(CommandSender sender, String label, String[] args) {
            return plugin.onCommand(sender, this, label, args);
        }
        
        public Plugin getPlugin() {
            return plugin;
        }
    }

    public static class FallbackRegistrationListener implements Listener {

        private final CommandMap commandRegistration;

        public FallbackRegistrationListener(CommandMap commandRegistration) {
            this.commandRegistration = commandRegistration;
        }

        @EventHandler
        public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
            if (event.isCancelled()) {
                return;
            }

            if (commandRegistration.dispatch(event.getPlayer(), event.getMessage())) {
                event.setCancelled(true);
            }
        }
    }
}
