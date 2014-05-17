package ch.test.eventHandler;

import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import ch.test.annotations.UpdatedExchangeRates;
import ch.test.entityCollections.ExchangeRateCollection;

/**
 * Represents an Event Handler that fires events across the application whenever
 * needed.
 * 
 * @author Marc Dünki
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