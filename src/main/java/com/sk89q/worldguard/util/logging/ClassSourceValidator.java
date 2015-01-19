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

package com.sk89q.worldguard.util.logging;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import java.security.CodeSource;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Validates that certain specified classes came from the same source as
 * a plugin.
 */
public class ClassSourceValidator {

    private static final Logger log = Logger.getLogger(ClassSourceValidator.class.getCanonicalName());
    private static final String separatorLine = Strings.repeat("*", 46);

    private final Plugin plugin;
    @Nullable
    private final CodeSource expectedCodeSource;

    /**
     * Create a new instance.
     *
     * @param plugin The plugin
     */
    public ClassSourceValidator(Plugin plugin) {
        checkNotNull(plugin, "plugin");
        this.plugin = plugin;
        this.expectedCodeSource = plugin.getClass().getProtectionDomain().getCodeSource();
    }

    /**
     * Return a map of classes that been loaded from a different source.
     *
     * @param classes A list of classes to check
     * @return The results
     */
    public Map<Class<?>, CodeSource> findMismatches(List<Class<?>> classes) {
        checkNotNull(classes, "classes");

        Map<Class<?>, CodeSource> mismatches = Maps.newHashMap();

        if (expectedCodeSource != null) {
            for (Class<?> testClass : classes) {
                CodeSource testSource = testClass.getProtectionDomain().getCodeSource();
                if (!expectedCodeSource.equals(testSource)) {
                    mismatches.put(testClass, testSource);
                }
            }
        }

        return mismatches;
    }

    /**
     * Reports classes that have come from a different source.
     *
     * <p>The warning is emitted to the log.</p>
     *
     * @param classes The list of classes to check
     */
    public void reportMismatches(List<Class<?>> classes) {
        Map<Class<?>, CodeSource> mismatches = findMismatches(classes);

        if (!mismatches.isEmpty()) {
            StringBuilder builder = new StringBuilder("\n");

            builder.append(separatorLine).append("\n");
            builder.append("** /!\\    SEVERE WARNING    /!\\\n");
            builder.append("** \n");
            builder.append("** A plugin developer has copied and pasted a portion of \n");
            builder.append("** ").append(plugin.getName()).append(" into their own plugin, so rather than using\n");
            builder.append("** the version of ").append(plugin.getName()).append(" that you downloaded, you\n");
            builder.append("** will be using a broken mix of old ").append(plugin.getName()).append(" (that came\n");
            builder.append("** with the plugin) and your downloaded version. THIS MAY\n");
            builder.append("** SEVERELY BREAK ").append(plugin.getName().toUpperCase()).append(" AND ALL OF ITS FEATURES.\n");
            builder.append("**\n");
            builder.append("** This may have happened because the developer is using\n");
            builder.append("** the ").append(plugin.getName()).append(" API and thinks that including\n");
            builder.append("** ").append(plugin.getName()).append(" is necessary. However, it is not!\n");
            builder.append("**\n");
            builder.append("** Here are some files that have been overridden:\n");
            builder.append("** \n");
            for (Map.Entry<Class<?>, CodeSource> entry : mismatches.entrySet()) {
                CodeSource codeSource = entry.getValue();
                String url = codeSource != null ? codeSource.getLocation().toExternalForm() : "(unknown)";
                builder.append("** '").append(entry.getKey().getSimpleName()).append("' came from '").append(url).append("'\n");
            }
            builder.append("**\n");
            builder.append("** Please report this to the plugins' developers.\n");
            builder.append(separatorLine).append("\n");

            log.log(Level.SEVERE, builder.toString());
        }
    }

}
