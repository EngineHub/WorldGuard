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

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A {@link Rule} implementation with a list of {@link Criteria} and
 * {@link Action}s.
 *
 * @param <T> underlying implementation-specific context
 */
public class DefinedRule<T extends Context> implements Rule<T> {
    
    private static Logger logger = Logger.getLogger(Rule.class.getCanonicalName());

    private final List<Criteria<T>> criterion = new LinkedList<Criteria<T>>();
    private final List<Action<T>> actions = new LinkedList<Action<T>>();
    
    /**
     * Add a criteria to this rule.
     * 
     * @param criteria the criteria
     */
    public void add(Criteria<T> criteria) {
        criterion.add(criteria);
    }
    
    /**
     * Add an action to this rule.
     * 
     * @param action the action
     */
    public void add(Action<T> action) {
        actions.add(action);
    }
    
    /**
     * Returns whether criteria are defined.
     * 
     * @return true if there are criteria
     */
    public boolean hasCriterion() {
        return criterion.size() > 0;
    }
    
    /**
     * Returns whether actions are defined.
     * 
     * @return true if there are actions
     */
    public boolean hasActions() {
        return actions.size() > 0;
    }
    
    @Override
    public boolean matches(T context) {
        for (Criteria<T> criteria : criterion) {
            if (!criteria.matches(context)) {
                context.setMatches(false);
                return false;
            }
        }

        context.setMatches(true);
        return true;
    }
    
    @Override
    public void apply(T context) {
        for (Action<T> action : actions) {
            try {
                action.apply(context);
            } catch (Throwable t) {
                logger.log(Level.WARNING, "Failed to apply action of rule", t);
            }
        }
    }
    
}
