package ch.test.businessBeans;

import java.security.Principal;
import java.sql.Date;
import java.util.Calendar;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import ch.test.entities.Account;
import ch.test.entities.User;

/**
 * Session Bean implementation class UserBean
 */
@Stateless
@LocalBean
public class UserBean implements UserBeanRemote {

	@PersistenceContext(unitName = "test")
	private EntityManager entityManager;

	@Resource
	private SessionContext sessionctx;

	@EJB
	AccountBean accountBean;

	/**
	 * Default constructor.
	 */
	public UserBean() {
		// TODO Auto-generated constructor stub

	}

	@RolesAllowed(value = { "trader" })
	@SuppressWarnings("unchecked")
	public List<User> retrieveAllUsers() {
		System.out.println(sessionctx.isCallerInRole("admin"));
		Principal p = sessionctx.getCallerPrincipal();
		System.out.println(p.getName());
		String q = "SELECT u from " + User.class.getName() + " u";
		Query query = entityManager.createQuery(q);
		try {
			List<User> users = query.getResultList();
			return users;
		} catch (PersistenceException e) {
			return null;
		}
	}

	@RolesAllowed(value = { "trader" })
	public boolean userExists(User user) {
		Query query = entityManager.createNamedQuery("userExists");
		query.setParameter(1, user.getUsername());
		query.setParameter(2, user.getEmail());
		try {
			return (User) query.getSingleResult() != null;
		} catch (NoResultException e) {
			return false;
		}
	}

	@RolesAllowed(value = { "trader" })
	public void deleteUser(User user) {
		User userToDelete = entityManager.find(User.class, user.getId());
		Account accountToDelete = entityManager.find(Account.class, user
				.getAccount().getId());
		if (userToDelete != null) {
			entityManager.remove(accountToDelete);
			entityManager.remove(userToDelete);
		}
	}

	@RolesAllowed(value = { "trader" })
	public void updateUser(User user) {
		User userToUpdate = entityManager.find(User.class, user.getId());
		if (userToUpdate != null) {
			entityManager.merge(user);
		}
	}

	@RolesAllowed(value = { "trader" })
	public int saveUser(User user) {
		Account account = new Account();
		account.setCurrency(Currency.getInstance(accountBean
				.getAccountCurrency().getCurrencyCode()));
		entityManager.persist(account);
		user.setCreated(new Date(Calendar.getInstance().getTimeInMillis())
				.toString());
		user.setAccount(account);
		entityManager.persist(user);
		return user.getId();
	}

	public User getUser() {
		Principal p = sessionctx.getCallerPrincipal();
		Query query = entityManager.createNamedQuery("getUser");
		query.setParameter(1, p.getName());
		try {
			return (User) query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}
}
