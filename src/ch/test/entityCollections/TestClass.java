package ch.test.entityCollections;

import java.rmi.RemoteException;

import ch.test.webservice.Currency;
import ch.test.webservice.CurrencyConvertorSoapProxy;

public class TestClass {

	public static void main(String[] args) {
		
		CurrencyConvertorSoapProxy proxy = new CurrencyConvertorSoapProxy();
		double rate;
		try {
			rate = proxy.conversionRate(new Currency("JPY"), new Currency("XAU"));
			System.out.println(rate);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO Auto-generated method stub

	}

}
