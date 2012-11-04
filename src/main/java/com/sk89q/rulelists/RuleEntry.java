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

import java.util.List;

/**
 * A pair of attachment/event names a rule.
 */
public class RuleEntry {
    
    private final List<String> attachmentNames;
    private final Rule<?> rule;
    
    /**
     * Construct the rule entry with the list of attachment names and rules.
     * 
     * @param attachmentNames attachment names
     * @param rule rules
     */
    public RuleEntry(List<String> attachmentNames, Rule<?> rule) {
        this.attachmentNames = attachmentNames;
        this.rule = rule;
    }

    /**
     * Get the list of attachment names.
     * 
     * @return attachment names
     */
    public List<String> getAttachmentNames() {
        return attachmentNames;
    }

    /**
     * Get the rule.
     * 
     * @return rule
     */
    public Rule<?> getRule() {
        return rule;
    }

}
