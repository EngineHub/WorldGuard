package com.sk89q.worldguard.protection.databases;

public class ProtectionDatabaseUpdateRequired extends ProtectionDatabaseException {
	private static final long serialVersionUID = 1L;

	protected String updateFile;

    public ProtectionDatabaseUpdateRequired(String updateFile) {
        super();
        
        this.updateFile = updateFile;
    }
    
    public String toString() {
    	return "You need to update your database to the latest version.\n" +
    			"\t\tPlease see " + this.updateFile;
    }
}
