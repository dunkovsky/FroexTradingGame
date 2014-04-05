package ch.test.businessBeans;

import java.util.List;

import ch.test.entities.User;

public interface UserBeanRemote {
	
	List<User> retrieveAllUsers();
	boolean userExists(User user);
	void deleteUser(User user);
	void updateUser(User user);
	int saveUser(User user);

}
