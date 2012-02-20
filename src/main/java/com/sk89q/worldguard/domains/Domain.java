package com.sk89q.worldguard.domains;

import com.sk89q.worldguard.LocalPlayer;

public interface Domain {
    /**
     * Returns true if a domain contains a player.
     * 
     * @param player The player to check
     * @return whether this domain contains {@code player}
     */
    public boolean contains(LocalPlayer player);
}
