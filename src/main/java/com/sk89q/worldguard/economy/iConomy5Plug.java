package com.sk89q.worldguard.economy;

import com.iConomy.iConomy;
import com.iConomy.system.Account;
import com.iConomy.system.BankAccount;
import com.iConomy.system.Holdings;
import com.iConomy.util.Constants;

import org.bukkit.plugin.Plugin;

public class iConomy5Plug implements EcoMethod {
	private iConomy iCoPlugin;

	public iConomy getPlugin() {
		return this.iCoPlugin;
	}

	public String getName() {
		return "iConomy";
	}

	public String getVersion() {
		return "5";
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

	public boolean hasBankAccount(String bank, String name) {
		return (hasBank(bank)) && iConomy.getBank(bank).hasAccount(name);
	}

	public EcoMethodAccount getAccount(String name) {
		return new iCoAccount(iConomy.getAccount(name));
	}

	public EcoMethodBankAccount getBankAccount(String bank, String name) {
		return new iCoBankAccount(iConomy.getBank(bank).getAccount(name));
	}

	public boolean isCompatible(Plugin plugin) {
		return plugin.getDescription().getName().equalsIgnoreCase("iconomy")
				&& plugin.getClass().getName().equals("com.iConomy.iConomy")
				&& plugin instanceof iConomy;
	}

	public void setPlugin(Plugin plugin) {
		iCoPlugin = (iConomy) plugin;
	}

	public class iCoAccount implements EcoMethodAccount {
		private Account account;
		private Holdings holdings;

		public iCoAccount(Account account) {
			this.account = account;
			this.holdings = account.getHoldings();
		}

		public Account getiCoAccount() {
			return account;
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

		public boolean multiply(double amount) {
			if (this.holdings == null)
				return false;
			this.holdings.multiply(amount);
			return true;
		}

		public boolean divide(double amount) {
			if (this.holdings == null)
				return false;
			this.holdings.divide(amount);
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

	public class iCoBankAccount implements EcoMethodBankAccount {
		private BankAccount account;
		private Holdings holdings;

		public iCoBankAccount(BankAccount account) {
			this.account = account;
			this.holdings = account.getHoldings();
		}

		public BankAccount getiCoBankAccount() {
			return account;
		}

		public String getBankName() {
			return this.account.getBankName();
		}

		public int getBankId() {
			return this.account.getBankId();
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

		public boolean multiply(double amount) {
			if (this.holdings == null)
				return false;
			this.holdings.multiply(amount);
			return true;
		}

		public boolean divide(double amount) {
			if (this.holdings == null)
				return false;
			this.holdings.divide(amount);
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
}