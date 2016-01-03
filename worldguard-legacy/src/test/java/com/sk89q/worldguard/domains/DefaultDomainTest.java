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

import com.sk89q.worldguard.TestPlayer;
import junit.framework.TestCase;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DefaultDomainTest extends TestCase {

    public void testContains() throws Exception {
        TestPlayer player1 = new TestPlayer("test1");
        TestPlayer player2 = new TestPlayer("test2");
        player2.addGroup("group1");
        player2.addGroup("group2");
        TestPlayer player3 = new TestPlayer("test3");
        player3.addGroup("group1");
        player3.addGroup("group3");

        DefaultDomain domain;

        domain = new DefaultDomain();
        domain.addGroup("group1");
        assertThat(domain.contains(player1), is(false));
        assertThat(domain.contains(player2), is(true));
        assertThat(domain.contains(player3), is(true));

        domain = new DefaultDomain();
        domain.addGroup("group1");
        domain.addGroup("group2");
        assertThat(domain.contains(player1), is(false));
        assertThat(domain.contains(player2), is(true));
        assertThat(domain.contains(player3), is(true));

        domain = new DefaultDomain();
        domain.addGroup("group1");
        domain.addGroup("group3");
        assertThat(domain.contains(player1), is(false));
        assertThat(domain.contains(player2), is(true));
        assertThat(domain.contains(player3), is(true));

        domain = new DefaultDomain();
        domain.addGroup("group3");
        assertThat(domain.contains(player1), is(false));
        assertThat(domain.contains(player2), is(false));
        assertThat(domain.contains(player3), is(true));

        domain = new DefaultDomain();
        domain.addPlayer(player1.getName());
        assertThat(domain.contains(player1), is(true));
        assertThat(domain.contains(player2), is(false));
        assertThat(domain.contains(player3), is(false));

        domain = new DefaultDomain();
        domain.addGroup("group3");
        domain.addPlayer(player1.getName());
        assertThat(domain.contains(player1), is(true));
        assertThat(domain.contains(player2), is(false));
        assertThat(domain.contains(player3), is(true));

        domain = new DefaultDomain();
        domain.addGroup("group3");
        domain.addPlayer(player1.getUniqueId());
        assertThat(domain.contains(player1), is(true));
        assertThat(domain.contains(player2), is(false));
        assertThat(domain.contains(player3), is(true));

        domain = new DefaultDomain();
        domain.addGroup("group3");
        domain.addPlayer(player1.getName());
        domain.addPlayer(player1.getUniqueId());
        assertThat(domain.contains(player1), is(true));
        assertThat(domain.contains(player2), is(false));
        assertThat(domain.contains(player3), is(true));

        domain = new DefaultDomain();
        domain.addGroup("group3");
        domain.addPlayer(player1);
        assertThat(domain.contains(player1), is(true));
        assertThat(domain.contains(player2), is(false));
        assertThat(domain.contains(player3), is(true));

        domain = new DefaultDomain();
        domain.addPlayer(player1);
        assertThat(domain.contains(player1), is(true));
        assertThat(domain.contains(player2), is(false));
        assertThat(domain.contains(player3), is(false));

        domain = new DefaultDomain();
        domain.addPlayer(player2);
        domain.addPlayer(player3);
        assertThat(domain.contains(player1), is(false));
        assertThat(domain.contains(player2), is(true));
        assertThat(domain.contains(player3), is(true));
    }

    public void testSize() throws Exception {

    }

}