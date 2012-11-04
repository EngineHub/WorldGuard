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

public class RuleSet implements Attachment {
    
    private List<Rule<?>> rules = new LinkedList<Rule<?>>();

    @Override
    public void learn(Rule<?> rule) {
        rules.add(rule);
    }

    @Override
    public void forgetRules() {
        rules.clear();
    }
    
    public boolean hasRules() {
        return rules.size() > 0;
    }
    
    public List<Rule<?>> getRules() {
        return rules;
    }
    
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
