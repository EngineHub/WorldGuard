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

package com.sk89q.worldguard.commands.task;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.squirrelid.Profile;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.event.ClickEvent;
import com.sk89q.worldedit.util.formatting.text.event.HoverEvent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.internal.permission.RegionPermissionModel;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RegionLister implements Callable<Integer> {

    private static final Logger log = Logger.getLogger(RegionLister.class.getCanonicalName());

    private final Actor sender;
    private final RegionManager manager;
    private final String world;
    private OwnerMatcher ownerMatcher;
    private int page;

    public RegionLister(RegionManager manager, Actor sender, String world) {
        checkNotNull(manager);
        checkNotNull(sender);
        checkNotNull(world);

        this.manager = manager;
        this.sender = sender;
        this.world = world;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void filterOwnedByPlayer(final Player player) {
        ownerMatcher = new OwnerMatcher() {
            @Override
            public String getName() {
                return player.getName();
            }

            @Override
            public boolean isContainedWithin(DefaultDomain domain) throws CommandException {
                return domain.contains(player.getUniqueId());
            }
        };
    }

    public void filterOwnedByName(String name, boolean nameOnly) {
        if (nameOnly) {
            filterOwnedByName(name);
        } else {
            filterOwnedByProfile(name);
        }
    }

    private void filterOwnedByName(final String name) {
        ownerMatcher = new OwnerMatcher() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public boolean isContainedWithin(DefaultDomain domain) throws CommandException {
                return domain.contains(name);
            }
        };
    }

    private void filterOwnedByProfile(final String name) {
        ownerMatcher = new OwnerMatcher() {
            private UUID uniqueId;

            @Override
            public String getName() {
                return name;
            }

            @Override
            public boolean isContainedWithin(DefaultDomain domain) throws CommandException {
                if (domain.contains(name)) {
                    return true;
                }

                if (uniqueId == null) {
                    Profile profile;

                    try {
                        profile = WorldGuard.getInstance().getProfileService().findByName(name);
                    } catch (IOException e) {
                        log.log(Level.WARNING, "Failed UUID lookup of '" + name + "'", e);
                        throw new CommandException("Failed to lookup the UUID of '" + name + "'");
                    } catch (InterruptedException e) {
                        log.log(Level.WARNING, "Failed UUID lookup of '" + name + "'", e);
                        throw new CommandException("The lookup the UUID of '" + name + "' was interrupted");
                    }

                    if (profile == null) {
                        throw new CommandException("A user by the name of '" + name + "' does not seem to exist.");
                    }

                    uniqueId = profile.getUniqueId();
                }

                return domain.contains(uniqueId);
            }
        };
    }

    @Override
    public Integer call() throws Exception {
        Map<String, ProtectedRegion> regions = manager.getRegions();

        // Build a list of regions to show
        List<RegionListEntry> entries = new ArrayList<>();

        int index = 0;
        for (Map.Entry<String, ProtectedRegion> rg : regions.entrySet()) {
            RegionListEntry entry = new RegionListEntry(rg.getValue(), index++);

            // Filtering by owner?
            if (ownerMatcher != null) {
                ProtectedRegion region = rg.getValue();
                entry.isOwner = ownerMatcher.isContainedWithin(region.getOwners());
                entry.isMember = ownerMatcher.isContainedWithin(region.getMembers());

                if (!entry.isOwner && !entry.isMember) {
                    continue; // Skip
                }
            }

            entries.add(entry);
        }

        Collections.sort(entries);

        final int totalSize = entries.size();
        final int pageSize = 10;
        final int pages = (int) Math.ceil(totalSize / (float) pageSize);

        sender.print((ownerMatcher == null ? "Regions (page " : "Regions for " + ownerMatcher.getName() + " (page ")
                + (page + 1) + " of " + pages + "):");
        RegionPermissionModel perms = new RegionPermissionModel(sender);

        if (page < pages) {
            // Print
            for (int i = page * pageSize; i < page * pageSize + pageSize; i++) {
                if (i >= totalSize) {
                    break;
                }

                final RegionListEntry entry = entries.get(i);
                final TextComponent.Builder builder = TextComponent.builder(entry.toString());
                if (perms.mayLookup(entry.region)) {
                    builder.append(Component.space().append(TextComponent.of("[Info]", TextColor.GRAY)
                            .hoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.of("Click for info")))
                            .clickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                    "/rg info -w " + world + " " + entry.region.getId()))));
                }
                if (perms.mayTeleportTo(entry.region) && entry.region.getFlag(Flags.TELE_LOC) != null) {
                    builder.append(Component.space().append(TextComponent.of("[Teleport]", TextColor.GRAY)
                            .hoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.of("Click to teleport")))
                            .clickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                    "/rg tp -w " + world + " " + entry.region.getId()))));
                }
                sender.print(builder.build());
            }
        }

        return page;
    }

    private interface OwnerMatcher {
        String getName();

        boolean isContainedWithin(DefaultDomain domain) throws CommandException;
    }

    private class RegionListEntry implements Comparable<RegionListEntry> {
        private final ProtectedRegion region;
        private final int index;
        boolean isOwner;
        boolean isMember;

        private RegionListEntry(ProtectedRegion rg, int index) {
            this.region = rg;
            this.index = index;
        }

        @Override
        public int compareTo(RegionListEntry o) {
            if (isOwner != o.isOwner) {
                return isOwner ? 1 : -1;
            }
            if (isMember != o.isMember) {
                return isMember ? 1 : -1;
            }
            return region.getId().compareTo(o.region.getId());
        }

        @Override
        public String toString() {
            if (isOwner) {
                return (index + 1) + ". +" + region.getId();
            } else if (isMember) {
                return (index + 1) + ". -" + region.getId();
            } else {
                return (index + 1) + ". " + region.getId();
            }
        }
    }
}
