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

package com.sk89q.worldguard.commands.region;

import com.sk89q.squirrelid.cache.ProfileCache;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.formatting.component.Error;
import com.sk89q.worldedit.util.formatting.component.Label;
import com.sk89q.worldedit.util.formatting.component.Subtle;
import com.sk89q.worldedit.util.formatting.component.TextComponentProducer;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.RegionGroupFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Callable;

import javax.annotation.Nullable;

/**
 * Create a region printout, as used in /region info to show information about
 * a region.
 */
public class RegionPrintoutBuilder implements Callable<TextComponent> {
    
    private final ProtectedRegion region;
    @Nullable
    private final ProfileCache cache;
    private final TextComponentProducer builder = new TextComponentProducer();

    /**
     * Create a new instance with a region to report on.
     *
     * @param region the region
     * @param cache a profile cache, or {@code null}
     */
    public RegionPrintoutBuilder(ProtectedRegion region, @Nullable ProfileCache cache) {
        this.region = region;
        this.cache = cache;
    }

    /**
     * Add a new line.
     */
    private void newLine() {
        builder.append(Component.newline());
    }
    
    /**
     * Add region name, type, and priority.
     */
    public void appendBasics() {
        builder.append(TextComponent.of("Region: ", TextColor.BLUE));
        builder.append(TextComponent.of(region.getId(), TextColor.YELLOW));
        
        builder.append(TextComponent.of(" (type=", TextColor.GRAY));
        builder.append(TextComponent.of(region.getType().getName()));
        
        builder.append(TextComponent.of(", priority=", TextColor.GRAY));
        builder.append(TextComponent.of(String.valueOf(region.getPriority())));
        builder.append(TextComponent.of(")", TextColor.GRAY));

        newLine();
    }
    
    /**
     * Add information about flags.
     */
    public void appendFlags() {
        builder.append(TextComponent.of("Flags: ", TextColor.BLUE));
        
        appendFlagsList(true);
        
        newLine();
    }
    
    /**
     * Append just the list of flags (without "Flags:"), including colors.
     *
     * @param useColors true to use colors
     */
    public void appendFlagsList(boolean useColors) {
        boolean hasFlags = false;
        
        for (Flag<?> flag : WorldGuard.getInstance().getFlagRegistry()) {
            Object val = region.getFlag(flag), group = null;
            
            // No value
            if (val == null) {
                continue;
            }

            if (hasFlags) {
                builder.append(TextComponent.of(", "));
            }

            RegionGroupFlag groupFlag = flag.getRegionGroupFlag();
            if (groupFlag != null) {
                group = region.getFlag(groupFlag);
            }

            String flagString;

            if (group == null) {
                flagString = flag.getName() + ": " + val;
            } else {
                flagString = flag.getName() + " -g "+ group + ": " + val;
            }

            builder.append(TextComponent.of(flagString, !(hasFlags && useColors) ? TextColor.YELLOW : TextColor.WHITE));

            hasFlags = true;
        }
            
        if (!hasFlags) {
            builder.append(TextComponent.of("(none)", useColors ? TextColor.RED : TextColor.WHITE));
        }
    }
    
    /**
     * Add information about parents.
     */
    public void appendParents() {
        appendParentTree(true);
    }
    
    /**
     * Add information about parents.
     * 
     * @param useColors true to use colors
     */
    public void appendParentTree(boolean useColors) {
        if (region.getParent() == null) {
            return;
        }
        
        List<ProtectedRegion> inheritance = new ArrayList<>();

        ProtectedRegion r = region;
        inheritance.add(r);
        while (r.getParent() != null) {
            r = r.getParent();
            inheritance.add(r);
        }

        ListIterator<ProtectedRegion> it = inheritance.listIterator(
                inheritance.size());

        int indent = 0;
        while (it.hasPrevious()) {
            ProtectedRegion cur = it.previous();

            StringBuilder nameString = new StringBuilder();
            
            // Put symbol for child
            if (indent != 0) {
                for (int i = 0; i < indent; i++) {
                    nameString.append("  ");
                }
                nameString.append("\u2517");
            }
            
            // Put name
            nameString.append(cur.getId());
            builder.append(TextComponent.of(nameString.toString(), useColors ? TextColor.GREEN : TextColor.WHITE));
            
            // Put (parent)
            if (!cur.equals(region)) {
                builder.append(TextComponent.of(" (parent, priority=" + cur.getPriority() + ")", useColors ? TextColor.GRAY : TextColor.WHITE));
            }
            
            indent++;
            newLine();
        }
    }
    
    /**
     * Add information about members.
     */
    public void appendDomain() {
        builder.append(TextComponent.of("Owners: ", TextColor.BLUE));
        addDomainString(region.getOwners());
        newLine();

        builder.append(TextComponent.of("Members: ", TextColor.BLUE));
        addDomainString(region.getMembers());
        newLine();
    }

    private void addDomainString(DefaultDomain domain) {
        if (domain.size() != 0) {
            builder.append(new Label(domain.toUserFriendlyString(cache)).create());
        } else {
            builder.append(new Error("(none)").create());
        }
    }
    
    /**
     * Add information about coordinates.
     */
    public void appendBounds() {
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();
        builder.append(TextComponent.of("Bounds:", TextColor.BLUE));
        builder.append(TextComponent.of(" (" + min.getBlockX() + "," + min.getBlockY() + "," + min.getBlockZ() + ")"
                + " -> (" + max.getBlockX() + "," + max.getBlockY() + "," + max.getBlockZ() + ")", TextColor.YELLOW));

        newLine();
    }

    private void appendRegionInformation() {
        builder.append(new Subtle("\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550").create());
        builder.append(new Subtle(" Region Info ").create());
        builder.append(new Subtle("\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550").create());
        newLine();
        appendBasics();
        appendFlags();
        appendParents();
        appendDomain();
        appendBounds();

        if (cache != null) {
            builder.append(new Subtle("Any names suffixed by * are 'last seen names' and may not be up to date.").create());
            newLine();
        }
    }

    @Override
    public TextComponent call() throws Exception {
        appendRegionInformation();
        return toComponent();
    }

    /**
     * Send the report to a {@link Actor}.
     *
     * @param sender the recipient
     */
    public void send(Actor sender) {
        sender.print(toComponent());
    }

    public TextComponentProducer append(String str) {
        return builder.append(TextComponent.of(str));
    }

    public TextComponentProducer append(TextComponent component) {
        return builder.append(component);
    }

    public TextComponent toComponent() {
        return builder.create();
    }

    @Override
    public String toString() {
        return builder.toString().trim();
    }

}
