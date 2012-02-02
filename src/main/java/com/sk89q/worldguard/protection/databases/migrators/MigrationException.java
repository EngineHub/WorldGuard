package com.sk89q.worldguard.protection.databases.migrators;

public class MigrationException extends Exception {
	public MigrationException() {
		super();
	}
	
	public MigrationException(String message) {
		super(message);
	}
	
	public MigrationException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public MigrationException(Throwable cause) {
		super(cause);
	}
	
}
