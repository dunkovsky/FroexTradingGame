package ch.test.entities;

import java.io.Serializable;
import javax.ejb.Stateless;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Represents a trade of the game, holding all data and providing operations
 * such as updating and receiving of data.
 * 
 * @author Marc Dünki
 */

@Entity(name = "trade")
@Table(name = "trade")
@NamedQueries({
		// @NamedQuery(name = "openTrades", query =
		// "SELECT t FROM trade t WHERE t.isClosed = false"),
		// @NamedQuery(name = "closedTrades", query =
		// "SELECT t FROM trade t WHERE t.isClosed = true"),
		@NamedQuery(name = "usersOpenTrades", query = "SELECT t FROM trade t WHERE t.account = ("
				+ "															SELECT u.account FROM user u"
				+ "															WHERE u.username = ?1)", hints = { @javax.persistence.QueryHint(name = "org.hibernate.cacheMode", value = "REFRESH") }),
		@NamedQuery(name = "openTradesOfAccount", query = "SELECT t FROM trade t WHERE t.account.id = ?1", hints = { @javax.persistence.QueryHint(name = "org.hibernate.cacheMode", value = "REFRESH") }) })
@Stateless
public class Trade implements Serializable {

	private static final long serialVersionUID = 3319130136439894275L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	private double stopLoss;
	private double stopWin;
	private double tradeValue;
	private double actualInvestmentValue;
	private double investmentOriginCurrency;
	private double investmentTradeCurrency;
	private double exchangeRateOnBuy;
	@Transient
	private static double feeInPercentge = 0.0002f;
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

	public ExchangeRatePair getExchangeRatePair() {
		return exchangeRatePair;
	}

	public void setExchangeRatePair(ExchangeRatePair exchangeRatePair) {
		this.exchangeRatePair = exchangeRatePair;
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

}
