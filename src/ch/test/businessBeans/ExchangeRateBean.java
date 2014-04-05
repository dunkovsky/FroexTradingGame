package ch.test.businessBeans;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.LinkedList;
import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import ch.test.entities.ExchangeRatePair;
import ch.test.entities.Trade;
import ch.test.entities.User;

/**
 * Session Bean implementation class ExchangeRateBean
 */
@Stateless
@LocalBean
public class ExchangeRateBean {

	@PersistenceContext(unitName = "test")
	private EntityManager entityManager;

	/**
	 * Default constructor.
	 */
	public ExchangeRateBean() {
		// TODO Auto-generated constructor stub
	}

	public void updateExchangeRatePair(ExchangeRatePair exchangeRatePair) {
		ExchangeRatePair exchangeRatePairToUpdate = entityManager.find(ExchangeRatePair.class, exchangeRatePair.getId());
		if (exchangeRatePairToUpdate != null) {
			System.out.println(exchangeRatePair.getExchangeRate());
			entityManager.merge(exchangeRatePair);
		}
	}

	@SuppressWarnings("unchecked")
	public List<ExchangeRatePair> getAllCrossRates() {
		String q = "SELECT e from " + ExchangeRatePair.class.getName() + " e";
		Query query = entityManager.createQuery(q);
		try {
			List<ExchangeRatePair> rates = query.getResultList();
			return rates;
		} catch (PersistenceException e) {
			System.out.println("getAllCrossRates Exception");
			return null;
		}

	}
		
	public ExchangeRatePair getExchangeRatePair(Currency currencyFrom, Currency currencyTo){		
		Query query = entityManager.createNamedQuery("getCrossRatePair");
		query.setParameter(1, currencyFrom);
		query.setParameter(2, currencyTo);
		try {
			ExchangeRatePair crossRate = (ExchangeRatePair) query.getSingleResult();
			return crossRate;
		} catch (NoResultException e) {
			return null;
		}
	}
	
	public void saveExchangeRatePair(ExchangeRatePair exchangeRatePair){
		entityManager.persist(exchangeRatePair);
	}
	
	public ExchangeRatePair findExchangeRatePair(ExchangeRatePair exchangeRate) {
		return entityManager.find(ExchangeRatePair.class, exchangeRate.getId());
	}

}
