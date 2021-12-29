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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.event.ClickEvent;
import com.sk89q.worldedit.util.formatting.text.event.HoverEvent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.util.ChangeTracked;
import com.sk89q.worldguard.util.profile.Profile;
import com.sk89q.worldguard.util.profile.cache.ProfileCache;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A combination of a {@link PlayerDomain} and a {@link GroupDomain}.
 */
public class DefaultDomain implements Domain, ChangeTracked {

    private PlayerDomain playerDomain = new PlayerDomain();
    private GroupDomain groupDomain = new GroupDomain();

    private Set<CustomDomain> customDomains = new HashSet<>();
    private boolean customDomainsChanged = false;

    /**
     * Create a new domain.
     */
    public DefaultDomain() {
    }

    /**
     * Create a new domain from an existing one, making a copy of all values.
     *
     * @param existing the other domain to copy values from
     */
    public DefaultDomain(DefaultDomain existing) {
        setPlayerDomain(existing.getPlayerDomain());
        setGroupDomain(existing.getGroupDomain());
        setCustomDomains(existing.getCustomDomains());
    }

    /**
     * Get the domain that holds the players.
     *
     * @return a domain
     */
    public PlayerDomain getPlayerDomain() {
        return playerDomain;
    }

    /**
     * Set a new player domain.
     *
     * @param playerDomain a domain
     */
    public void setPlayerDomain(PlayerDomain playerDomain) {
        checkNotNull(playerDomain);
        this.playerDomain = new PlayerDomain(playerDomain);
    }

    /**
     * Set the domain that holds the groups.
     *
     * @return a domain
     */
    public GroupDomain getGroupDomain() {
        return groupDomain;
    }

    /**
     * Set a new group domain.
     *
     * @param groupDomain a domain
     */
    public void setGroupDomain(GroupDomain groupDomain) {
        checkNotNull(groupDomain);
        this.groupDomain = new GroupDomain(groupDomain);
    }

    /**
     * Add new custom domains
     *
     * @param customDomain a domain
     */
    public void addCustomDomain(CustomDomain customDomain) {
        checkNotNull(customDomain);
        removeCustomDomain(customDomain.getName());
        this.customDomains.add(customDomain);
        customDomainsChanged = true;
    }

    /**
     * Remove a custom domain matched by the name
     *
     * @param name the name
     */
    public void removeCustomDomain(String name) {
        checkNotNull(name);
        if (this.customDomains.removeIf(d -> d.getName().equalsIgnoreCase(name))) {
            customDomainsChanged = true;
        }
    }

    /**
     * Remove a custom domain
     *
     * @param customDomain a domain
     */
    public void removeCustomDomain(CustomDomain customDomain) {
        checkNotNull(customDomain);
        if (this.customDomains.remove(customDomain)) {
            customDomainsChanged = true;
        }
    }

    /**
     * Set the api domains to a specified value
     *
     * @param customDomains the domains
     */
    public void setCustomDomains(Collection<CustomDomain> customDomains) {
        checkNotNull(customDomains);
        this.customDomains = new HashSet<>(customDomains);
        customDomainsChanged = true;
    }

    /**
     * Get all api domains
     *
     * @return a unmodifiable copy of the domains
     */
    public Set<CustomDomain> getCustomDomains() {
        return Collections.unmodifiableSet(this.customDomains);
    }

    /**
     * Add the given player to the domain, identified by the player's name.
     *
     * @param name the name of the player
     */
    public void addPlayer(String name) {
        playerDomain.addPlayer(name);
    }

    /**
     * Remove the given player from the domain, identified by the player's name.
     *
     * @param name the name of the player
     */
    public void removePlayer(String name) {
        playerDomain.removePlayer(name);
    }

    /**
     * Remove the given player from the domain, identified by the player's UUID.
     *
     * @param uuid the UUID of the player
     */
    public void removePlayer(UUID uuid) {
        playerDomain.removePlayer(uuid);
    }

    /**
     * Add the given player to the domain, identified by the player's UUID.
     *
     * @param uniqueId the UUID of the player
     */
    public void addPlayer(UUID uniqueId) {
        playerDomain.addPlayer(uniqueId);
    }

    /**
     * Remove the given player from the domain, identified by either the
     * player's name, the player's unique ID, or both.
     *
     * @param player the player
     */
    public void removePlayer(LocalPlayer player) {
        playerDomain.removePlayer(player);
    }

    /**
     * Add the given player to the domain, identified by the player's UUID.
     *
     * @param player the player
     */
    public void addPlayer(LocalPlayer player) {
        playerDomain.addPlayer(player);
    }

    /**
     * Add all the entries from another domain.
     *
     * @param other the other domain
     */
    public void addAll(DefaultDomain other) {
        checkNotNull(other);
        for (String player : other.getPlayers()) {
            addPlayer(player);
        }
        for (UUID uuid : other.getUniqueIds()) {
            addPlayer(uuid);
        }
        for (String group : other.getGroups()) {
            addGroup(group);
        }
        for (CustomDomain domain : other.getCustomDomains()) {
            addCustomDomain(domain);
        }
    }

    /**
     * Remove all the entries from another domain.
     *
     * @param other the other domain
     */
    public void removeAll(DefaultDomain other) {
        checkNotNull(other);
        for (String player : other.getPlayers()) {
            removePlayer(player);
        }
        for (UUID uuid : other.getUniqueIds()) {
            removePlayer(uuid);
        }
        for (String group : other.getGroups()) {
            removeGroup(group);
        }
        for (CustomDomain domain : other.getCustomDomains()) {
            removeCustomDomain(domain.getName());
        }
    }

    /**
     * Get the set of player names.
     *
     * @return the set of player names
     */
    public Set<String> getPlayers() {
        return playerDomain.getPlayers();
    }

    /**
     * Get the set of player UUIDs.
     *
     * @return the set of player UUIDs
     */
    public Set<UUID> getUniqueIds() {
        return playerDomain.getUniqueIds();
    }

    /**
     * Add the name of the group to the domain.
     *
     * @param name the name of the group.
     */
    public void addGroup(String name) {
        groupDomain.addGroup(name);
    }

    /**
     * Remove the given group from the domain.
     *
     * @param name the name of the group
     */
    public void removeGroup(String name) {
        groupDomain.removeGroup(name);
    }

    /**
     * Get the set of group names.
     *
     * @return the set of group names
     */
    public Set<String> getGroups() {
        return groupDomain.getGroups();
    }

    @Override
    public boolean contains(LocalPlayer player) {
        return playerDomain.contains(player) || groupDomain.contains(player) || customDomains.stream().anyMatch(d -> d.contains(player));
    }

    @Override
    public boolean contains(UUID uniqueId) {
        return playerDomain.contains(uniqueId) || customDomains.stream().anyMatch(d -> d.contains(uniqueId));
    }

    @Override
    public boolean contains(String playerName) {
        return playerDomain.contains(playerName);
    }

    @Override
    public int size() {
        return groupDomain.size() + playerDomain.size() + customDomains.size();
    }

    @Override
    public void clear() {
        playerDomain.clear();
        groupDomain.clear();
    }

    public void removeAll() {
        clear();
    }

    public String toPlayersString() {
        return toPlayersString(null);
    }

    public String toPlayersString(@Nullable ProfileCache cache) {
        List<String> output = new ArrayList<>();

        for (String name : playerDomain.getPlayers()) {
            output.add("name:" + name);
        }

        if (cache != null) {
            ImmutableMap<UUID, Profile> results = cache.getAllPresent(playerDomain.getUniqueIds());
            for (UUID uuid : playerDomain.getUniqueIds()) {
                Profile profile = results.get(uuid);
                if (profile != null) {
                    output.add(profile.getName() + "*");
                } else {
                    output.add("uuid:" + uuid);
                }
            }
        } else {
            for (UUID uuid : playerDomain.getUniqueIds()) {
                output.add("uuid:" + uuid);
            }
        }

        output.sort(String.CASE_INSENSITIVE_ORDER);
        return String.join(", ", output);
    }
    
    public String toGroupsString() {
        StringBuilder str = new StringBuilder();
        for (Iterator<String> it = groupDomain.getGroups().iterator(); it.hasNext(); ) {
            str.append("g:");
            str.append(it.next());
            if (it.hasNext()) {
                str.append(", ");
            }
        }
        return str.toString();
    }

    public String toCustomDomainsString() {
        List<String> output = new ArrayList<>();
        for (CustomDomain customDomain : customDomains) {
            output.add(customDomain.getName() + ":" + customDomain.toString());
        }
        output.sort(String.CASE_INSENSITIVE_ORDER);
        return String.join(", ", output);
    }

    public String toUserFriendlyString() {
        return toUserFriendlyString(null);
    }

    public String toUserFriendlyString(@Nullable ProfileCache cache) {
        StringBuilder str = new StringBuilder();

        if (playerDomain.size() > 0) {
            str.append(toPlayersString(cache));
        }

        if (groupDomain.size() > 0) {
            if (str.length() > 0) {
                str.append("; ");
            }

            str.append(toGroupsString());
        }
        if (!customDomains.isEmpty()) {
            if (str.length() > 0) {
                str.append("; ");
            }
            str.append(toCustomDomainsString());
        }

        return str.toString();
    }

    public Component toUserFriendlyComponent(@Nullable ProfileCache cache) {
        final TextComponent.Builder builder = TextComponent.builder("");
        if (playerDomain.size() > 0) {
            builder.append(toPlayersComponent(cache));
        }
        if (groupDomain.size() > 0) {
            if (playerDomain.size() > 0) {
                builder.append(TextComponent.of("; "));
            }
            builder.append(toGroupsComponent());
        }
        if (!customDomains.isEmpty()) {
            if (playerDomain.size() > 0 || groupDomain.size() > 0) {
                builder.append(TextComponent.of("; "));
            }
            builder.append(toCustomDomainsComponent());
        }
        return builder.build();
    }

    private Component toGroupsComponent() {
        final TextComponent.Builder builder = TextComponent.builder("");
        for (Iterator<String> it = groupDomain.getGroups().iterator(); it.hasNext(); ) {
            builder.append(TextComponent.of("g:", TextColor.GRAY))
                    .append(TextComponent.of(it.next(), TextColor.GOLD));
            if (it.hasNext()) {
                builder.append(TextComponent.of(", "));
            }
        }
        return builder.build().hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT, TextComponent.of("Groups")));
    }

    private Component toPlayersComponent(ProfileCache cache) {
        List<String> uuids = Lists.newArrayList();
        Map<String, UUID> profileMap = Maps.newHashMap();

        for (String name : playerDomain.getPlayers()) {
            profileMap.put(name, null);
        }

        if (cache != null) {
            ImmutableMap<UUID, Profile> results = cache.getAllPresent(playerDomain.getUniqueIds());
            for (UUID uuid : playerDomain.getUniqueIds()) {
                Profile profile = results.get(uuid);
                if (profile != null) {
                    profileMap.put(profile.getName(), uuid);
                } else {
                    uuids.add(uuid.toString());
                }
            }
        } else {
            for (UUID uuid : playerDomain.getUniqueIds()) {
                uuids.add(uuid.toString());
            }
        }

        final TextComponent.Builder builder = TextComponent.builder("");
        final Iterator<TextComponent> profiles = profileMap.keySet().stream().sorted().map(name -> {
            final UUID uuid = profileMap.get(name);
            if (uuid == null) {
                return TextComponent.of(name, TextColor.YELLOW)
                        .hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT, TextComponent.of("Name only", TextColor.GRAY)
                            .append(TextComponent.newline()).append(TextComponent.of("Click to copy"))))
                        .clickEvent(ClickEvent.of(ClickEvent.Action.COPY_TO_CLIPBOARD, name));
            } else {
                return TextComponent.of(name, TextColor.YELLOW)
                        .hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT, TextComponent.of("Last known name of uuid: ", TextColor.GRAY)
                            .append(TextComponent.of(uuid.toString(), TextColor.WHITE))
                            .append(TextComponent.newline()).append(TextComponent.of("Click to copy"))))
                        .clickEvent(ClickEvent.of(ClickEvent.Action.COPY_TO_CLIPBOARD, uuid.toString()));
            }
        }).iterator();
        while (profiles.hasNext()) {
            builder.append(profiles.next());
            if (profiles.hasNext() || !uuids.isEmpty()) {
                builder.append(TextComponent.of(", "));
            }
        }

        if (!uuids.isEmpty()) {
            builder.append(TextComponent.of(uuids.size() + " unknown uuid" + (uuids.size() == 1 ? "" : "s"), TextColor.GRAY)
                    .hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT, TextComponent.of("Unable to resolve the name for:", TextColor.GRAY)
                        .append(TextComponent.newline())
                        .append(TextComponent.of(String.join("\n", uuids), TextColor.WHITE))
                        .append(TextComponent.newline().append(TextComponent.of("Click to copy")))))
                    .clickEvent(ClickEvent.of(ClickEvent.Action.COPY_TO_CLIPBOARD, String.join(",", uuids))));
        }


        return builder.build();
    }

    private Component toCustomDomainsComponent() {
        final TextComponent.Builder builder = TextComponent.builder("");
        for (Iterator<CustomDomain> it = customDomains.iterator(); it.hasNext(); ) {
            CustomDomain domain = it.next();
            builder.append(TextComponent.of(domain.getName() + ":", TextColor.LIGHT_PURPLE))
                    .append(TextComponent.of(domain.toString(), TextColor.GOLD));
            if (it.hasNext()) {
                builder.append(TextComponent.of(", "));
            }
        }
        return builder.build().hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT, TextComponent.of("CustomDomain")));
    }


    @Override
    public boolean isDirty() {
        return playerDomain.isDirty() || groupDomain.isDirty() ||
                customDomainsChanged ||  customDomains.stream().anyMatch(ChangeTracked::isDirty);
    }

    @Override
    public void setDirty(boolean dirty) {
        playerDomain.setDirty(dirty);
        groupDomain.setDirty(dirty);
        customDomainsChanged = dirty;
        customDomains.forEach(d -> d.setDirty(dirty));
    }

    @Override
    public String toString() {
        return "{players=" + playerDomain +
                ", groups=" + groupDomain +
                ", custom=" + customDomains +
                '}';
    }

}
