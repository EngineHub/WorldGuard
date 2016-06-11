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

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;


public class CustomFlagBrokerImpl implements CustomFlagBroker {
    
    private Logger log = Logger.getLogger(DefaultFlag.class.getCanonicalName());

    /**
     * Add a new custom flag to the flags list
     *
     * @param flag The flag to add
     */
    @Override
    public void addFlag(Flag<?> flag) {
        Flag<?> match = DefaultFlag.fuzzyMatchFlag(flag.getName());
        if (match != null) {
            throw new IllegalArgumentException("Duplicate flag");
        }
        Flag<?>[] newList = Arrays.copyOf(DefaultFlag.flagsList, DefaultFlag.flagsList.length + 1);
        newList[DefaultFlag.flagsList.length] = flag;

        // Force update the flagsList
        // flagsList has to be public to allow other packages to access it, yet
        // we have to be able to change it too :/
        try {
            java.lang.reflect.Field field = DefaultFlag.class.getField("flagsList");
            java.lang.reflect.Field modifiersField = java.lang.reflect.Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~java.lang.reflect.Modifier.FINAL);
            field.set(null, newList);
        } catch (SecurityException e) {
            log.log(Level.WARNING, "A Security Policy prevented setting the custom flag", e);
        } catch (IllegalAccessException e) {
            log.log(Level.WARNING, "Failed to overwrite DefaultFlag.flagsList", e);
        } catch (NoSuchFieldException e) {
            log.log(Level.WARNING, "Failed to find a field", e);
        }
    }
}
