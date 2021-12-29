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

package com.sk89q.worldguard.domains.registry;

import com.sk89q.worldguard.domains.CustomDomain;

import java.util.UUID;

public class UnknownDomain extends CustomDomain {
    public static DomainFactory<UnknownDomain> FACTORY = UnknownDomain::new;

    private boolean isDirty = false;
    private Object o;

    public UnknownDomain(String name) {
        super(name);
    }

    @Override
    public void parseInput(CustomDomainContext context) throws InvalidDomainFormatException {
        throw new InvalidDomainFormatException("The plugin that registered this domain is not currently installed");
    }

    @Override
    public void unmarshal(Object o) {
        this.o = o;
    }

    @Override
    public Object marshal() {
        return o;
    }

    @Override
    public boolean contains(UUID uniqueId) {
        return false;
    }

    @Override
    public boolean contains(String playerName) {
        return false;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void clear() {
        isDirty = true;
        o = null;
    }

    @Override
    public void setDirty(boolean dirty) {
        isDirty = dirty;
    }

    @Override
    public boolean isDirty() {
        return isDirty;
    }

    @Override
    public String toString() {
        return "UnknownDomain{" +
                "o=" + o +
                '}';
    }
}
