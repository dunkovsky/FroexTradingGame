package ch.test.entities;

import java.io.Serializable;
import java.util.Currency;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import ch.test.interfaces.IConvertible;

/**
 * Session Bean implementation class ExchangeRatePair
 */
@Stateless
@Entity
@Table
@NamedQuery(name = "getCrossRatePair", query = "SELECT e FROM ExchangeRatePair e WHERE (e.currencyFrom = ?1 AND e.currencyTo = ?2)")
public class ExchangeRatePair implements Serializable, IConvertible {

	/**
	 * 
	 */
	private static final long serialVersionUID = -471601470697669225L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	private Currency currencyFrom;
	private Currency currencyTo;
	private double exchangeRate;
	private double inversiveExchangeRate;
	private boolean hasIncreased;
	private double changeInDifference;
	private double changeInPercentage;

	/**
	 * Default constructor.
	 */
	public ExchangeRatePair() {
		// TODO Auto-generated constructor stub
	}

	public Currency getCurrencyFrom() {
		return currencyFrom;
	}

	public void setCurrencyFrom(Currency currencyFrom) {
		this.currencyFrom = currencyFrom;
	}

	public Currency getCurrencyTo() {
		return currencyTo;
	}

	public void setCurrencyTo(Currency currencyTo) {
		this.currencyTo = currencyTo;
	}

	public double getExchangeRate() {
		return exchangeRate;
	}

	public void setExchangeRate(double exchangeRate) {
		this.exchangeRate = exchangeRate;
	}

	public Integer getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean getHasIncreased() {
		return hasIncreased;
	}

	public void setHasIncreased(boolean hasIncreased) {
		this.hasIncreased = hasIncreased;
	}

	public double getChangeInDifference() {
		return changeInDifference;
	}

	public double getChangeInPercentage() {
		return changeInPercentage;
	}

	public String toString() {
		String result;
		if (currencyFrom == null || currencyTo == null) {
			result = "";
		} else {
			result = this.currencyFrom.getCurrencyCode() + "/"
					+ this.currencyTo.getCurrencyCode();
		}
		return result;
	}

	public double getInversiveExchangeRate() {
		return inversiveExchangeRate;
	}

	public void setInversiveExchangeRate(double inversiveExchangeRate) {
		this.inversiveExchangeRate = inversiveExchangeRate;
	}

	public void setChangeInDifference(double changeInDifference) {
		this.changeInDifference = changeInDifference;
	}

	public void setChangeInPercentage(double changeInPercentage) {
		this.changeInPercentage = changeInPercentage;
	}
	
	
}
