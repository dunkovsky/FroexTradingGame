package ch.test.webservice;

public class CurrencyConvertorSoapProxy implements ch.test.webservice.CurrencyConvertorSoap {
  private String _endpoint = null;
  private ch.test.webservice.CurrencyConvertorSoap currencyConvertorSoap = null;
  
  public CurrencyConvertorSoapProxy() {
	  _endpoint= "http://webservicex.com/CurrencyConvertor.asmx?wsdl";
    _initCurrencyConvertorSoapProxy();
  }
  
  public CurrencyConvertorSoapProxy(String endpoint) {
    _endpoint = endpoint;
    _initCurrencyConvertorSoapProxy();
  }
  
  private void _initCurrencyConvertorSoapProxy() {
    try {
      currencyConvertorSoap = (new ch.test.webservice.CurrencyConvertorLocator()).getCurrencyConvertorSoap();
      if (currencyConvertorSoap != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)currencyConvertorSoap)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)currencyConvertorSoap)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (currencyConvertorSoap != null)
      ((javax.xml.rpc.Stub)currencyConvertorSoap)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public ch.test.webservice.CurrencyConvertorSoap getCurrencyConvertorSoap() {
    if (currencyConvertorSoap == null)
      _initCurrencyConvertorSoapProxy();
    return currencyConvertorSoap;
  }
  
  public double conversionRate(ch.test.webservice.Currency fromCurrency, ch.test.webservice.Currency toCurrency) throws java.rmi.RemoteException{
    if (currencyConvertorSoap == null)
      _initCurrencyConvertorSoapProxy();
    return currencyConvertorSoap.conversionRate(fromCurrency, toCurrency);
  }
  
  
}