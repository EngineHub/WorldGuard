// $Id$
/*
 * WorldGuard
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.sk89q.worldguard.protection;

import com.sk89q.worldguard.bukkit.BukkitPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.Iterator;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.regions.flags.*;
import com.sk89q.worldguard.protection.regions.flags.Flags.FlagType;
import com.sk89q.worldguard.protection.regions.flags.RegionFlag;
import com.sk89q.worldguard.protection.regions.flags.RegionFlag.State;
import com.sk89q.worldguard.protection.regions.flags.info.*;
import java.util.List;

/**
 * Represents a setFlag of regions and their rules as applied to one point or region.
 * 
 * @author sk89q
 */
public class ApplicableRegionSet {

	private GlobalFlags global;
	private List<ProtectedRegion> applicable;
	private ProtectedRegion affectedRegion;

	/**
	 * Construct the object.
	 * 
	 * @param applicable
	 * @param global
	 */
	public ApplicableRegionSet(List<ProtectedRegion> applicable, GlobalFlags global) {
		this.applicable = applicable;
		this.global = global;

		determineAffectedRegion();
	}

	/**
	 * Checks if a player can build in an area.
	 * 
	 * @param player
	 * @return
	 */
	public boolean canBuild(LocalPlayer player) {

		if (!this.isAnyRegionAffected()) {
			if (!global.canBuild) {
				return player.hasPermission("region.build_on_nomansland");
			}
			return false;
		}

		return getStateFlag(Flags.BUILD, true).getValue(State.DENY) == State.ALLOW || isMember(player);
	}

	public boolean isStateFlagAllowed(StateRegionFlagInfo info) {

		return isStateFlagAllowed(info, global.getDefaultValue(info.type));
	}

	public boolean isStateFlagAllowed(StateRegionFlagInfo info, boolean def) {

		if (!this.isAnyRegionAffected()) {
			return def;
		}
		State defState = def ? State.ALLOW : State.DENY;
		return getStateFlag(info, true).getValue(defState) == State.ALLOW;
	}

	public boolean isStateFlagAllowed(StateRegionFlagInfo info, LocalPlayer player) {

		if (info.type == FlagType.BUILD) {
			return canBuild(player);
		}
		return isStateFlagAllowed(info, global.getDefaultValue(info.type), player);
	}

	public boolean isStateFlagAllowed(StateRegionFlagInfo info, boolean def, LocalPlayer player) {

		if (info.type == FlagType.BUILD) {
			return canBuild(player);
		}

		if (!this.isAnyRegionAffected()) {
			return def;
		}

		State defState = def ? State.ALLOW : State.DENY;
		return getStateFlag(info, true).getValue(defState) == State.ALLOW || this.isMember(player);
	}

	private RegionFlag getFlag(RegionFlagInfo<?> info, Boolean inherit) {

		ProtectedRegion region = affectedRegion;

		if (region == null) {
			return null;
		}

		if (!inherit) {
			return region.getFlags().getFlag(info);
		} else {
			RegionFlag value;
			do {
				value = region.getFlags().getFlag(info);
				region = region.getParent();

			} while (!value.hasValue() && region != null);

			return value;
		}

	}

	public BooleanRegionFlag getBooleanFlag(BooleanRegionFlagInfo info, boolean inherit) {

		RegionFlag flag = this.getFlag(info, inherit);

		if (flag instanceof BooleanRegionFlag) {
			return (BooleanRegionFlag) flag;
		} else {
			return new BooleanRegionFlag();
		}
	}

	public StateRegionFlag getStateFlag(StateRegionFlagInfo info, boolean inherit) {

		RegionFlag flag = this.getFlag(info, inherit);

		if (flag instanceof StateRegionFlag) {
			return (StateRegionFlag) flag;
		} else {
			return new StateRegionFlag();
		}
	}

	public IntegerRegionFlag getIntegerFlag(IntegerRegionFlagInfo info, boolean inherit) {

		RegionFlag flag = this.getFlag(info, inherit);

		if (flag instanceof IntegerRegionFlag) {
			return (IntegerRegionFlag) flag;
		} else {
			return new IntegerRegionFlag();
		}
	}

	public DoubleRegionFlag getDoubleFlag(DoubleRegionFlagInfo info, boolean inherit) {

		RegionFlag flag = this.getFlag(info, inherit);

		if (flag instanceof DoubleRegionFlag) {
			return (DoubleRegionFlag) flag;
		} else {
			return new DoubleRegionFlag();
		}
	}

	public StringRegionFlag getStringFlag(StringRegionFlagInfo info, boolean inherit) {

		RegionFlag flag = this.getFlag(info, inherit);

		if (flag instanceof StringRegionFlag) {
			return (StringRegionFlag) flag;
		} else {
			return new StringRegionFlag();
		}
	}

	public RegionGroupRegionFlag getRegionGroupFlag(RegionGroupRegionFlagInfo info, boolean inherit) {

		RegionFlag flag = this.getFlag(info, inherit);

		if (flag instanceof RegionGroupRegionFlag) {
			return (RegionGroupRegionFlag) flag;
		} else {
			return new RegionGroupRegionFlag();
		}
	}

	public LocationRegionFlag getLocationFlag(LocationRegionFlagInfo info, boolean inherit) {

		RegionFlag flag = this.getFlag(info, inherit);

		if (flag instanceof LocationRegionFlag) {
			return (LocationRegionFlag) flag;
		} else {
			return new LocationRegionFlag();
		}
	}

	public boolean isAnyRegionAffected() {
		return this.applicable.size() > 0;
	}

	/**
	 * Determines the region with the hightest priority.
	 * 
	 */
	private void determineAffectedRegion() {

		affectedRegion = null;
		Iterator<ProtectedRegion> iter = applicable.iterator();

		while (iter.hasNext()) {
			ProtectedRegion region = iter.next();

			if (affectedRegion == null || affectedRegion.getPriority() < region.getPriority()) {
				affectedRegion = region;
			}
		}
	}

	public boolean isOwner(LocalPlayer player) {
		return affectedRegion != null ? affectedRegion.isOwner(player) : false;
	}

	/**
	 * Checks whether a player is a member of the region or any of its parents.
	 * 
	 * @param player
	 * @return
	 */
	public boolean isMember(LocalPlayer player) {
		return affectedRegion != null ? affectedRegion.isMember(player) : false;
	}

	public String getAffectedRegionId() {
		return affectedRegion != null ? affectedRegion.getId() : "";
	}

	public int getAffectedRegionPriority() {
		return affectedRegion != null ? affectedRegion.getPriority() : 0;
	}

}
