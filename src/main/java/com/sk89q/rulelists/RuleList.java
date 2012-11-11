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

import java.util.HashMap;
import java.util.Map;

/**
 * Holds all the rules.
 */
public class RuleList {

    private Map<Attachment, RuleSet> collections = new HashMap<Attachment, RuleSet>();

    /**
     * Build a new instance.
     */
    public RuleList() {
    }

    /**
     * Get a rule set for a given attachment. A rule set is guaranteed to be
     * returned for all given attachments.
     *
     * @param attachment the attachment
     * @return the rule set
     */
    public synchronized RuleSet get(Attachment attachment) {
        RuleSet collection = collections.get(attachment);
        if (collection == null) {
            collection = new RuleSet();
            collections.put(attachment, collection);
        }
        return collection;
    }

    /**
     * Register a rule with this rule list.
     *
     * @param entry entry
     */
    public void learn(RuleEntry entry) {
        Rule<?> rule = entry.getRule();

        for (Attachment attachment : entry.getAttachments()) {
            learn(attachment, rule);
        }
    }

    /**
     * Register a rule with this rule list.
     *
     * @param attachment the attachment for the rule
     * @param rule the rule itself
     */
    public void learn(Attachment attachment, Rule<?> rule) {
        get(attachment).learn(rule);
    }

    /**
     * Forget all rules.
     */
    public void clear() {
        collections.clear();
    }

}
