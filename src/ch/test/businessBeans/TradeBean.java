package ch.test.businessBeans;

import java.security.Principal;
import java.util.Currency;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import ch.test.entities.Account;
import ch.test.entities.ExchangeRatePair;
import ch.test.entities.Trade;
import ch.test.entities.User;

/**
 * Session Bean implementation class TradeBean
 */
@Stateless
@LocalBean
public class TradeBean {

	@PersistenceContext(unitName = "test")
	private EntityManager entityManager;

	@Resource
	private SessionContext sessionctx;

	@EJB
	private AccountBean accountBean;
	@EJB
	private UserBean userBean;
	@EJB
	private ExchangeRateBean exchangeRateBean;

	/**
	 * Default constructor.
	 */
	public TradeBean() {
		System.out.println("TradeBean created");
	}

	@SuppressWarnings("unchecked")
	public List<Trade> getUsersOpenTrades() {
		Principal p = sessionctx.getCallerPrincipal();
		Query query = entityManager.createNamedQuery("usersOpenTrades");
		query.setParameter(1, p.getName());
		try {
			List<Trade> trades = query.getResultList();
			return trades;
		} catch (PersistenceException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public List<Trade> getOpenTradesOfAccount(Account account) {
		Query query = entityManager.createNamedQuery("openTradesOfAccount");
		query.setParameter(1, account.getId());
		try {
			List<Trade> trades = query.getResultList();
			return trades;
		} catch (PersistenceException e) {
			return null;
		}
	}

	public Trade openTrade(ExchangeRatePair exchangeRatePair, double amount,
			double stopLoss, double stopWin) {

		Trade trade = new Trade();
		User user = userBean.getUser();
		ExchangeRatePair exchangeRate = exchangeRateBean
				.findExchangeRatePair(exchangeRatePair);
		trade.setExchangeRatePair(exchangeRate);
		trade.setExchangeRateOnBuy(exchangeRate.getExchangeRate());
		trade.setAccount(user.getAccount());
		trade.setStopLoss(stopLoss);
		trade.setStopWin(stopWin);
		double investmentTradeCurrency = amount;
		double investmentOriginCurrency = amount;

		if (accountBean.checkAccountLiquidity(amount / Trade.getLeverage())
				&& (!accountBean.isAccountBlocked(trade.getAccount()))) {
			Currency accountCurrency = accountBean.getAccountCurrency();

			// If Account currency is not equal Investment currency, convert the
			// investment value
			if (!accountBean.isAccountCurrencyAsInvestmentCurrency(trade)) {
				ExchangeRatePair conversionRatePair = exchangeRateBean
						.getExchangeRatePair(accountCurrency,
								exchangeRate.getCurrencyFrom());
				double conversionRate = conversionRatePair.getExchangeRate();
				investmentTradeCurrency *= conversionRate;
			}
			trade.setInvestmentOriginCurrency(investmentOriginCurrency);
			trade.setInvestmentTradeCurrency(investmentTradeCurrency);
			trade.setActualInvestmentValue(investmentTradeCurrency);
			trade.setTradeValue(investmentTradeCurrency
					* exchangeRate.getExchangeRate());
			saveTrade(trade);
			accountBean.updateBalance(trade);
			return trade;
		}
		return null;
	}

	private void saveTrade(Trade trade) {
		entityManager.persist(trade);
	}

	public void closeTrades(List<Trade> tradesToClose) {
		for (Trade trade : tradesToClose) {
			closeTrade(trade);
		}
	}
	/// Transaktion-Handling !!
	public void closeTrade(Trade trade) {
		Trade tradeToClose = findTrade(trade);
		tradeToClose.setClosed(true);
		updateTrade(tradeToClose);
		accountBean.updateBalance(tradeToClose);
	}

	@SuppressWarnings("unchecked")
	public List<Trade> getOpenTrades() {
		Query query = entityManager.createNamedQuery("openTrades");
		try {
			List<Trade> openTrades = query.getResultList();
			return openTrades;
		} catch (PersistenceException e) {
			return null;
		}
	}

	public void updateTrade(Trade trade) {
		Trade tradeToUpdate = entityManager.find(Trade.class, trade.getId());
		if (tradeToUpdate != null) {
			entityManager.merge(trade);
		}
	}

	// ALLENFALLS IN SERVICE BEAN AUSLAGERN DAMIT WIRKLICH NUR EINMAL AUSGEF†HRT
	// WIRD !!!
	public void updateAllTrades() {
		List<ExchangeRatePair> exchangeRates = exchangeRateBean
				.getAllCrossRates();
		List<Trade> openTrades = getOpenTrades();
		for (ExchangeRatePair crossrate : exchangeRates) {
			for (Trade openTrade : openTrades) {
				if (openTrade.getExchangeRatePair().getId() == crossrate
						.getId()) {
					double newRate = crossrate.getInversiveExchangeRate();
					double actualInvestmentValue = openTrade
							.getActualInvestmentValue();
					double newInvestmentValue = (openTrade.getTradeValue() * newRate);
					openTrade.setDifference(newInvestmentValue
							- actualInvestmentValue);
					openTrade.setActualInvestmentValue(newInvestmentValue);
					openTrade.setBooked(true);
					validateTrade(openTrade, crossrate);
				}
			}
		}
	}

	private void validateTrade(Trade openTrade, ExchangeRatePair crossRate) {
		ExchangeRatePair exchangeRatePair = exchangeRateBean
				.findExchangeRatePair(crossRate);
		if (openTrade.getStopLoss() <= exchangeRatePair.getExchangeRate()
				|| openTrade.getStopWin() >= exchangeRatePair.getExchangeRate()) {
			accountBean.updateBalance(openTrade);
			openTrade.setClosed(true);
			accountBean.updateBalance(openTrade);
		}
		updateTrade(openTrade);
	}

	public Trade findTrade(Trade trade) {
		Trade tradeToFind = entityManager.find(Trade.class, trade.getId());
		return tradeToFind;
	}

	public void removeTrade(Trade trade) {
		Trade tradeToDelete = findTrade(trade);
		if(tradeToDelete != null){
			entityManager.remove(tradeToDelete);
		}
		
	}

	@SuppressWarnings("unchecked")
	public List<Trade> getClosedTrades() {
		Query query = entityManager.createNamedQuery("closedTrades");
		try {
			List<Trade> closedTrades = query.getResultList();
			return closedTrades;
		} catch (PersistenceException e) {
			return null;
		}
	}

}
