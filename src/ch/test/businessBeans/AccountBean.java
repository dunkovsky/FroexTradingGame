package ch.test.businessBeans;

import java.security.Principal;
import java.util.Currency;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
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
 * Session Bean implementation class AccountBean
 */
@Stateless
@LocalBean
public class AccountBean {

	@PersistenceContext(unitName = "test")
	private EntityManager entityManager;

	@Resource
	private SessionContext sessionctx;

	@EJB
	private TradeBean tradeBean;

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

	public void updateAllAccounts() {
		List<Trade> openTrades = tradeBean.getOpenTrades();
		if (openTrades.size() > 0) {
			for (Trade openTrade : openTrades) {
				updateBalance(openTrade);
			}
		}
	}

	public void updateBalance(Trade trade) {
		Account accountToUpdate = findAccount(trade.getAccount());
		double investmentOriginCurrency = trade.getInvestmentOriginCurrency()
				/ Trade.getLeverage();
		double actualInvestmentValue = trade.getActualInvestmentValue();
		double differenceToOriginalInvestment = actualInvestmentValue
				- trade.getInvestmentTradeCurrency();
		double changeOfInvestment = trade.getDifference();
		ExchangeRatePair rateOfTrade = trade.getExchangeRatePair();

		if (!isAccountCurrencyAsInvestmentCurrency(trade)) {
			ExchangeRatePair conversionRatePair = exchangeRateBean
					.getExchangeRatePair(trade.getAccount().getCurrency(),
							rateOfTrade.getCurrencyFrom());
			double conversionRateToAccountCurrency = conversionRatePair
					.getInversiveExchangeRate();
			changeOfInvestment *= conversionRateToAccountCurrency;
			differenceToOriginalInvestment *= conversionRateToAccountCurrency;

		}
		// Wenn Trade geschlossen wird, wird der Betrag dem Konto gutgeschrieben
		// und dem Margin-Konto abgezogen
		if (trade.isClosed()) {
			// Falls der Trade vor dem Schliessen durch
			double investmentValueAfterTradeFee = investmentOriginCurrency
					* (1 - (2 * Trade.getTradingFeeInPercentge()) / 100);
			accountToUpdate.updateBalance(investmentValueAfterTradeFee
					+ differenceToOriginalInvestment);
			accountToUpdate
					.updateMarginBalance(-(investmentOriginCurrency + differenceToOriginalInvestment));
			System.out.println("difference to Origignal Investment: "
					+ differenceToOriginalInvestment);
			System.out.println("Margin updated: - " + investmentOriginCurrency
					+ differenceToOriginalInvestment);
		}
		// Wenn der Trade gešffnet worden ist, wird der Betrag dem Konto
		// abgebucht und dem Margin-Konto aufgerechnet
		else if ((!trade.isClosed()) && (!trade.isBooked())) {
			accountToUpdate.updateBalance(-investmentOriginCurrency);
			accountToUpdate.updateMarginBalance(investmentOriginCurrency);
			// Guthaben updaten
		} else if ((!trade.isClosed()) && (trade.isBooked())) {
			accountToUpdate.updateMarginBalance(changeOfInvestment);
			System.out.println("Margin updated: " + changeOfInvestment);
		}
		updateAccount(accountToUpdate);
	}

	public void updateAccount(Account account) {
		Account accountToUpdate = entityManager.find(Account.class,
				account.getId());
		if (accountToUpdate != null) {
			entityManager.merge(account);
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
		if (accounts.size() > 0) {
			for (Account account : accounts) {
				List<Trade> openTradesOfAccount = tradeBean
						.getOpenTradesOfAccount(account);
				if (openTradesOfAccount.size() > 0) {
					double valueOfTrades = 0.0D;
					double balance = account.getBalance();
					for (Trade trade : openTradesOfAccount) {
						double investment = trade.getInvestmentTradeCurrency()
								/ Trade.getLeverage();
						double actualInvestmentValue = trade
								.getActualInvestmentValue();
						valueOfTrades += (investment + (actualInvestmentValue - trade
								.getInvestmentTradeCurrency()));
						if (!isAccountCurrencyAsInvestmentCurrency(trade)) {
							ExchangeRatePair conversionRate = exchangeRateBean
									.getExchangeRatePair(trade.getAccount()
											.getCurrency(), trade
											.getExchangeRatePair()
											.getCurrencyFrom());
							valueOfTrades *= conversionRate
									.getInversiveExchangeRate();
						}
					}
					if (balance + valueOfTrades < 0.0) {
						account.setIsBlocked(true);
						updateAccount(account);
						tradeBean.closeTrades(openTradesOfAccount);
					}
				}
			}
		}
	}

	public boolean checkAccountLiquidity(double amountToBook) {
		Account accountToCheck = getUsersAccount();
		return ((accountToCheck.getBalance() - amountToBook) >= 0.0);
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
}
