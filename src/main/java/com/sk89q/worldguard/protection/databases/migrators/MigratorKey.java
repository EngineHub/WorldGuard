package com.sk89q.worldguard.protection.databases.migrators;

public class MigratorKey {
	public final String from;
	public final String to;
	
	public MigratorKey(String from, String to) {
		this.from = from;
		this.to = to;
	}
	
	public boolean equals(Object o) {
		MigratorKey other = (MigratorKey) o;
		
		return other.from == this.from && other.to == this.to;
	}
	
	public int hashCode() {
		int hash = 17;
		hash = hash * 31 + this.from.hashCode();
		hash = hash * 31 + this.to.hashCode();
		return hash;
	}
}