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

import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.util.DomainInputResolver;
import com.sk89q.worldguard.protection.util.DomainInputResolver.UserLocatorPolicy;

import javax.annotation.Nullable;
import java.util.concurrent.Callable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Creates a new region.
 */
public class RegionAdder implements Callable<ProtectedRegion> {

    private final RegionManager manager;
    private final ProtectedRegion region;
    private final Actor actor;
    @Nullable
    private String[] ownersInput;
    private UserLocatorPolicy locatorPolicy = UserLocatorPolicy.UUID_ONLY;

    /**
     * Create a new instance.
     *
     * @param manager the manager
     * @param region the region
     */
    public RegionAdder(RegionManager manager, ProtectedRegion region) {
        this(manager, region, null);
    }

    /**
     * Create a new instance.
     * @param manager the manager
     * @param region the region
     * @param actor the actor
     */
    public RegionAdder(RegionManager manager, ProtectedRegion region, Actor actor) {
        checkNotNull(manager);
        checkNotNull(region);

        this.manager = manager;
        this.region = region;
        this.actor = actor;
    }

    /**
     * Add the owners from the command's arguments.
     *
     * @param args the arguments
     * @param namesIndex the index in the list of arguments to read the first name from
     */
    public void addOwnersFromCommand(CommandContext args, int namesIndex) {
        if (args.argsLength() >= namesIndex) {
            setLocatorPolicy(args.hasFlag('n') ? UserLocatorPolicy.NAME_ONLY : UserLocatorPolicy.UUID_ONLY);
            setOwnersInput(args.getSlice(namesIndex));
        }
    }

    @Override
    public ProtectedRegion call() throws Exception {
        if (ownersInput != null) {
            DomainInputResolver resolver = new DomainInputResolver(WorldGuard.getInstance().getProfileService(), ownersInput);
            resolver.setLocatorPolicy(locatorPolicy);
            resolver.setActor(actor);
            resolver.setRegion(region);

            DefaultDomain domain = resolver.call();
            region.getOwners().addAll(domain);
        }

        manager.addRegion(region);

        return region;
    }

    @Nullable
    public String[] getOwnersInput() {
        return ownersInput;
    }

    public void setOwnersInput(@Nullable String[] ownersInput) {
        this.ownersInput = ownersInput;
    }

    public UserLocatorPolicy getLocatorPolicy() {
        return locatorPolicy;
    }

    public void setLocatorPolicy(UserLocatorPolicy locatorPolicy) {
        this.locatorPolicy = locatorPolicy;
    }

}
