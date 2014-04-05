package ch.test.BackingBeans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.event.CellEditEvent;

import ch.test.businessBeans.ExchangeRateBean;
import ch.test.businessBeans.UserBean;
import ch.test.entities.ExchangeRatePair;
import ch.test.entities.User;

/**
 * Session Bean implementation class LandingController
 */
@Named
@SessionScoped
public class AdminController implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7897818691161388094L;

	@Inject
	UserBean userBean;
	
	@Inject
	ExchangeRateBean exchangeRateBean;

	private List<User> users;
	private User user;
	private ExchangeRatePair exchangeRatePair;
	private List<ExchangeRatePair> exchangeRates;

    /**
     * Default constructor. 
     */
    public AdminController() {
        users = new ArrayList<User>();
        user = new User();
        exchangeRatePair = new ExchangeRatePair();
        System.out.println("AdminController created");
    }
    
	@PostConstruct
	public void init() {
		this.users = userBean.retrieveAllUsers();
		this.exchangeRates = exchangeRateBean.getAllCrossRates();
	}
    
    public void onCellEdit(CellEditEvent event){
		String oldValue = (String) event.getOldValue();
		String newValue = (String) event.getNewValue();
		
		FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
				"Cell Changed", "Old value: " + oldValue + ", New Value:"
						+ newValue);
		FacesContext.getCurrentInstance().addMessage(null, msg);
		
    }

    public void deleteUser(User user){
    	userBean.deleteUser(user);
    	this.users.remove(user);
    }
    
    public void setUser(User user){
    	this.user=user;
    }
    
    public User getUser(){
    	return this.user;
    }
    
    public void setUsers(List<User> users){
    	this.users=users;
    }
    
    public List<User> getUsers(){
    	return this.users;
    }
    
    public void saveUser(){
    	userBean.saveUser(this.user);
    	this.users.add(user);
    	this.user=new User();
    }

	public ExchangeRatePair getExchangeRatePair() {
		return exchangeRatePair;
	}

	public void setExchangeRatePair(ExchangeRatePair exchangeRatePair) {
		this.exchangeRatePair = exchangeRatePair;
	}

	public List<ExchangeRatePair> getExchangeRates() {
		return exchangeRates;
	}

	public void setExchangeRates(List<ExchangeRatePair> exchangeRates) {
		this.exchangeRates = exchangeRates;
	}
    
    public void saveExchangeRatePair() {
    	// Check Currency Strings
    	
    	exchangeRateBean.saveExchangeRatePair(exchangeRatePair);
    	this.exchangeRates.add(exchangeRatePair);
    }

}
