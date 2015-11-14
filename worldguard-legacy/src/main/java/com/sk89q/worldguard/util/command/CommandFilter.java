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

package com.sk89q.worldguard.util.command;

import com.google.common.base.Predicate;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Checks whether a command is permitted with support for subcommands
 * split by {@code \s} (regular expressions).
 *
 * <p>{@code permitted} always overrides {@code denied} (unlike other parts of
 * WorldGuard). Either can be null. If both are null, then every command is
 * permitted. If only {@code permitted} is null, then all commands but
 * those in the list of denied are permitted. If only {@code denied} is null,
 * then only commands in the list of permitted are permitted. If neither are
 * null, only permitted commands are permitted and the list of denied commands
 * is not used.</p>
 *
 * <p>The test is case in-sensitive.</p>
 */
public class CommandFilter implements Predicate<String> {

    @Nullable
    private final Collection<String> permitted;
    @Nullable
    private final Collection<String> denied;

    /**
     * Create a new instance.
     *
     * @param permitted a list of rules for permitted commands
     * @param denied a list of rules for denied commands
     */
    public CommandFilter(@Nullable Collection<String> permitted, @Nullable Collection<String> denied) {
        this.permitted = permitted;
        this.denied = denied;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean apply(String command) {
        command = command.toLowerCase().replaceAll("^/([^ :]*:)?", "/");

         /*
         * denied      used        allow?
         * x            x           no
         * x            x y         no
         * x y          x           yes
         * x y          x y         no
         *
         * permitted      used        allow?
         * x            x           yes
         * x            x y         yes
         * x y          x           no
         * x y          x y         yes
         */
        String result = "";
        String[] usedParts = command.split("\\s+");
        if (denied != null) {
            denied:
            for (String deniedCommand : denied) {
                String[] deniedParts = deniedCommand.split("\\s+");
                for (int i = 0; i < deniedParts.length && i < usedParts.length; i++) {
                    if (deniedParts[i].equalsIgnoreCase(usedParts[i])) {
                        // first part matches - check if it's the whole thing
                        if (i + 1 == deniedParts.length) {
                            // consumed all denied parts, block entire command
                            result = deniedCommand;
                            break denied;
                        } else {
                            // more denied parts to check, also check used length
                            if (i + 1 == usedParts.length) {
                                // all that was used, but there is more in denied
                                // allow this, check next command in flag
                                continue denied;
                            } else {
                                // more in both denied and used, continue checking
                            }
                        }
                    } else {
                        // found non-matching part, stop checking this command
                        continue denied;
                    }
                }
            }
        }

        if (permitted != null) {
            permitted:
            for (String permittedCommand : permitted) {
                String[] permittedParts = permittedCommand.split("\\s+");
                for (int i = 0; i < permittedParts.length && i < usedParts.length; i++) {
                    if (permittedParts[i].equalsIgnoreCase(usedParts[i])) {
                        // this part matches - check if it's the whole thing
                        if (i + 1 == permittedParts.length) {
                            // consumed all permitted parts before reaching used length
                            // this command is definitely permitted
                            result = "";
                            break permitted;
                        } else {
                            // more permitted parts to check
                            if (i + 1 == usedParts.length) {
                                // all that was used, but there is more in permitted
                                // block for now, check next part of flag
                                result = command;
                                continue permitted;
                            } else {
                                // more in both permitted and used, continue checking for match
                            }
                        }
                    } else {
                        // doesn't match at all, block it, check next flag string
                        result = command;
                        continue permitted;
                    }
                }
            }
        }

        return result.isEmpty();
    }

    /**
     * Builder class for {@code CommandFilter}.
     *
     * <p>If {@link #permit(String...)} is never called, then the
     * permitted rule list will be {@code null}. Likewise if
     * {@link #deny(String...)} is never called.</p>
     */
    public static class Builder {
        private Set<String> permitted;
        private Set<String> denied;

        /**
         * Create a new instance.
         */
        public Builder() {
        }

        /**
         * Permit the given list of commands.
         *
         * @param rules list of commands
         * @return the builder object
         */
        public Builder permit(String ... rules) {
            checkNotNull(rules);
            if (permitted == null) {
                permitted = new HashSet<String>();
            }
            permitted.addAll(Arrays.asList(rules));
            return this;
        }

        /**
         * Deny the given list of commands.
         *
         * @param rules list of commands
         * @return the builder object
         */
        public Builder deny(String ... rules) {
            checkNotNull(rules);
            if (denied == null) {
                denied = new HashSet<String>();
            }
            denied.addAll(Arrays.asList(rules));
            return this;
        }

        /**
         * Create a command filter.
         *
         * @return a new command filter
         */
        public CommandFilter build() {
            return new CommandFilter(
                    permitted != null ? new HashSet<String>(permitted) : null,
                    denied != null ? new HashSet<String>(denied) : null);
        }
    }

}
