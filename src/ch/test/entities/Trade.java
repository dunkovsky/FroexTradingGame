package ch.test.entities;

import java.io.Serializable;
import java.util.Currency;
import java.util.List;
import java.util.Set;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Session Bean implementation class Trade
 */

@Entity(name = "trade")
@Table(name = "trade")
@NamedQueries({
		@NamedQuery(name = "openTrades", query = "SELECT t FROM trade t WHERE t.isClosed = false"),
		@NamedQuery(name = "closedTrades", query = "SELECT t FROM trade t WHERE t.isClosed = true"),
		@NamedQuery(name = "usersOpenTrades", query = "SELECT t FROM trade t WHERE t.isClosed = false "
				+ "															AND t.account = ("
				+ "															SELECT u.account FROM user u"
				+ "															WHERE u.username = ?1)"),
		@NamedQuery(name = "openTradesOfAccount", query = "SELECT t FROM trade t WHERE t.isClosed = false AND t.account.id = ?1") })
@Stateless
public class Trade implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3319130136439894275L;
	/**
	 * 
	 */

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	private double stopLoss;
	private double stopWin;
	private double tradeValue;
	private double actualInvestmentValue;
	private double difference;
	private double investmentOriginCurrency;
	private double investmentTradeCurrency;
	private double exchangeRateOnBuy;
	private boolean isClosed;
	private boolean isBooked;
	@Transient
	private static double feeInPercentge = 0.02f;
	@Transient
	private static double leverage = 100f;
	@OneToOne
	protected ExchangeRatePair exchangeRatePair;
	@ManyToOne
	protected Account account;

	/**
	 * Default constructor.
	 */
	public Trade() {
		// TODO Auto-generated constructor stub
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public double getStopLoss() {
		return stopLoss;
	}

	public void setStopLoss(double stopLoss) {
		this.stopLoss = stopLoss;
	}

	public double getStopWin() {
		return stopWin;
	}

	public void setStopWin(double stopWin) {
		this.stopWin = stopWin;
	}

	public double getExchangeRateOnBuy() {
		return exchangeRateOnBuy;
	}

	public void setExchangeRateOnBuy(double exchangeRate) {
		this.exchangeRateOnBuy = exchangeRate;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
		if (!account.getTrades().contains(this)) {
			account.getTrades().add(this);
		}
	}

	public boolean isClosed() {
		return isClosed;
	}

	public void setClosed(boolean isClosed) {
		this.isClosed = isClosed;
	}

	public ExchangeRatePair getExchangeRatePair() {
		return exchangeRatePair;
	}

	public void setExchangeRatePair(ExchangeRatePair exchangeRatePair) {
		this.exchangeRatePair = exchangeRatePair;
	}

	public boolean isBooked() {
		return isBooked;
	}

	public void setBooked(boolean isBooked) {
		this.isBooked = isBooked;
	}

	public static double getTradingFeeInPercentge() {
		return feeInPercentge;
	}

	public double getTradeValue() {
		return tradeValue;
	}

	public void setTradeValue(double tradeValue) {
		this.tradeValue = tradeValue;
	}

	public double getActualInvestmentValue() {
		return this.actualInvestmentValue;
	}

	public void setActualInvestmentValue(double actualInvestmentValue) {
		this.actualInvestmentValue = actualInvestmentValue;
	}

	public double getInvestmentOriginCurrency() {
		return this.investmentOriginCurrency;
	}

	public void setInvestmentOriginCurrency(double investment) {
		this.investmentOriginCurrency = investment;
	}

	public double getInvestmentTradeCurrency() {
		return this.investmentTradeCurrency;
	}

	public void setInvestmentTradeCurrency(double investment) {
		this.investmentTradeCurrency = investment;
	}

	public static double getLeverage() {
		return leverage;
	}

	public double getDifference() {
		return difference;
	}

	public void setDifference(double difference) {
		this.difference = difference;
	}

}
