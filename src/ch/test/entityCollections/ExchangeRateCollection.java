package ch.test.entityCollections;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import ch.test.entities.ExchangeRatePair;

public class ExchangeRateCollection implements Serializable {

	/**
	 * Provides a Collection of ExchangeRatePair entities and therefore services
	 * as adding, deleting and receiving of objects.
	 * 
	 * @author Marc Dünki
	 */
	private static final long serialVersionUID = 8624929255090231895L;

	private List<ExchangeRatePair> exchangeRates;

	public ExchangeRateCollection() {
		this.exchangeRates = new ArrayList<ExchangeRatePair>();
	}

	public List<ExchangeRatePair> getCollection() {
		return this.exchangeRates;
	}

	public void addElement(ExchangeRatePair exchangeRate) {
		this.exchangeRates.add(exchangeRate);
	}

	public ExchangeRatePair getExchangeRatePair(int index) {
		return this.exchangeRates.get(index);
	}

	public void removeExchangeRatePair(int index) {
		this.exchangeRates.remove(index);
	}

	public ExchangeRatePair getExchangeRatePair(Currency currencyFrom,
			Currency currencyTo) {
		ExchangeRatePair exrate = null;
		for (ExchangeRatePair xrate : this.exchangeRates) {
			if (xrate.getCurrencyFrom() == currencyFrom
					&& xrate.getCurrencyTo() == currencyTo) {
				exrate = xrate;
			}
		}
		return exrate;
	}

}
