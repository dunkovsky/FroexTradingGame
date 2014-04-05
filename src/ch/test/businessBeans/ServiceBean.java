package ch.test.businessBeans;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;

import ch.test.BackingBeans.UserController;
import ch.test.entities.ExchangeRatePair;
import ch.test.entities.Trade;
import ch.test.entityCollections.ExchangeRateCollection;
import ch.test.eventHandler.EventHandler;
import ch.test.webservice.Currency;
import ch.test.webservice.CurrencyConvertorSoapProxy;

/**
 * Session Bean implementation class ServiceBean
 */
@Singleton
@LocalBean
public class ServiceBean {

	@EJB
	private ExchangeRateBean exchangeRateBean;

	@EJB
	private AccountBean accountBean;

	@EJB
	private TradeBean tradeBean;

	@Inject
	private EventHandler eventHandler;

	private CurrencyConvertorSoapProxy currencyProxy;

	private List<UserController> userControllers;

	/**
	 * Default constructor.
	 */
	public ServiceBean() {
		currencyProxy = new CurrencyConvertorSoapProxy();
		// TODO Auto-generated constructor stub
	}

	@PostConstruct
	public void init() {
		System.out.println("Service Bean created");
		userControllers = new ArrayList<UserController>();
	}

	private void updateExchangeRates() {
		if (exchangeRateBean == null) {
			System.out.println("ExchangeRateBean null");
		}
		List<ExchangeRatePair> exchangeRates = exchangeRateBean
				.getAllCrossRates();
		ExchangeRateCollection exchangeRateCollection = new ExchangeRateCollection();
		if (exchangeRates.size() > 0) {
			for (ExchangeRatePair exchangeRate : exchangeRates) {
				try {
					double rateRaw = (double) currencyProxy.conversionRate(
							new Currency(exchangeRate.getCurrencyFrom()
									.getCurrencyCode()), new Currency(
									exchangeRate.getCurrencyTo()
											.getCurrencyCode()));
					double rate = this.approximate(rateRaw, 4);
					System.out.println("Rate: " + rate);
					double inverse = 1.0D;
					double formerExchangeRate = exchangeRate.getExchangeRate();
					System.out.println("Inversive Rate: " + inverse / rate);
					exchangeRate
							.setHasIncreased(rate - formerExchangeRate >= 0);
					exchangeRate.setChangeInDifference(rate
							- formerExchangeRate);
					exchangeRate
							.setChangeInPercentage(((rate - formerExchangeRate) / formerExchangeRate) * 100);
					exchangeRate.setExchangeRate(rate);
					exchangeRate.setInversiveExchangeRate(inverse / rate);
					exchangeRateBean.updateExchangeRatePair(exchangeRate);
					exchangeRateCollection.addElement(exchangeRate);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			eventHandler.publishUpdatedExchangeRates(exchangeRateCollection);
		} else {
			System.out.println("Exchange Rates List null");
		}
	}

	public void register(UserController userController) {
		this.userControllers.add(userController);
	}

	public void unregister(UserController userController) {
		this.userControllers.remove(userController);
	}

	@Schedule(dayOfMonth = "*", dayOfWeek = "Mon-Fri", hour = "*", minute = "*", second = "0")
	private void refreshTradeValues() {
		updateExchangeRates();
		tradeBean.updateAllTrades();
		accountBean.updateAllAccounts();
		accountBean.checkAllAccounts();
	}

	@Schedule(dayOfMonth = "*", dayOfWeek = "Mon-Fri", hour = "23", minute = "59", second = "0")
	private void deleteRecordsDB() {
		List<Trade> closedTrades = tradeBean.getClosedTrades();
		if (closedTrades.size() > 0) {
			for (Trade trade : closedTrades) {
				tradeBean.removeTrade(trade);
			}
		}
	}

	private double approximate(double value, int digits) {
		double gerundet = Math.round(value * Math.pow(10d, digits));
		return gerundet / Math.pow(10d, digits);
	}

}
