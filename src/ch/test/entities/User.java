package ch.test.entities;

import java.io.Serializable;
import java.util.Set;

import javax.ejb.Stateless;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Session Bean implementation class User
 */
@Entity(name = "user")
@Table(name = "user")
@NamedQueries({
		@NamedQuery(name = "ranking", query = "SELECT u.username, u.account.balance FROM user u ORDER BY u.account.balance DESC"),
		@NamedQuery(name = "getUser", query = "SELECT u FROM user u WHERE u.username =?1") })
@Stateless
public class User implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4318894675555439959L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	private String username;
	private String email;
	private String password;
	private String accountCreated;
	@OneToOne
	protected Account account;

	/**
	 * Default constructor.
	 */
	public User() {
		// this.account=new Account();
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCreated() {
		return accountCreated.toString();
	}

	public void setCreated(String created) {
		this.accountCreated = created;
	}

	public String getAccountCreated() {
		return accountCreated;
	}

	public void setAccountCreated(String accountCreated) {
		this.accountCreated = accountCreated;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

}
