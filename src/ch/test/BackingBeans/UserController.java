package ch.test.BackingBeans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.push.PushContext;
import org.primefaces.push.PushContextFactory;
import ch.test.businessBeans.AccountBean;
import ch.test.businessBeans.ExchangeRateBean;
import ch.test.businessBeans.UserBean;
import ch.test.entities.ExchangeRatePair;
import ch.test.entities.Trade;
import ch.test.entities.User;

/**
 * User Controller that provides creating and deleting of trades. It holds the
 * actual game ranking as well as game-help. It also provides the actual
 * collection of open trades of the user and gets updated regularly.
 * 
 * @author Marc Dünki
 */
@Named
@SessionScoped
public class UserController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2903691433969982321L;

	@Inject
	private AccountBean accountBean;

	@Inject
	private ExchangeRateBean exchangeRateBean;

	@Inject
	private UserBean userBean;

	private List<User> ranking;
	private List<Trade> trades;
	private List<ExchangeRatePair> exchangeRates;
	private List<Double> tradeAmounts;
	private ExchangeRatePair exchangeRatePair;
	private double tradeAmount;
	private double stopLoss;
	private double takeProfit;
	private User user;
	private String help;
	private double totalAmountOfOpenTrades;

	/**
	 * Default constructor.
	 */
	public UserController() {
		this.tradeAmounts = new ArrayList<Double>();

	}

	public void TestTrading() {
		List<Trade> localTrades = new ArrayList<Trade>();
		for (int i = 0; i < 20; i++) {
			for (ExchangeRatePair exrate : this.exchangeRates) {
				this.exchangeRatePair = exrate;
				this.stopLoss = 0.01;
				this.takeProfit = 9999.0;
				this.tradeAmount = 10000.00;
				if (!this.exchangeRatePair.getCurrencyFrom().getCurrencyCode()
						.equals("EUR")) {
					localTrades.add(accountBean.openTrade(exrate, tradeAmount,
							stopLoss, takeProfit));

				}
			}

			this.trades = accountBean.getUsersOpenTradesRefreshed();
			Iterator<Trade> it = localTrades.iterator();
			while (it.hasNext()) {
				Trade trade = it.next();
				it.remove();
				this.closeTrade(trade);
			}
		}
	}

	@PostConstruct
	public void init() {
		this.exchangeRatePair = new ExchangeRatePair();
		this.exchangeRates = exchangeRateBean.getAllCrossRates();
		this.totalAmountOfOpenTrades = accountBean.getTotalValueOfTrades();
		this.user = this.userBean.getUser();
		this.trades = accountBean.getUsersOpenTradesRefreshed();
		this.ranking = accountBean.getRanking();
		this.tradeAmounts.add(10000.00D);
		this.tradeAmounts.add(20000.00D);
		this.tradeAmounts.add(50000.00D);
		this.tradeAmounts.add(100000.00D);
		this.tradeAmounts.add(200000.00D);
		this.tradeAmounts.add(300000.00D);

		this.help = "<html><head><title>HTML Online Editor Sample</title></head><body><h1>Forex Trading Game Rules</h1><p style=\"margin: 0px; font-size: 11px; font-family: Monaco;\">1. Account:</p><p style=\"margin: 0px; font-size: 11px; font-family: Monaco;\">&nbsp;</p><p style=\"margin: 0px; font-size: 11px; font-family: Monaco;\">You start with a balance of 5000 Euros. Your account as well as actual balance are always displayed in the options-tab &quot;my account&quot;. Balance is the amount of your actual account including the real-time value of your open trades.</p><p style=\"margin: 0px; font-size: 11px; font-family: Monaco;\">Once the real-time values of your trades can&#39;t be convered by your account value, all of your open trades will be closed and your account will be blocked.</p><p style=\"margin: 0px; font-size: 11px; font-family: Monaco; min-height: 15px;\">&nbsp;</p><p style=\"margin: 0px; font-size: 11px; font-family: Monaco;\">2. Trades:</p><p style=\"margin: 0px; font-size: 11px; font-family: Monaco;\">&nbsp;</p><p style=\"margin: 0px; font-size: 11px; font-family: Monaco;\">As long as your account can cover the amount you want to spend, you can open trades. The leverage of the total amout is 100, this means if you spend 100&#39;000 EUR your investment will be 1&#39;000 EUR. These 1&#39;000 EUR will be deducted from your account and added to the balance. During the lifetime of your trades, the trade value will be calculated everytime the exchange rates are updated.</p><p style=\"margin: 0px; font-size: 11px; font-family: Monaco;\">The resulting differences of your trades are charged to your balance. Once you close a trade, the trade value will be charged to your account and deducted from the balance. A trading fee of 0.02% is applied when you buy and sell a trade. For every trade one can set a Stop-Loss and Take Profit. A Stop-Loss sells the trade automatically once the exchange rate hits the specified max-value. A Take Profit, on the other hand, secures a certain win once the exchange rate hits the specied min-value.</p><p style=\"margin: 0px; font-size: 11px; font-family: Monaco; min-height: 15px;\">&nbsp;</p><p style=\"margin: 0px; font-size: 11px; font-family: Monaco;\">3. Ranking:</p><p style=\"margin: 0px; font-size: 11px; font-family: Monaco;\">&nbsp;</p><p style=\"margin: 0px; font-size: 11px; font-family: Monaco;\">In the options-tab &quot;ranking&quot; you can check the ranking of all active players that are participating.</p></body></html>";

		System.out.println("UserController created");

		checkIfAccountIsBlocked();

	}

	private void checkIfAccountIsBlocked() {
		if (this.user.getAccount().getIsBlocked()) {
			this.notifyBlockedAccount();
		}

	}

	public void openTrade() {
		if ((Double) stopLoss instanceof Double
				&& (Double) takeProfit instanceof Double) {
			Trade trade = accountBean.openTrade(exchangeRatePair, tradeAmount,
					stopLoss, takeProfit);

			this.tradeAmount = 0.0D;
			this.exchangeRatePair = null;
			this.stopLoss = 0.0D;
			this.takeProfit = 0.0D;
			if (trade != null) {
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
						"Trade successfully opened", "");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				this.trades.add(trade);
				this.refreshRanking();
				this.refreshAccount();
				this.refreshRanking();
				this.pushSocketMessageOptionsPanel();

			} else if (trade == null) {
				FacesMessage msg = new FacesMessage(
						FacesMessage.SEVERITY_WARN,
						"You can't open a trade. Your account doesn't have enough balance",
						"");
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		} else {
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN,
					"Please enter valid values for stop-loss and take profit",
					"");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}

	public void closeTrade(Trade trade) {
		this.accountBean.closeTrade(trade);
		this.trades.remove(trade);
		this.refreshAccount();
		this.refreshRanking();
		this.pushSocketMessageOptionsPanel();
	}

	@SuppressWarnings("unused")
	private void pushSocketMessageAccount() {
		PushContext pushContext = PushContextFactory.getDefault()
				.getPushContext();
		System.out.println("Push:" + pushContext.toString());
		pushContext.push("/account", "Updated Account");
	}

	@SuppressWarnings("unused")
	private void pushSocketMessageRanking() {
		PushContext pushContext = PushContextFactory.getDefault()
				.getPushContext();
		System.out.println("Push:" + pushContext.toString());
		pushContext.push("/ranking", "Updated Ranking");
	}

	private void notifyBlockedAccount() {
		FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
				"Your account has been blocked due to loss.", "");
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}

	private void pushSocketMessageOptionsPanel() {
		PushContext pushContext = PushContextFactory.getDefault()
				.getPushContext();
		System.out.println("Push:" + pushContext.toString()
				+ " updateOptionsPanel");
		pushContext.push("/updateOptionsPanel", "Updated Options Panel");
	}

	@SuppressWarnings("unused")
	private void pushSocketMessageTradeForm() {
		PushContext pushContext = PushContextFactory.getDefault()
				.getPushContext();
		pushContext.push("/updateTradeForm", "Update Trade Form");
	}

	public void setRanking(List<User> ranking) {
		this.ranking = ranking;
	}

	public List<User> getRanking() {
		return this.ranking;
	}

	public List<Trade> getTrades() {
		return trades;
	}

	public void setTrades(List<Trade> trades) {
		this.trades = trades;
	}

	public List<ExchangeRatePair> getExchangeRates() {
		return exchangeRates;
	}

	public void setExchangeRates(List<ExchangeRatePair> exchangeRates) {
		this.exchangeRates = exchangeRates;
	}

	public double getTradeAmount() {
		return tradeAmount;
	}

	public void setTradeAmount(double amount) {
		this.tradeAmount = amount;
	}

	public double getStopLoss() {
		return stopLoss;
	}

	public void setStopLoss(double stopLoss) {
		this.stopLoss = stopLoss;
	}

	public double getTakeProfit() {
		return takeProfit;
	}

	public void setTakeProfit(double takeProfit) {
		this.takeProfit = takeProfit;
	}

	public ExchangeRatePair getExchangeRatePair() {
		return exchangeRatePair;
	}

	public void setExchangeRatePair(ExchangeRatePair exchangeRates) {
		this.exchangeRatePair = exchangeRates;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User actualUser) {
		this.user = actualUser;
	}

	public List<Double> getTradeAmounts() {
		return this.tradeAmounts;
	}

	public void refreshTradeValues() {
		this.trades = accountBean.getUsersOpenTradesRefreshed();
	}

	public void refreshAccount() {
		this.user = userBean.getUser();
		this.checkIfAccountIsBlocked();
		this.totalAmountOfOpenTrades = accountBean.getTotalValueOfTrades();
	}

	public void refreshRanking() {
		this.ranking = accountBean.getRanking();
	}

	public void refreshOptionsPanel() {
		this.refreshAccount();
		this.refreshRanking();
	}

	public String getHelpText() {
		return this.help;
	}

	public double getTotalAmountOfOpenTrades() {
		return this.totalAmountOfOpenTrades;
	}
}
