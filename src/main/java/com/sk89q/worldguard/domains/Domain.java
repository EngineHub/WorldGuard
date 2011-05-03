package com.sk89q.worldguard.domains;

import com.sk89q.worldguard.LocalPlayer;

public interface Domain {
    /**
     * Returns true if a domain contains a player.
     * 
     * @param player
     * @return
     */
    public boolean contains(LocalPlayer player);
}
