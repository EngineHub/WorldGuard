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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Holds a collection of rules that can be applied to a context.
 *
 * @see RuleList
 */
public class RuleSet {

    private List<Rule<?>> rules = new LinkedList<Rule<?>>();

    RuleSet() {
    }

    /**
     * Add the given rule to this set.
     *
     * @param rule the rule
     */
    public synchronized void learn(Rule<?> rule) {
        rules.add(rule);
    }

    /**
     * Forget all rules.
     */
    public synchronized void clear() {
        rules.clear();
    }

    /**
     * Return whether this set has rules.
     *
     * @return true if there are rules
     */
    public synchronized boolean hasRules() {
        return rules.size() > 0;
    }

    /**
     * Get the list of rules. The returned list cannot be modified.
     *
     * @return rule list
     */
    public synchronized List<Rule<?>> getRules() {
        return Collections.unmodifiableList(rules);
    }

    /**
     * Apply this rule set onto the given context. Only one thread should call
     * this method at a given time.
     *
     * @param context the context
     * @return true if the event has been cancelled
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public boolean process(Context context) {
        if (context.isCancelled()) {
            return true; // Ignore cancelled events for now
        }

        for (Rule rule : rules) {
            if (rule.matches(context)) {
                rule.apply(context);
            }
        }

        return context.isCancelled();
    }

}
