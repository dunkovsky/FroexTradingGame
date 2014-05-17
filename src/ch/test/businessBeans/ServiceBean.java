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

import com.arjuna.ats.jta.exceptions.RollbackException;

import ch.test.BackingBeans.UserController;
import ch.test.entities.ExchangeRatePair;
import ch.test.entityCollections.ExchangeRateCollection;
import ch.test.eventHandler.EventHandler;
import ch.test.webservice.Currency;
import ch.test.webservice.CurrencyConvertorSoapProxy;

/**
 * Service Bean is a Singleton Bean that handles the update routine of the
 * exchange rates as well. It uses a timer to get the exchange rate updates and
 * launches the update and validation of the trades.
 */
@Singleton
@LocalBean
public class ServiceBean {

	@Inject
	private ExchangeRateBean exchangeRateBean;

	@EJB
	private AccountBean accountBean;

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
		List<ExchangeRatePair> exchangeRates = exchangeRateBean
				.getAllCrossRates();
		ExchangeRateCollection exchangeRateCollection = new ExchangeRateCollection();
		if (exchangeRates.size() > 0) {
			int counter = 0;
			for (ExchangeRatePair exchangeRate : exchangeRates) {
				try {
					double inverseRate;
					double rate;
					if (exchangeRate.getCurrencyTo().getCurrencyCode()
							.equals("XAU")) {
						double rateRawInversiveXAU = (double) currencyProxy
								.conversionRate(new Currency(exchangeRate
										.getCurrencyTo().getCurrencyCode()),
										new Currency(exchangeRate
												.getCurrencyFrom()
												.getCurrencyCode()));
						inverseRate = rateRawInversiveXAU;
						rate = 1.0 / inverseRate;
					} else {
						double rateRaw = (double) currencyProxy.conversionRate(
								new Currency(exchangeRate.getCurrencyFrom()
										.getCurrencyCode()), new Currency(
										exchangeRate.getCurrencyTo()
												.getCurrencyCode()));
						rate = rateRaw;
						inverseRate = 1.0 / rate;
					}

					double formerExchangeRate = exchangeRate.getExchangeRate();
					exchangeRate
							.setHasIncreased(rate - formerExchangeRate >= 0.0);
					exchangeRate.setChangeInDifference(rate
							- formerExchangeRate);
					exchangeRate
							.setChangeInPercentage(((rate - formerExchangeRate) / formerExchangeRate) * 100);
					exchangeRate.setExchangeRate(rate);
					exchangeRate.setInversiveExchangeRate(inverseRate);
					exchangeRateCollection.addElement(exchangeRate);
					++counter;
				} catch (Exception e) {
					System.out.println(e.getMessage());
					e.printStackTrace();

				} finally {

				}
			}
			if (counter == exchangeRates.size()) {
				exchangeRateBean.updateExchangeRatePair(exchangeRates);
				eventHandler
						.publishUpdatedExchangeRates(exchangeRateCollection);
			}
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

	@Schedule(dayOfMonth = "*", dayOfWeek = "Mon-Fri", hour = "*", minute = "*", second = "*/15")
	private void refreshTradeValues() {
		updateExchangeRates();
		accountBean.updateAllTrades();
		accountBean.checkAllAccounts();
	}

	/*
	 * @Schedule(dayOfMonth = "*", dayOfWeek = "Mon-Fri", hour = "23", minute =
	 * "59", second = "0") private void deleteRecordsDB() { List<Trade>
	 * closedTrades = accountBean.getClosedTrades(); if (closedTrades.size() >
	 * 0) { for (Trade trade : closedTrades) { accountBean.removeTrade(trade); }
	 * } }
	 */

	@SuppressWarnings("unused")
	private double approximate(double value, int digits) {
		double gerundet = Math.round(value * Math.pow(10d, digits));
		return gerundet / Math.pow(10d, digits);
	}

}
