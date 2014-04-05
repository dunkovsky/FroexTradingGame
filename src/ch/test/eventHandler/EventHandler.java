package ch.test.eventHandler;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import ch.test.annotations.UpdatedExchangeRates;
import ch.test.entities.ExchangeRatePair;
import ch.test.entityCollections.ExchangeRateCollection;

/**
 * 
 * @author Marc DŸnki
 * 
 */

@LocalBean
@Singleton
public class EventHandler {

	@Inject
	@UpdatedExchangeRates
	private Event<ExchangeRateCollection> exchangeRatesNotifier;

	public void publishUpdatedExchangeRates(
			ExchangeRateCollection updatedExchangeRates) {
		this.exchangeRatesNotifier.fire(updatedExchangeRates);
	}
}