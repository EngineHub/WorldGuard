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
package com.sk89q.worldguard.protection.flags;

/**
 * 
 * Bucket plugins that need to use custom world guard flags should implement
 * this interface. World Guard will invoke the addCustomFlags method on every
 * plugin that implements this when world guard's "onEnable" is called.
 * 
 * Plugin's can then use the provided broker object to add custom flags. Plugins
 * should add custom flags only, and not attempt to access bukkit or world
 * edit APIs, are all of the plugins have not been loaded yet.
 * 
 * Every plugins "onLoad()" will be called before addCustomFlags() is called, but
 * this could be before or after "onEnable()" is called.
 * 
 * @author Challenger2
 *
 */
public interface CustomFlagProvider {

    public void addCustomFlags(CustomFlagBroker broker);
}
