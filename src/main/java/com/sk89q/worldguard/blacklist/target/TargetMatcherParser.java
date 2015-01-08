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

package com.sk89q.worldguard.blacklist.target;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.sk89q.guavabackport.collect.Range;
import com.sk89q.worldedit.blocks.ItemType;
import com.sk89q.worldguard.util.Enums;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TargetMatcherParser {

    private static final Pattern DATA_VALUE_PATTERN = Pattern.compile("^([^:]+):([^:]+)$");
    private static final Pattern LESS_THAN_PATTERN = Pattern.compile("^<=\\s*([0-9]+)$");
    private static final Pattern GREATER_THAN_PATTERN = Pattern.compile("^>=\\s*([0-9]+)$");
    private static final Pattern RANGE_PATTERN = Pattern.compile("^([0-9]+)\\s*-\\s*([0-9]+)$");

    public TargetMatcher fromInput(String input) throws TargetMatcherParseException {
        Matcher matcher = DATA_VALUE_PATTERN.matcher(input.trim());
        if (matcher.matches()) {
            return new DataValueRangeMatcher(parseType(matcher.group(1)), parseDataValueRanges(matcher.group(2)));
        } else {
            return new WildcardDataMatcher(parseType(input));
        }
    }

    private int parseType(String input) throws TargetMatcherParseException {
        input = input.trim();

        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            int id = getItemID(input);
            if (id > 0) {
                return id;
            }
            
            Material material = Enums.findFuzzyByValue(Material.class, input);
            if (material != null) {
                return material.getId();
            }

            throw new TargetMatcherParseException("Unknown block or item name: " + input);
        }
    }

    private Predicate<Short> parseDataValueRanges(String input) throws TargetMatcherParseException {
        List<Predicate<Short>> predicates = new ArrayList<Predicate<Short>>();

        for (String part : input.split(";")) {
            predicates.add(parseRange(part));
        }

        return Predicates.or(predicates);
    }

    private Predicate<Short> parseRange(String input) throws TargetMatcherParseException {
        input = input.trim();

        Matcher matcher;

        matcher = LESS_THAN_PATTERN.matcher(input);
        if (matcher.matches()) {
            return Range.atMost(Short.parseShort(matcher.group(1)));
        }

        matcher = GREATER_THAN_PATTERN.matcher(input);
        if (matcher.matches()) {
            return Range.atLeast(Short.parseShort(matcher.group(1)));
        }

        matcher = RANGE_PATTERN.matcher(input);
        if (matcher.matches()) {
            return Range.closed(Short.parseShort(matcher.group(1)), Short.parseShort(matcher.group(2)));
        }

        try {
            short s = Short.parseShort(input);
            return Range.closed(s, s);
        } catch (NumberFormatException e) {
            throw new TargetMatcherParseException("Unknown data value range: " + input);
        }
    }

    /**
     * Get an item's ID from its name.
     *
     * @param name the name of the item to look up
     * @return the id for name if contained in ItemId, else -1
     */
    private static int getItemID(String name) {
        ItemType type = ItemType.lookup(name);
        if (type != null) {
            return type.getID();
        } else {
            return -1;
        }
    }

}
