// $Id$
/*
 * This file is a part of WorldGuard.
 * Copyright (c) sk89q <http://www.sk89q.com>
 * Copyright (c) the WorldGuard team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.rulelists;

/**
 * A criteria wrapper that inverts the condition.
 * 
 * @author sk89q
 * @param <T> underlying criteria
 */
public class InvertedCriteria<T extends Context> implements Criteria<T> {
    
    private final Criteria<T> criteria;
    
    public InvertedCriteria(Criteria<T> criteria) {
        this.criteria = criteria;
    }

    @Override
    public boolean matches(T context) {
        return !criteria.matches(context);
    }

}
