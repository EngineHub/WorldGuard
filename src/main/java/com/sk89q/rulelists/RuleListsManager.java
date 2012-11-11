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
 * Manages action lists and details relevant to them.
 */
public class RuleListsManager {

    private final AttachmentManager attachments = new AttachmentManager();
    private final DefinitionManager<Criteria<?>> criterion = new DefinitionManager<Criteria<?>>();
    private final DefinitionManager<Action<?>> actions = new DefinitionManager<Action<?>>();
    private final ResolverManager subjectResolvers = new ResolverManager();
    private final ExpressionParser exprParser = new ExpressionParser();

    /**
     * Get the attachment manager.
     *
     * @return the attachment manager
     */
    public AttachmentManager getAttachments() {
        return attachments;
    }

    /**
     * Get the criterion manager.
     *
     * @return criterion manager
     */
    public DefinitionManager<Criteria<?>> getCriterion() {
        return criterion;
    }

    /**
     * Get the actions manager.
     *
     * @return actions manager
     */
    public DefinitionManager<Action<?>> getActions() {
        return actions;
    }

    /**
     * Get the subject resolvers manager.
     *
     * @return the subject resolvers manager
     */
    public ResolverManager getResolvers() {
        return subjectResolvers;
    }

    /**
     * Get the expression parser.
     *
     * @return the expression parser
     */
    public ExpressionParser getParser() {
        return exprParser;
    }

}
