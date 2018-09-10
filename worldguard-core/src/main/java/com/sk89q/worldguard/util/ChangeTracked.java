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

package com.sk89q.worldguard.util;

/**
 * An object that keeps track of a dirty flag that is set to true when changes
 * are made to this object.
 */
public interface ChangeTracked {

    /**
     * Tests whether changes have been made.
     *
     * @return true if changes have been made
     */
    boolean isDirty();

    /**
     * Set whether changes have been made.
     *
     * @param dirty a new dirty state
     */
    void setDirty(boolean dirty);

}
