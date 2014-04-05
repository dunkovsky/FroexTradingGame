package ch.test.entities;

import java.io.Serializable;
import java.util.Currency;
import java.util.Set;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Session Bean implementation class Account
 */

@Entity(name="account")
@Table(name="account")

@NamedQuery(name = "receiveValidAccounts", query = "SELECT a FROM account a WHERE a.isBlocked = false")

@Stateless
public class Account implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1702358894028191680L;
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;
	private double balance;
	private double marginBalance;
	private Currency currency;
	private boolean isBlocked;
	@OneToMany(mappedBy="account")
	protected Set<Trade> trades;

	/**
     * Default constructor. 
     */
    public Account() {
        this.setBalance(5000.00f);
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

	public double getMarginBalance() {
		return marginBalance;
	}

	public void setMarginBalance(double marginBalance) {
		this.marginBalance = marginBalance;
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
	
	public void updateBalance(double value){
		this.balance += value;
	}
	
	public void updateMarginBalance(double value){
		this.marginBalance += value;
	}
	
	public void addTrade(Trade trade){
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
