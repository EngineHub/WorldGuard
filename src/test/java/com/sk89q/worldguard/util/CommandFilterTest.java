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

package com.sk89q.worldguard.util;

import com.sk89q.worldguard.util.command.CommandFilter;
import com.sk89q.worldguard.util.command.CommandFilter.Builder;
import junit.framework.TestCase;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CommandFilterTest extends TestCase {

    private static final String[] COMMAND_SEPARATORS = new String[] {" ", "  ", "\t", " \t", "\n", "\r\n"};

    public void testApply() throws Exception {
        CommandFilter filter;

        // ====================================================================
        // No rules
        // ====================================================================

        filter = new Builder().build();
        assertSubcommands(filter, "/permit1", true);
        assertSubcommands(filter, "/deny1", true);
        assertSubcommands(filter, "/other", true);

        // ====================================================================
        // Root PERMIT
        // ====================================================================

        filter = new Builder()
                .permit("/permit1", "/permit2")
                .build();
        assertSubcommands(filter, "/permit1", true);
        assertSubcommands(filter, "/permit2", true);
        assertSubcommands(filter, "/other", false);

        // ====================================================================
        // Root DENY
        // ====================================================================

        filter = new Builder()
                .deny("/deny1", "/deny2")
                .build();
        assertSubcommands(filter, "/deny1", false);
        assertSubcommands(filter, "/deny2", false);
        assertSubcommands(filter, "/other", true);

        // ====================================================================
        // Root PERMIT + DENY no overlap
        // ====================================================================

        filter = new Builder()
                .permit("/permit1", "/permit2")
                .deny("/deny1", "/deny2")
                .build();
        assertSubcommands(filter, "/permit1", true);
        assertSubcommands(filter, "/permit1", true);
        assertSubcommands(filter, "/deny1", false);
        assertSubcommands(filter, "/deny2", false);
        assertSubcommands(filter, "/other", false);

        // ====================================================================
        // Root PERMIT + DENY WITH overlap
        // ====================================================================

        filter = new Builder()
                .permit("/permit1", "/permit2", "/strange")
                .deny("/deny1", "/deny2", "/strange")
                .build();
        assertSubcommands(filter, "/permit1", true);
        assertSubcommands(filter, "/permit1", true);
        assertSubcommands(filter, "/deny1", false);
        assertSubcommands(filter, "/deny2", false);
        assertSubcommands(filter, "/strange", true);
        assertSubcommands(filter, "/other", false);

        // ====================================================================
        // Subcommand PERMIT
        // ====================================================================

        filter = new Builder()
                .permit("/permit1", "/parent permit1", "/parent between subpermit1")
                .build();
        assertSubcommands(filter, "/permit1", true);
        assertSubcommands(filter, "/parent", false);
        assertSubcommands(filter, "/parent permit1", true);
        assertSubcommands(filter, "/parent other", false);
        assertSubcommands(filter, "/parent between", false);
        assertSubcommands(filter, "/parent between subpermit1", true);
        assertSubcommands(filter, "/parent between other", false);
        assertSubcommands(filter, "/parent between other subpermit1", false);
        assertSubcommands(filter, "/other", false);
        assertSubcommands(filter, "/other permit1", false);
        assertSubcommands(filter, "/other between", false);
        assertSubcommands(filter, "/other between subpermit1", false);

        // ====================================================================
        // Mixed DENY
        // ====================================================================

        filter = new Builder()
                .deny("/deny1", "/parent deny1", "/parent between subdeny1")
                .build();
        assertSubcommands(filter, "/deny1", false);
        assertSubcommands(filter, "/parent", true);
        assertSubcommands(filter, "/parent deny1", false);
        assertSubcommands(filter, "/parent between", true);
        assertSubcommands(filter, "/parent between subdeny1", false);
        assertSubcommands(filter, "/parent between else", true);
        assertSubcommands(filter, "/parent else", true);
        assertSubcommands(filter, "/other", true);
        assertSubcommands(filter, "/other deny1", true);
        assertSubcommands(filter, "/other between", true);
        assertSubcommands(filter, "/other between subdeny1", true);
        assertSubcommands(filter, "/other between else", true);

        // ====================================================================
        // Mixed PERMIT + DENY no overlap
        // ====================================================================

        filter = new Builder()
                .deny("/deny1", "/denyparent deny1", "/denyparent between subdeny1")
                .permit("/permit1", "/permitparent permit1", "/permitparent between subpermit1")
                .build();
        assertSubcommands(filter, "/deny1", false);
        assertSubcommands(filter, "/denyparent", false);
        assertSubcommands(filter, "/denyparent deny1", false);
        assertSubcommands(filter, "/denyparent else", false);
        assertSubcommands(filter, "/denyparent between", false);
        assertSubcommands(filter, "/denyparent between subdeny1", false);
        assertSubcommands(filter, "/permit1", true);
        assertSubcommands(filter, "/permitparent", false);
        assertSubcommands(filter, "/permitparent permit1", true);
        assertSubcommands(filter, "/permitparent else", false);
        assertSubcommands(filter, "/permitparent between", false);
        assertSubcommands(filter, "/permitparent between subpermit1", true);
        assertSubcommands(filter, "/other", false);
        assertSubcommands(filter, "/other permit1", false);
        assertSubcommands(filter, "/other between", false);
        assertSubcommands(filter, "/other between subpermit1", false);

        // ====================================================================
        // Mixed PERMIT + DENY overlap
        // ====================================================================

        filter = new Builder()
                .deny("/deny1", "/parent deny1", "/parent between subdeny1", "/parent between", "/parent between strange", "/parent between strange sub")
                .permit("/permit1", "/parent permit1", "/parent between sub", "/parent between strange")
                .build();
        assertSubcommands(filter, "/deny1", false);
        assertSubcommands(filter, "/parent", false);
        assertSubcommands(filter, "/parent deny1", false);
        assertSubcommands(filter, "/parent else", false);
        assertSubcommands(filter, "/parent permit1", true);
        assertSubcommands(filter, "/parent between", false);
        assertSubcommands(filter, "/parent between sub", true);
        assertSubcommands(filter, "/parent between other", false);
        assertSubcommands(filter, "/parent between strange", true);
        assertSubcommands(filter, "/parent between strange sub", true);
        assertSubcommands(filter, "/parent between strange other", true);
        assertSubcommands(filter, "/permit1", true);
        assertSubcommands(filter, "/permit1 deny1", true);
        assertSubcommands(filter, "/other", false);
        assertSubcommands(filter, "/other permit1", false);
        assertSubcommands(filter, "/other between", false);
        assertSubcommands(filter, "/other between subpermit1", false);
    }

    private void assertSubcommands(CommandFilter filter, final String root, boolean expected) {
        for (String separator : COMMAND_SEPARATORS) {
            assertThat(filter.apply(root.replaceAll(" ", separator)), is(expected));
            assertThat(filter.apply((root + " _subcmd").replaceAll(" ", separator)), is(expected));
            assertThat(filter.apply((root + " _subcmd _another").replaceAll(" ", separator)), is(expected));
        }
    }

}