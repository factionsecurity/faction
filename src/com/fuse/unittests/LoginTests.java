package com.fuse.unittests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.struts2.StrutsJUnit4TestCase;
import org.junit.Test;

import com.fuse.actions.Login;
import com.fuse.dao.User;
import com.opensymphony.xwork2.ActionProxy;

public class LoginTests extends StrutsJUnit4TestCase {

	public User login(String username, String password) throws Exception {

		ActionProxy proxy = getActionProxy("/Login.action");
		Login login = (Login) proxy.getAction();
		login.setUsername(username);
		login.setPassword(password);
		String result = proxy.execute();
		assertEquals("Did not Redirect to assesment Queue", "assessorQueue", result);
		User u = (User) request.getSession().getAttribute("user");
		assertNotNull("User is not Null", u);
		assertEquals("Not the right user", username, u.getUsername());
		return u;
	}

	@Test
	public void test() {
		try {
			User u = login("admin", "password123");
			System.out.println(u.getFname());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
