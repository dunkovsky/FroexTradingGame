package ch.test.entities;

import java.io.Serializable;
import java.util.Currency;
import java.util.Set;
import javax.ejb.Stateless;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Represents an account of the game, holding all data and providing operations
 * such as updating and receiving data.
 * 
 * @author Marc Dünki
 */

@Entity(name = "account")
@Table(name = "account")
@NamedQuery(name = "receiveValidAccounts", query = "SELECT a FROM account a WHERE a.isBlocked = false")
@Stateless
public class Account implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1702358894028191680L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	private double balance;
	private Currency currency;
	private boolean isBlocked;
	@OneToMany(mappedBy = "account")
	protected Set<Trade> trades;

	/**
	 * Default constructor.
	 */
	public Account() {
		this.setBalance(5000.00D);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}

	public Currency getCurrency() {
		return currency;
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	public Set<Trade> getTrades() {
		return trades;
	}

	public void setTrades(Set<Trade> trade) {
		this.trades = trade;
	}

	public void updateBalance(double value) {
		this.balance += value;
	}

	public void addTrade(Trade trade) {
		this.trades.add(trade);
		if (trade.getAccount() != this) {
			trade.setAccount(this);
		}
	}

	public boolean getIsBlocked() {
		return isBlocked;
	}

	public void setIsBlocked(boolean isBlocked) {
		this.isBlocked = isBlocked;
	}

}
