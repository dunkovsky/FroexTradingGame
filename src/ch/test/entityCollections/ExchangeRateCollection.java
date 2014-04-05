package ch.test.entityCollections;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import ch.test.entities.ExchangeRatePair;

public class ExchangeRateCollection implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8624929255090231895L;
	
	private List<ExchangeRatePair> exchangeRates;
	
	public ExchangeRateCollection(){
		this.exchangeRates = new ArrayList<ExchangeRatePair>();
	}
	
	public List<ExchangeRatePair> getCollection(){
		return this.exchangeRates;
	}
	
	public void addElement(ExchangeRatePair exchangeRate) {
		this.exchangeRates.add(exchangeRate);
	}
	
	public ExchangeRatePair getExchangeRatePair(int index){
		return this.exchangeRates.get(index);
	}

}
