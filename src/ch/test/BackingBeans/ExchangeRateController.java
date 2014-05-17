package ch.test.BackingBeans;

import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.push.PushContext;
import org.primefaces.push.PushContextFactory;
import ch.test.annotations.UpdatedExchangeRates;
import ch.test.businessBeans.ExchangeRateBean;
import ch.test.entities.ExchangeRatePair;
import ch.test.entityCollections.ExchangeRateCollection;

/**
 * Exchange Rate Controller that's valid for the lifetime of the application. It
 * holds a collection of ExchangeRatePairs to provide the exchange rates to
 * users. It is updated regularly with the updated exchange rates.
 * 
 * @author Marc Dünki
 */
@Named
@ApplicationScoped
public class ExchangeRateController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 241886859809224044L;

	@Inject
	private ExchangeRateBean exchangeRateBean;

	private List<ExchangeRatePair> exchangeRates;

	/**
	 * Default constructor.
	 */
	public ExchangeRateController() {
		// TODO Auto-generated constructor stub
	}

	@PostConstruct
	public void init() {
		this.exchangeRates = exchangeRateBean.getAllCrossRates();
		System.out.println("Exchange Rate Controller created");

	}

	public List<ExchangeRatePair> getExchangeRates() {
		return exchangeRates;
	}

	public void setExchangeRates(List<ExchangeRatePair> exchangeRates) {
		this.exchangeRates = exchangeRates;
	}

	public void onUpdatedExchangeRates(
			@Observes @UpdatedExchangeRates ExchangeRateCollection exchangeRates) {
		this.exchangeRates = exchangeRates.getCollection();
		PushContext pushContext = PushContextFactory.getDefault()
				.getPushContext();
		System.out.println("Push:" + pushContext.toString());
		pushContext.push("/rates", "Updated Exchange Rates");
	}
}
