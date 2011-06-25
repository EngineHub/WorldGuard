package com.sk89q.worldguard.bukkit;

import java.util.List;

import org.bukkit.plugin.Plugin;

import com.iConomy.iConomy;
import com.iConomy.system.Account;
import com.iConomy.system.BankAccount;
import com.iConomy.system.Holdings;
import com.iConomy.util.Constants;

/**
 * Manager to handle iConomy integration.  Note: ALL iConomy functions must be defined in this class.
 * No native iConomy datatypes are accessible.  If your trying to access them, your violating the modularity of this class.
 * 
 * @author Donald Scott
 * @since 6/24/11
 */
public class iConomyManager {
	/**
	 * Reference to the iConomy plugin.
	 */
	private static iConomy ecoPlug = null;

	public iConomyManager(){
		
	}
	/**
	 * Should be called on iConomy plugin load.
	 * 
	 * @param iConomy The iConomy object provided by the WorldGuardPluginListener.
	 */
	public static void initialize(iConomy ecoPlug){
		iConomyManager.ecoPlug = ecoPlug;
	}
	/**
	 * <b>Must</b> be called to ensure iConomy is loaded!
	 * 
	 * @return iConomy != null
	 */
	public static boolean isloaded(){
		return ecoPlug != null;
	}
	/**
	 * Should be called if the iConomy plugin is unloaded by the WorldGuardPluginListener.
	 */
	public static void deInitialize(){
		iConomyManager.ecoPlug = null;
	}
	
	public String format(double amount) {
		return iConomy.format(amount);
	}

	public boolean hasBanks() {
		return Constants.Banking;
	}

	public boolean hasBank(String bank) {
		return (hasBanks()) && iConomy.Banks.exists(bank);
	}

	public boolean hasAccount(String name) {
		return iConomy.hasAccount(name);
	}
    public void createAccount(String name){
    	iConomy.Accounts.create(name);
    }
	public boolean hasBankAccount(String bank, String name) {
		return (hasBank(bank)) && iConomy.getBank(bank).hasAccount(name);
	}

	public EcoAccount getAccount(String name) {
		return new EcoAccount(iConomy.getAccount(name));
	}

	public EcoBankAccount getBankAccount(String bank, String name) {
		return new EcoBankAccount(iConomy.getBank(bank).getAccount(name));
	}

	public boolean isCompatible(Plugin plugin) {
		return plugin.getDescription().getName().equalsIgnoreCase("iconomy")
				&& plugin.getClass().getName().equals("com.iConomy.iConomy")
				&& plugin instanceof iConomy;
	}

	public void setPlugin(Plugin plugin) {
		ecoPlug = (iConomy) plugin;
	}
	/**
	 * Represents a users finances.
	 * 
	 * @author Donald Scott
	 *
	 */
	public class EcoAccount{
		private Account account;
		private Holdings holdings;

		public EcoAccount(Account account) {
			this.account = account;
			this.holdings = account.getHoldings();
		}

		public double balance() {
			return this.holdings.balance();
		}

		public boolean set(double amount) {
			if (this.holdings == null)
				return false;
			this.holdings.set(amount);
			return true;
		}

		public boolean add(double amount) {
			if (this.holdings == null)
				return false;
			this.holdings.add(amount);
			return true;
		}

		public boolean subtract(double amount) {
			if (this.holdings == null)
				return false;
			this.holdings.subtract(amount);
			return true;
		}

		public boolean hasEnough(double amount) {
			return this.holdings.hasEnough(amount);
		}

		public boolean hasOver(double amount) {
			return this.holdings.hasOver(amount);
		}

		public boolean hasUnder(double amount) {
			return this.holdings.hasUnder(amount);
		}

		public boolean isNegative() {
			return this.holdings.isNegative();
		}

		public boolean remove() {
			if (this.account == null)
				return false;
			this.account.remove();
			return true;
		}
	}
	/**
	 * Represents an iConomy bank account.
	 * 
	 * @author Donald Scott
	 *
	 */
	public class EcoBankAccount {
		private BankAccount account;
		private Holdings holdings;

		public EcoBankAccount(BankAccount account) {
			this.account = account;
			this.holdings = account.getHoldings();
		}

		public String getBankName() {
			return this.account.getBankName();
		}

		public int getBankId() {
			return this.account.getBankId();
		}
		
		public boolean hasEnough(double amount) {
			return this.holdings.hasEnough(amount);
		}

		public boolean hasOver(double amount) {
			return this.holdings.hasOver(amount);
		}

		public boolean hasUnder(double amount) {
			return this.holdings.hasUnder(amount);
		}

		public boolean isNegative() {
			return this.holdings.isNegative();
		}

		public double balance() {
			return this.holdings.balance();
		}

		public boolean set(double amount) {
			if (this.holdings == null)
				return false;
			this.holdings.set(amount);
			return true;
		}

		public boolean add(double amount) {
			if (this.holdings == null)
				return false;
			this.holdings.add(amount);
			return true;
		}

		public boolean subtract(double amount) {
			if (this.holdings == null)
				return false;
			this.holdings.subtract(amount);
			return true;
		}


		public boolean remove() {
			if (this.account == null)
				return false;
			this.account.remove();
			return true;
		}
	}
	public void dividAndDistribute(double regionPrice,
			List<EcoAccount> ownerAccounts) {
		double amountPer = regionPrice/ownerAccounts.size();
		amountPer = Math.round(amountPer*100.0) / 100.0; //Remove trailing decimal, and yes, some money will be eaten by java
		for (EcoAccount account:ownerAccounts){
			account.add(amountPer);
		}
	}
}
