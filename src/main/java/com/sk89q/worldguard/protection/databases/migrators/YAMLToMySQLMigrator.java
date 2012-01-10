package com.sk89q.worldguard.protection.databases.migrators;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.databases.MySQLDatabase;
import com.sk89q.worldguard.protection.databases.ProtectionDatabase;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.databases.YAMLDatabase;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class YAMLToMySQLMigrator extends AbstractDatabaseMigrator {

	private WorldGuardPlugin plugin;
	private HashMap<String,File> regionYamlFiles;
	
	public YAMLToMySQLMigrator(WorldGuardPlugin plugin) {
		this.plugin = plugin;
		
		this.regionYamlFiles = new HashMap<String,File>();
		
		File files[] = new File(plugin.getDataFolder(), "worlds" + File.separator).listFiles();
		for (File item : files) {
			if (item.isDirectory()) {
				for (File subItem : item.listFiles()) {
					if (item.getName().equals("regions.yml")) {
						this.regionYamlFiles.put(item.getName(), subItem);
					}
				}
			}
		}
	}
	
	@Override
	protected Set<String> getWorldsFromOld() {
		return this.regionYamlFiles.keySet();
	}

	@Override
	protected Map<String, ProtectedRegion> getRegionsForWorldFromOld(String world) throws MigrationException {
		ProtectionDatabase oldDatabase;
		try {
			oldDatabase = new YAMLDatabase(this.regionYamlFiles.get(world));
			oldDatabase.load();
		} catch (FileNotFoundException e) {
			throw new MigrationException((Exception) e);
		} catch (ProtectionDatabaseException e) {
			throw new MigrationException((Exception) e);
		}
		
		return oldDatabase.getRegions();
	}

	@Override
	protected ProtectionDatabase getNewWorldStorage(String world) throws MigrationException {
		try {
			return new MySQLDatabase(plugin.getGlobalStateManager(), world);
		} catch (ProtectionDatabaseException e) {
			throw new MigrationException((Exception) e);
		}
	}

}
