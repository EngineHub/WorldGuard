package com.sk89q.worldguard.bukkit.scheduler;

public class FoliaSchedulerAdapter {

  private static final boolean SUPPORTED = checkSupport();

  public static boolean isSupported() {
    return SUPPORTED;
  }

  private static boolean checkSupport() {
    try {
      Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }
}
