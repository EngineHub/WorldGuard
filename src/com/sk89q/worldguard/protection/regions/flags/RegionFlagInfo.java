/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sk89q.worldguard.protection.regions.flags;

import com.sk89q.worldguard.protection.regions.flags.FlagDatabase.FlagType;
import com.sk89q.worldguard.protection.regions.flags.RegionFlag.FlagDataType;

/**
 *
 * @author Michael
 */
public class RegionFlagInfo {

    public String name;
    public FlagType type;
    public FlagDataType dataType;

    public RegionFlagInfo(String name, FlagType type, FlagDataType dataType)
    {
        this.name = name;
        this.type = type;
        this.dataType = dataType;
    }
}
