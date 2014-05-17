package ch.test.businessBeans;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import ch.test.Enum.AccountStatus;
import ch.test.entities.Account;
import ch.test.entities.ExchangeRatePair;
import ch.test.entities.Trade;
import ch.test.entities.User;

/**
 * Account Bean handles Trade and Account Entities and provides creating,
 * updating, deleting and receiving of those entities.
 *
 * @author Marc Duenki
 */
@Stateless
@LocalBean
public class AccountBean {

	@PersistenceContext(unitName = "test")
	private EntityManager entityManager;

	@Resource
	private SessionContext sessionctx;

	@EJB
	private UserBean userBean;

	@EJB
	private ExchangeRateBean exchangeRateBean;

	/**
	 * Default constructor.
	 */
	public AccountBean() {
		System.out.println("AccountBean created");
	}

	@SuppressWarnings("unchecked")
	public List<User> getRanking() {
		Query query = entityManager.createNamedQuery("ranking");
		try {
			List<User> users = query.getResultList();
			return users;
		} catch (NoResultException e) {
			return null;
		}
	}

	public Account getUsersAccount() {
		Principal p = sessionctx.getCallerPrincipal();
		Query query = entityManager.createNamedQuery("getUser");
		query.setParameter(1, p.getName());
		try {
			User users = (User) query.getSingleResult();
			return users.getAccount();
		} catch (NoResultException e) {
			return null;
		}
	}

	public void updateAccount(Trade trade, AccountStatus status) {
		Trade t1 = findTrade(trade);
		if (t1 != null) {
			Account accountToUpdate = findAccount(t1.getAccount());
			double investmentOriginCurrency = t1.getInvestmentOriginCurrency()
					/ Trade.getLeverage();
			double actualInvestmentValue = t1.getActualInvestmentValue();
			double differenceToOriginalInvestment = actualInvestmentValue
					- t1.getInvestmentTradeCurrency();
			ExchangeRatePair rateOfTrade = t1.getExchangeRatePair();
			switch (status) {
			case OPEN:
				accountToUpdate.updateBalance(-investmentOriginCurrency);
				System.out.println("Trade opened" + investmentOriginCurrency);
				break;
			case CLOSED:
				if (!isAccountCurrencyAsInvestmentCurrency(t1)) {
					ExchangeRatePair conversionRatePair = exchangeRateBean
							.getExchangeRatePair(t1.getAccount().getCurrency(),
									rateOfTrade.getCurrencyFrom());
					double conversionRateToAccountCurrency = conversionRatePair
							.getInversiveExchangeRate();
					differenceToOriginalInvestment *= conversionRateToAccountCurrency;

				}
				System.out.println("Trade closed");
				double tradingFee = investmentOriginCurrency * 2
						* Trade.getTradingFeeInPercentge();
				accountToUpdate.updateBalance(approximate(
						investmentOriginCurrency - tradingFee
								+ differenceToOriginalInvestment, 4));
				System.out.println("difference to Origignal Investment: "
						+ differenceToOriginalInvestment);
				break;
			default:
				System.out
						.println("Unknown trade-state. Not possible to update Account");
				break;
			}
			mergeAccount(accountToUpdate);
		}
	}

	public void mergeAccount(Account account) {
		Account accountToUpdate = entityManager.find(Account.class,
				account.getId());
		if (accountToUpdate != null) {
			entityManager.merge(account);
			entityManager.flush();
			entityManager.clear();
		}
	}

	public Currency getAccountCurrency() {
		return Currency.getInstance("EUR");
	}

	@SuppressWarnings("unchecked")
	private List<Account> receiveAllAccounts() {
		Query query = entityManager.createNamedQuery("receiveValidAccounts");
		try {
			List<Account> accounts = query.getResultList();
			return accounts;
		} catch (PersistenceException e) {
			return null;
		}

	}

	public void checkAllAccounts() {
		List<Account> accounts = receiveAllAccounts();
		if (accounts != null && accounts.size() > 0) {
			for (Account account : accounts) {
				List<Trade> openTradesOfAccount = getOpenTradesOfAccount(account);
				if (openTradesOfAccount != null
						&& openTradesOfAccount.size() > 0) {
					double valueOfTrades = 0.0D;
					double balance = account.getBalance();
					for (Trade trade : openTradesOfAccount) {
						valueOfTrades += getTradeValue(trade);
					}
					if (balance + valueOfTrades <= 0.2D) {
						account.setIsBlocked(true);
						mergeAccount(account);
						closeTrades(openTradesOfAccount);
					}
				}
			}
		}
	}

	private double getTradeValue(Trade trade) {
		double value = 0.0D;
		System.out.println("getTradeValue");
		Trade t1 = findTrade(trade);
		if (t1 != null) {
			double investment = trade.getInvestmentTradeCurrency()
					/ Trade.getLeverage();
			double actualInvestmentValue = approximate(
					trade.getActualInvestmentValue(), 4);
			value = approximate(
					(investment + (actualInvestmentValue - trade
							.getInvestmentTradeCurrency())),
					4);
			if (!isAccountCurrencyAsInvestmentCurrency(trade)) {
				ExchangeRatePair conversionRate = exchangeRateBean
						.getExchangeRatePair(trade.getAccount().getCurrency(),
								trade.getExchangeRatePair().getCurrencyFrom());
				value *= conversionRate.getInversiveExchangeRate();
			}
		}
		return value;
	}

	public boolean checkAccountLiquidity(double amountToBook) {
		Account accountToCheck = getUsersAccount();
		return ((accountToCheck.getBalance() - amountToBook) >= 0.00);
	}

	public boolean isAccountCurrencyAsInvestmentCurrency(Trade trade) {
		return (trade.getAccount().getCurrency().getCurrencyCode().equals(trade
				.getExchangeRatePair().getCurrencyFrom().getCurrencyCode()));
	}

	public boolean isAccountBlocked(Account account) {
		return (findAccount(account).getIsBlocked());
	}

	private Account findAccount(Account account) {
		Account a = entityManager.find(Account.class, account.getId());
		return a;

	}

	@SuppressWarnings("unchecked")
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public List<Trade> getUsersOpenTrades() {
		Principal p = sessionctx.getCallerPrincipal();
		entityManager.clear();
		Query query = entityManager.createNamedQuery("usersOpenTrades");
		entityManager.clear();
		query.setParameter(1, p.getName());
		try {
			List<Trade> trades = query.getResultList();
			return trades;
		} catch (PersistenceException e) {
			return null;
		}
	}

	public List<Trade> getUsersOpenTradesRefreshed() {
		List<Trade> openTrades = getUsersOpenTrades();
		ArrayList<Trade> refreshedTrades = new ArrayList<Trade>();
		for (Trade trade : openTrades) {
			refreshedTrades.add(findTrade(trade));
		}
		return refreshedTrades;
	}

	@SuppressWarnings("unchecked")
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public List<Trade> getOpenTradesOfAccount(Account account) {
		entityManager.clear();
		Query query = entityManager.createNamedQuery("openTradesOfAccount");
		entityManager.clear();
		query.setParameter(1, account.getId());
		try {
			List<Trade> trades = query.getResultList();
			return trades;
		} catch (PersistenceException e) {
			return null;
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
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

		if (checkAccountLiquidity(amount / Trade.getLeverage())
				&& (!isAccountBlocked(trade.getAccount()))) {
			Currency accountCurrency = getAccountCurrency();

			// If Account currency is not equal Investment currency, convert the
			// investment value
			if (!isAccountCurrencyAsInvestmentCurrency(trade)) {
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
			Trade newTrade = saveTrade(trade);
			updateAccount(trade, AccountStatus.OPEN);
			return newTrade;
		}
		return null;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	private Trade saveTrade(Trade trade) {
		entityManager.persist(trade);
		entityManager.flush();
		entityManager.clear();
		return trade;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void closeTrades(List<Trade> tradesToClose) {
		for (Trade trade : tradesToClose) {
			closeTrade(trade);
		}
	}

	// / Transaktion-Handling !!
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void closeTrade(Trade trade) {
		updateAccount(trade, AccountStatus.CLOSED);
		this.removeTrade(trade);
	}

	@SuppressWarnings("unchecked")
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public List<Trade> getOpenTrades() {
		entityManager.clear();
		String q = "Select t from " + Trade.class.getName() + " t";
		Query query = entityManager.createQuery(q).setHint(
				"org.hibernate.cacheMode", "REFRESH");
		entityManager.clear();
		try {
			List<Trade> openTrades = query.getResultList();
			return openTrades;
		} catch (PersistenceException e) {
			e.printStackTrace();
			return null;
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public double getTotalValueOfTrades() {
		double value = 0.0D;
		List<Trade> openTrades = this.getUsersOpenTrades();
		if (openTrades != null && openTrades.size() > 0) {
			for (Trade trade : openTrades) {
				value += getTradeValue(trade);
			}
		}
		return value;
	}

	// ALLENFALLS IN SERVICE BEAN AUSLAGERN DAMIT WIRKLICH NUR EINMAL AUSGEF�HRT
	// WIRD !!!
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void updateAllTrades() {
		List<Trade> openTrades = getOpenTrades();
		if (openTrades != null && openTrades.size() > 0) {
			for (Trade openTrade : openTrades) {
				updateTrade(openTrade);
				validateTrade(openTrade);
			}

		} else {
			System.out.println("openTrades null: updateAllTrades");
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	private void updateTrade(Trade trade) {
		System.out.println("updateTrade");
		// entityManager.clear();
		Trade t1 = findTrade(trade);
		if (t1 != null) {
			System.out.println("update Trade: " + t1.getId());
			ExchangeRatePair xrate = exchangeRateBean
					.findExchangeRatePair(trade.getExchangeRatePair());
			System.out.println("xrate: " + xrate.getExchangeRate());

			double newRate = xrate.getInversiveExchangeRate();
			double newInvestmentValue = (trade.getTradeValue() * newRate);
			t1.setActualInvestmentValue(newInvestmentValue);
			entityManager.flush();
			entityManager.clear();
		}
	}

	private void validateTrade(Trade openTrade) {
		ExchangeRatePair exchangeRatePair = exchangeRateBean
				.findExchangeRatePair(openTrade.getExchangeRatePair());
		if (openTrade.getStopLoss() <= exchangeRatePair.getExchangeRate()
				|| openTrade.getStopWin() >= exchangeRatePair.getExchangeRate()) {
			this.closeTrade(openTrade);
		}
	}

	public Trade findTrade(Trade trade) {
		System.out.println("findTrade");
		entityManager.clear();
		Trade tradeToFind = entityManager.find(Trade.class, trade.getId(),
				LockModeType.PESSIMISTIC_WRITE);
		/*
		 * if (entityManager.contains(tradeToFind)) {
		 * entityManager.refresh(tradeToFind, LockModeType.PESSIMISTIC_WRITE); }
		 */
		return tradeToFind;
	}

	public void removeTrade(Trade trade) {
		System.out.println("Remove");
		Trade tradeToDelete = findTrade(trade);
		if (tradeToDelete != null) {
			entityManager.remove(tradeToDelete);
			entityManager.flush();
			entityManager.clear();
		}

	}

	@SuppressWarnings("unchecked")
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public List<Trade> getClosedTrades() {
		Query query = entityManager.createNamedQuery("closedTrades");
		try {
			List<Trade> closedTrades = query.getResultList();
			return closedTrades;
		} catch (PersistenceException e) {
			return null;
		}
	}

	private double approximate(double value, int digits) {
		int gerundet = (int) Math.round(value * Math.pow(10d, digits));
		return gerundet / Math.pow(10d, digits);
	}
}
