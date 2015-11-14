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

package com.sk89q.worldguard.domains;

import com.sk89q.worldguard.LocalPlayer;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @deprecated not used by WorldGuard and not maintained
 */
@Deprecated
public class DomainCollection implements Domain {

    private Set<Domain> domains;

    public DomainCollection() {
        domains = new LinkedHashSet<Domain>();
    }

    public void add(Domain domain) {
        domains.add(domain);
    }

    public void remove(Domain domain) {
        domains.remove(domain);
    }

    @Override
    public int size() {
        return domains.size();
    }

    @Override
    public void clear() {
        domains.clear();
    }

    @Override
    public boolean contains(LocalPlayer player) {
        for (Domain domain : domains) {
            if (domain.contains(player)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean contains(UUID uniqueId) {
        for (Domain domain : domains) {
            if (domain.contains(uniqueId)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean contains(String playerName) {
        for (Domain domain : domains) {
            if (domain.contains(playerName)) {
                return true;
            }
        }

        return false;
    }

}
