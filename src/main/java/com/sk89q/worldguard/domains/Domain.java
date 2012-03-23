package com.sk89q.worldguard.domains;

import com.sk89q.worldguard.LocalPlayer;

public interface Domain {
    /**
     * Returns true if a domain contains a player.
     *
     * @param player The player to check
     * @return whether this domain contains {@code player}
     */
    boolean contains(LocalPlayer player);

    /**
     * Returns true if a domain contains a player.<br />
     * This method doesn't check for groups!
     *
     * @param playerName The name of the player to check
     * @return whether this domain contains a player by that name
     */
    boolean contains(String playerName);
}
