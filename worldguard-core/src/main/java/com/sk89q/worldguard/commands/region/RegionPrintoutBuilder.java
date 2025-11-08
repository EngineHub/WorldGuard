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

import com.sk89q.worldguard.protection.flags.registry.UnknownFlag;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.formatting.component.ErrorFormat;
import com.sk89q.worldedit.util.formatting.component.MessageBox;
import com.sk89q.worldedit.util.formatting.component.TextComponentProducer;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.event.ClickEvent;
import com.sk89q.worldedit.util.formatting.text.event.HoverEvent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.internal.permission.RegionPermissionModel;
import com.sk89q.worldguard.protection.FlagValueCalculator;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.RegionGroupFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.Callable;

import javax.annotation.Nullable;

/**
 * Create a region printout, as used in /region info to show information about
 * a region.
 */
public class RegionPrintoutBuilder implements Callable<TextComponent> {

    private final String world;
    private final ProtectedRegion region;
    private final TextComponentProducer builder = new TextComponentProducer();
    private final RegionPermissionModel perms;

    /**
     * Create a new instance with a region to report on.
     *
     * @param world the world name
     * @param region the region
     * @param actor an optional actor to evaluate permissions for
     */
    public RegionPrintoutBuilder(String world, ProtectedRegion region, @Nullable Actor actor) {
        this.world = world;
        this.region = region;
        this.perms = actor != null && actor.isPlayer() ? new RegionPermissionModel(actor) : null;
    }

    /**
     * Add a new line.
     */
    public void newline() {
        builder.append(TextComponent.newline());
    }
    
    /**
     * Add region name, type, and priority.
     */
    public void appendBasics() {
        builder.append(TextComponent.of(message("commands.region.print.region-label"), TextColor.BLUE));
        builder.append(TextComponent.of(region.getId(), TextColor.YELLOW)
                .clickEvent(ClickEvent.of(ClickEvent.Action.RUN_COMMAND, "/rg info -w \"" + world + "\" " + region.getId())));
        
        builder.append(TextComponent.of(message("commands.region.print.type-prefix"), TextColor.GRAY));
        builder.append(TextComponent.of(region.getType().getName()));
        
        builder.append(TextComponent.of(message("commands.region.print.priority-prefix"), TextColor.GRAY));
        appendPriorityComponent(region);
        builder.append(TextComponent.of(message("commands.region.print.paren-close"), TextColor.GRAY));

        newline();
    }

    /**
     * Add information about flags.
     */
    public void appendFlags() {
        builder.append(TextComponent.of(message("commands.region.print.flags-label"), TextColor.BLUE));
        
        appendFlagsList(true);
        
        newline();
    }
    
    /**
     * Append just the list of flags (without "Flags:"), including colors.
     *
     * @param useColors true to use colors
     */
    public void appendFlagsList(boolean useColors) {
        boolean hasFlags = false;
        
        for (Flag<?> flag : WorldGuard.getInstance().getFlagRegistry()) {
            Object val = region.getFlag(flag);

            // No value
            if (val == null) {
                continue;
            }

            if (hasFlags) {
                builder.append(TextComponent.of(message("commands.region.print.flag-separator")));
            }

            RegionGroupFlag groupFlag = flag.getRegionGroupFlag();
            Object group = null;
            if (groupFlag != null) {
                group = region.getFlag(groupFlag);
            }

            String flagString = group == null
                    ? message("commands.region.print.flag-format", flag.getName())
                    : message("commands.region.print.flag-group-format", flag.getName(), group);

            TextColor flagColor = TextColor.WHITE;
            if (useColors) {
                // passthrough is ok on global
                if (FlagHelperBox.DANGER_ZONE.contains(flag)
                        && !(region.getId().equals(ProtectedRegion.GLOBAL_REGION) && flag == Flags.PASSTHROUGH)) {
                    flagColor = TextColor.DARK_RED;
                } else if (Flags.INBUILT_FLAGS.contains(flag.getName())) {
                    flagColor = TextColor.GOLD;
                } else if (flag instanceof UnknownFlag) {
                    flagColor = TextColor.GRAY;
                } else {
                    flagColor = TextColor.LIGHT_PURPLE;
                }
            }
            TextComponent flagText = TextComponent.of(flagString, flagColor)
                    .append(TextComponent.of(String.valueOf(val), useColors ? TextColor.YELLOW : TextColor.WHITE));
            if (perms != null && perms.maySetFlag(region, flag)) {
                flagText = flagText.hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT, TextComponent.of(message("commands.region.print.flag-hover"))))
                        .clickEvent(ClickEvent.of(ClickEvent.Action.SUGGEST_COMMAND,
                                "/rg flag -w \"" + world + "\" " + region.getId() + " " + flag.getName() + " "));
            }
            builder.append(flagText);

            hasFlags = true;
        }

        if (!hasFlags) {
            TextComponent noFlags = TextComponent.of(message("commands.region.print.none"), useColors ? TextColor.RED : TextColor.WHITE);
            builder.append(noFlags);
        }

        if (perms != null && perms.maySetFlag(region)) {
            builder.append(TextComponent.space())
                    .append(TextComponent.of(message("commands.region.print.flags-button"), useColors ? TextColor.GREEN : TextColor.GRAY)
                    .hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT, TextComponent.of(message("commands.region.print.flags-button-hover"))))
                    .clickEvent(ClickEvent.of(ClickEvent.Action.RUN_COMMAND, "/rg flags -w \"" + world + "\" " + region.getId())));
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

        ProtectedRegion last = null;
        int indent = 0;
        while (it.hasPrevious()) {
            ProtectedRegion cur = it.previous();

            StringBuilder namePrefix = new StringBuilder();
            
            // Put symbol for child
            if (indent != 0) {
                for (int i = 0; i < indent; i++) {
                    namePrefix.append(" ");
                }
                namePrefix.append(message("commands.region.print.parent-arrow"));
            }

            // Put name
            builder.append(TextComponent.of(namePrefix.toString(), useColors ? TextColor.GREEN : TextColor.WHITE));
            if (perms != null && perms.mayLookup(cur)) {
                builder.append(TextComponent.of(cur.getId(), useColors ? TextColor.GREEN : TextColor.WHITE)
                    .hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT, TextComponent.of(message("commands.region.print.parent-info-hover"))))
                    .clickEvent(ClickEvent.of(ClickEvent.Action.RUN_COMMAND, "/rg info -w \"" + world + "\" " + cur.getId())));
            } else {
                builder.append(TextComponent.of(cur.getId(), useColors ? TextColor.GREEN : TextColor.WHITE));
            }
            
            // Put (parent)
            if (!cur.equals(region)) {
                builder.append(TextComponent.of(message("commands.region.print.parent-info-prefix"), useColors ? TextColor.GRAY : TextColor.WHITE));
                appendPriorityComponent(cur);
                builder.append(TextComponent.of(message("commands.region.print.paren-close"), useColors ? TextColor.GRAY : TextColor.WHITE));
            }
            if (last != null && cur.equals(region) && perms != null && perms.maySetParent(cur, last)) {
                builder.append(TextComponent.space());
                builder.append(TextComponent.of(message("commands.region.print.parent-unlink"), TextColor.RED)
                        .hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT, TextComponent.of(message("commands.region.print.parent-unlink-hover"))))
                        .clickEvent(ClickEvent.of(ClickEvent.Action.SUGGEST_COMMAND, "/rg setparent -w \"" + world + "\" " + cur.getId())));
            }

            last = cur;
            indent++;
            newline();
        }
    }

    /**
     * Add information about members.
     */
    public void appendDomain() {
        builder.append(TextComponent.of(message("commands.region.print.owners-label"), TextColor.BLUE));
        addDomainString(region.getOwners(),
                perms != null && perms.mayAddOwners(region) ? "addowner" : null,
                perms != null && perms.mayRemoveOwners(region) ? "removeowner" : null);
        newline();

        builder.append(TextComponent.of(message("commands.region.print.members-label"), TextColor.BLUE));
        addDomainString(region.getMembers(),
                perms != null && perms.mayAddMembers(region) ? "addmember" : null,
                perms != null && perms.mayRemoveMembers(region) ? "removemember" : null);
        newline();
    }

    private void addDomainString(DefaultDomain domain, String addCommand, String removeCommand) {
        if (domain.size() == 0) {
            builder.append(ErrorFormat.wrap(message("commands.region.print.none")));
        } else {
            List<String> segments = new ArrayList<>();
            if (!domain.getPlayers().isEmpty()) {
                segments.add(String.join(", ", new TreeSet<>(domain.getPlayers())));
            }
            Set<UUID> uuidSet = domain.getUniqueIds();
            if (!uuidSet.isEmpty()) {
                segments.add(uuidSet.stream().map(UUID::toString).collect(java.util.stream.Collectors.joining(", ")));
            }
            if (!domain.getGroups().isEmpty()) {
                segments.add(domain.getGroups().stream().map(group -> "g:" + group).collect(java.util.stream.Collectors.joining(", ")));
            }
            String display = String.join("; ", segments);
            builder.append(TextComponent.of(display, perms != null ? TextColor.YELLOW : TextColor.GRAY));
        }
        if (addCommand != null) {
            builder.append(TextComponent.space().append(TextComponent.of(message("commands.region.print.add-label"), TextColor.GREEN)
                            .hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT, TextComponent.of(message("commands.region.print.add-hover"))))
                            .clickEvent(ClickEvent.of(ClickEvent.Action.SUGGEST_COMMAND,
                                    "/rg " + addCommand + " -w \"" + world + "\" " + region.getId() + " "))));
        }
        if (removeCommand != null && domain.size() > 0) {
            builder.append(TextComponent.space().append(TextComponent.of(message("commands.region.print.remove-label"), TextColor.RED)
                    .hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT, TextComponent.of(message("commands.region.print.remove-hover"))))
                    .clickEvent(ClickEvent.of(ClickEvent.Action.SUGGEST_COMMAND,
                            "/rg " + removeCommand + " -w \"" + world + "\" " + region.getId() + " "))));
            builder.append(TextComponent.space().append(TextComponent.of(message("commands.region.print.clear-label"), TextColor.RED)
                    .hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT, TextComponent.of(message("commands.region.print.clear-hover"))))
                    .clickEvent(ClickEvent.of(ClickEvent.Action.SUGGEST_COMMAND,
                            "/rg " + removeCommand + " -w \"" + world + "\" -a " + region.getId()))));
        }
    }

    /**
     * Add information about coordinates.
     */
    public void appendBounds() {
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();
        builder.append(TextComponent.of(message("commands.region.print.bounds-label"), TextColor.BLUE));
        TextComponent bound = TextComponent.of(" " + min + " -> " + max, TextColor.YELLOW);
        if (perms != null && perms.maySelect(region)) {
            bound = bound
                    .hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT, TextComponent.of(message("commands.region.print.select-hover"))))
                    .clickEvent(ClickEvent.of(ClickEvent.Action.RUN_COMMAND, "/rg select " + region.getId()));
        }
        builder.append(bound);
        final Location teleFlag = FlagValueCalculator.getEffectiveFlagOf(region, Flags.TELE_LOC, perms != null && perms.getSender() instanceof RegionAssociable ? (RegionAssociable) perms.getSender() : null);
        if (teleFlag != null && perms != null && perms.mayTeleportTo(region)) {
            builder.append(TextComponent.space().append(TextComponent.of(message("commands.region.print.teleport-label"), TextColor.GRAY)
                    .hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT,
                            TextComponent.of(message("commands.region.print.teleport-hover")).append(TextComponent.newline()).append(
                                    TextComponent.of(teleFlag.getBlockX() + ", "
                                            + teleFlag.getBlockY() + ", "
                                            + teleFlag.getBlockZ()))))
                    .clickEvent(ClickEvent.of(ClickEvent.Action.RUN_COMMAND,
                            "/rg tp -w \"" + world + "\" " + region.getId()))));
        } else if (perms != null && perms.mayTeleportToCenter(region) && region.isPhysicalArea()) {
            builder.append(TextComponent.space().append(TextComponent.of(message("commands.region.print.center-teleport-label"), TextColor.GRAY)
                    .hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT,
                            TextComponent.of(message("commands.region.print.center-teleport-hover"))))
                    .clickEvent(ClickEvent.of(ClickEvent.Action.RUN_COMMAND,
                            "/rg tp -c -w \"" + world + "\" " + region.getId()))));
        }

        newline();
    }

    private void appendPriorityComponent(ProtectedRegion rg) {
        final String content = String.valueOf(rg.getPriority());
        if (perms != null && perms.maySetPriority(rg)) {
            builder.append(TextComponent.of(content, TextColor.GOLD)
                    .hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT, TextComponent.of(message("commands.region.print.priority-hover"))))
                    .clickEvent(ClickEvent.of(ClickEvent.Action.SUGGEST_COMMAND, "/rg setpriority -w \"" + world + "\" " + rg.getId() + " ")));
        } else {
            builder.append(TextComponent.of(content, TextColor.WHITE));
        }
    }

    private void appendRegionInformation() {
        appendBasics();
        appendFlags();
        appendParents();
        appendDomain();
        appendBounds();

    }

    @Override
    public TextComponent call() {
        MessageBox box = new MessageBox(message("commands.region.print.title"), builder);
        appendRegionInformation();
        return box.create();
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

    private String message(String key, Object... arguments) {
        return WorldGuard.getInstance().getLocalization().format(key, arguments);
    }

}
