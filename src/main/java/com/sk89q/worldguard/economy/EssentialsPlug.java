package com.sk89q.worldguard.economy;

import com.earth2me.essentials.Essentials;

import com.earth2me.essentials.api.Economy;
import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;



import org.bukkit.plugin.Plugin;

public class EssentialsPlug implements EcoMethod {
	private Essentials Essentials;

	public Essentials getPlugin() {
		return this.Essentials;
	}

	public String getName() {
		return "EssentialsEco";
	}

	public String getVersion() {
		return "2.2";
	}

	public String format(double amount) {
		return Economy.format(amount);
	}

	public boolean hasBanks() {
		return false;
	}

	public boolean hasBank(String bank) {
		return false;
	}

	public boolean hasAccount(String name) {
		return Economy.playerExists(name);
	}

	public boolean hasBankAccount(String bank, String name) {
		return false;
	}

	public EcoMethodAccount getAccount(String name) {
		if (!hasAccount(name))
			return null;
		return new EEcoAccount(name);
	}

	public EcoMethodBankAccount getBankAccount(String bank, String name) {
		return null;
	}

	public boolean isCompatible(Plugin plugin) {
		try {
			Class.forName("com.earth2me.essentials.api.Economy");
		} catch (Exception e) {
			return false;
		}

		return plugin.getDescription().getName().equalsIgnoreCase("essentials")
				&& plugin instanceof Essentials;
	}

	public void setPlugin(Plugin plugin) {
		Essentials = (Essentials) plugin;
	}

	public class EEcoAccount implements EcoMethodAccount {
		private String name;

		public EEcoAccount(String name) {
			this.name = name;
		}

		public double balance() {
			Double balance = 0.0;

			try {
				balance = Economy.getMoney(this.name);
			} catch (UserDoesNotExistException ex) {
				System.out
						.println("[REGISTER] Failed to grab balance in Essentials Economy: "
								+ ex.getMessage());
			}

			return balance;
		}

		public boolean set(double amount) {
			try {
				Economy.setMoney(name, amount);
			} catch (UserDoesNotExistException ex) {
				System.out
						.println("[REGISTER] User does not exist in Essentials Economy: "
								+ ex.getMessage());
				return false;
			} catch (NoLoanPermittedException ex) {
				System.out
						.println("[REGISTER] No loan permitted in Essentials Economy: "
								+ ex.getMessage());
				return false;
			}

			return true;
		}

		public boolean add(double amount) {
			try {
				Economy.add(name, amount);
			} catch (UserDoesNotExistException ex) {
				System.out
						.println("[REGISTER] User does not exist in Essentials Economy: "
								+ ex.getMessage());
				return false;
			} catch (NoLoanPermittedException ex) {
				System.out
						.println("[REGISTER] No loan permitted in Essentials Economy: "
								+ ex.getMessage());
				return false;
			}

			return true;
		}

		public boolean subtract(double amount) {
			try {
				Economy.subtract(name, amount);
			} catch (UserDoesNotExistException ex) {
				System.out
						.println("[REGISTER] User does not exist in Essentials Economy: "
								+ ex.getMessage());
				return false;
			} catch (NoLoanPermittedException ex) {
				System.out
						.println("[REGISTER] No loan permitted in Essentials Economy: "
								+ ex.getMessage());
				return false;
			}

			return true;
		}

		public boolean multiply(double amount) {
			try {
				Economy.multiply(name, amount);
			} catch (UserDoesNotExistException ex) {
				System.out
						.println("[REGISTER] User does not exist in Essentials Economy: "
								+ ex.getMessage());
				return false;
			} catch (NoLoanPermittedException ex) {
				System.out
						.println("[REGISTER] No loan permitted in Essentials Economy: "
								+ ex.getMessage());
				return false;
			}

			return true;
		}

		public boolean divide(double amount) {
			try {
				Economy.divide(name, amount);
			} catch (UserDoesNotExistException ex) {
				System.out
						.println("[REGISTER] User does not exist in Essentials Economy: "
								+ ex.getMessage());
				return false;
			} catch (NoLoanPermittedException ex) {
				System.out
						.println("[REGISTER] No loan permitted in Essentials Economy: "
								+ ex.getMessage());
				return false;
			}

			return true;
		}

		public boolean hasEnough(double amount) {
			try {
				return Economy.hasEnough(name, amount);
			} catch (UserDoesNotExistException ex) {
				System.out
						.println("[REGISTER] User does not exist in Essentials Economy: "
								+ ex.getMessage());
			}

			return false;
		}

		public boolean hasOver(double amount) {
			try {
				return Economy.hasMore(name, amount);
			} catch (UserDoesNotExistException ex) {
				System.out
						.println("[REGISTER] User does not exist in Essentials Economy: "
								+ ex.getMessage());
			}

			return false;
		}

		public boolean hasUnder(double amount) {
			try {
				return Economy.hasLess(name, amount);
			} catch (UserDoesNotExistException ex) {
				System.out
						.println("[REGISTER] User does not exist in Essentials Economy: "
								+ ex.getMessage());
			}

			return false;
		}

		public boolean isNegative() {
			try {
				return Economy.isNegative(name);
			} catch (UserDoesNotExistException ex) {
				System.out
						.println("[REGISTER] User does not exist in Essentials Economy: "
								+ ex.getMessage());
			}

			return false;
		}

		public boolean remove() {
			return false;
		}
	}
}