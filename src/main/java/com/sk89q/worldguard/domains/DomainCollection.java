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

package com.sk89q.worldguard.domains;

import java.util.Set;
import java.util.LinkedHashSet;
import com.sk89q.worldguard.LocalPlayer;

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
    
    public int size() {
        return domains.size();
    }

    public boolean contains(LocalPlayer player) {
        for (Domain domain : domains) {
            if (domain.contains(player)) {
                return true;
            }
        }
        
        return false;
    }

}
