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

package com.sk89q.worldguard.bukkit.commands.region;

import com.sk89q.squirrelid.cache.ProfileCache;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.RegionGroupFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Callable;

/**
 * Create a region printout, as used in /region info to show information about
 * a region.
 */
public class RegionPrintoutBuilder implements Callable<String> {
    
    private final ProtectedRegion region;
    @Nullable
    private final ProfileCache cache;
    private final StringBuilder builder = new StringBuilder();

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
        builder.append("\n");
    }
    
    /**
     * Add region name, type, and priority.
     */
    public void appendBasics() {
        builder.append(ChatColor.BLUE);
        builder.append("Region: ");
        builder.append(ChatColor.YELLOW);
        builder.append(region.getId());
        
        builder.append(ChatColor.GRAY);
        builder.append(" (type=");
        builder.append(region.getType().getName());
        
        builder.append(ChatColor.GRAY);
        builder.append(", priority=");
        builder.append(region.getPriority());
        builder.append(")");

        newLine();
    }
    
    /**
     * Add information about flags.
     */
    public void appendFlags() {
        builder.append(ChatColor.BLUE);
        builder.append("Flags: ");
        
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
        
        for (Flag<?> flag : DefaultFlag.getFlags()) {
            Object val = region.getFlag(flag), group = null;
            
            // No value
            if (val == null) {
                continue;
            }

            if (hasFlags) {
                builder.append(", ");
            } else {
                if (useColors) {
                    builder.append(ChatColor.YELLOW);
                }
            }

            RegionGroupFlag groupFlag = flag.getRegionGroupFlag();
            if (groupFlag != null) {
                group = region.getFlag(groupFlag);
            }

            if (group == null) {
                builder.append(flag.getName()).append(": ")
                        .append(ChatColor.stripColor(String.valueOf(val)));
            } else {
                builder.append(flag.getName()).append(" -g ")
                        .append(group).append(": ")
                        .append(ChatColor.stripColor(String.valueOf(val)));
            }

            hasFlags = true;
        }
            
        if (!hasFlags) {
            if (useColors) {
                builder.append(ChatColor.RED);
            }
            builder.append("(none)");
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
        
        List<ProtectedRegion> inheritance = new ArrayList<ProtectedRegion>();

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
            if (useColors) {
                builder.append(ChatColor.GREEN);
            }
            
            // Put symbol for child
            if (indent != 0) {
                for (int i = 0; i < indent; i++) {
                    builder.append("  ");
                }
                builder.append("\u2517");
            }
            
            // Put name
            builder.append(cur.getId());
            
            // Put (parent)
            if (!cur.equals(region)) {
                if (useColors) {
                    builder.append(ChatColor.GRAY);
                }
                builder.append(" (parent, priority=").append(cur.getPriority()).append(")");
            }
            
            indent++;
            newLine();
        }
    }
    
    /**
     * Add information about members.
     */
    public void appendDomain() {
        builder.append(ChatColor.BLUE);
        builder.append("Owners: ");
        addDomainString(region.getOwners());
        newLine();

        builder.append(ChatColor.BLUE);
        builder.append("Members: ");
        addDomainString(region.getMembers());
        newLine();
    }

    private void addDomainString(DefaultDomain domain) {
        if (domain.size() != 0) {
            builder.append(ChatColor.YELLOW);
            builder.append(domain.toUserFriendlyString(cache));
        } else {
            builder.append(ChatColor.RED);
            builder.append("(none)");
        }
    }
    
    /**
     * Add information about coordinates.
     */
    public void appendBounds() {
        BlockVector min = region.getMinimumPoint();
        BlockVector max = region.getMaximumPoint();
        builder.append(ChatColor.BLUE);
        builder.append("Bounds:");
        builder.append(ChatColor.YELLOW);
        builder.append(" (").append(min.getBlockX()).append(",").append(min.getBlockY()).append(",").append(min.getBlockZ()).append(")");
        builder.append(" -> (").append(max.getBlockX()).append(",").append(max.getBlockY()).append(",").append(max.getBlockZ()).append(")");
        
        newLine();
    }

    private void appendRegionInformation() {
        builder.append(ChatColor.GRAY);
        builder.append("\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550");
        builder.append(" Region Info ");
        builder.append("\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550");
        newLine();
        appendBasics();
        appendFlags();
        appendParents();
        appendDomain();
        appendBounds();

        if (cache != null) {
            builder.append(ChatColor.GRAY).append("Any names suffixed by * are 'last seen names' and may not be up to date.");
            newLine();
        }
    }

    @Override
    public String call() throws Exception {
        appendRegionInformation();
        return builder.toString();
    }

    /**
     * Send the report to a {@link CommandSender}.
     *
     * @param sender the recipient
     */
    public void send(CommandSender sender) {
        sender.sendMessage(toString());
    }

    public StringBuilder append(boolean b) {
        return builder.append(b);
    }

    public StringBuilder append(char c) {
        return builder.append(c);
    }

    public StringBuilder append(char[] str, int offset, int len) {
        return builder.append(str, offset, len);
    }

    public StringBuilder append(char[] str) {
        return builder.append(str);
    }

    public StringBuilder append(CharSequence s, int start, int end) {
        return builder.append(s, start, end);
    }

    public StringBuilder append(CharSequence s) {
        return builder.append(s);
    }

    public StringBuilder append(double d) {
        return builder.append(d);
    }

    public StringBuilder append(float f) {
        return builder.append(f);
    }

    public StringBuilder append(int i) {
        return builder.append(i);
    }

    public StringBuilder append(long lng) {
        return builder.append(lng);
    }

    public StringBuilder append(Object obj) {
        return builder.append(obj);
    }

    public StringBuilder append(String str) {
        return builder.append(str);
    }

    public StringBuilder append(StringBuffer sb) {
        return builder.append(sb);
    }

    public StringBuilder appendCodePoint(int codePoint) {
        return builder.appendCodePoint(codePoint);
    }
    
    @Override
    public String toString() {
        return builder.toString().trim();
    }

}
