package com.fuse.unittests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.apache.struts2.StrutsJUnit4TestCase;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import com.fuse.actions.AjaxServices;
import com.fuse.actions.Login;
import com.fuse.dao.User;
import com.opensymphony.xwork2.ActionProxy;

public class TestAjax extends StrutsJUnit4TestCase {

	public User login(String username, String password) {
		try {
			ActionProxy proxy = getActionProxy("/Login.action");
			Login login = (Login) proxy.getAction();
			login.setUsername(username);
			login.setPassword(password);
			String result;

			result = proxy.execute();

			assertEquals("Did not Redirect to assesment Queue", "assessorQueue", result);
			User u = (User) request.getSession().getAttribute("user");
			assertNotNull("User is not Null", u);
			assertEquals("Not the right user", username, u.getUsername());
			return u;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Test
	public void getAssessments() throws Exception {
		User u = login("admin", "password123");
		ActionProxy proxy = getActionProxy("/services/getAssessments");
		AjaxServices as = (AjaxServices) proxy.getAction();
		assertNotNull("AjaxServices Action is null", as);

		String result = proxy.execute();
		assertEquals("Did not return a json response", as.JSON, result);
		String stream = response.getContentAsString();
		System.out.println(stream);

		assertNotNull(stream);
		// assertNotEquals("",stream);
		JSONParser parse = new JSONParser();
		try {
			parse.parse(stream);
		} catch (ParseException e) {

			e.printStackTrace();
			fail("Not Valid Json");
		}

	}

	@Test
	public void getVerifications() throws Exception {
		User u = login("admin", "password123");
		ActionProxy proxy = getActionProxy("/services/getVerifications");
		AjaxServices as = (AjaxServices) proxy.getAction();
		assertNotNull("AjaxServices Action is null", as);

		String result = proxy.execute();
		assertEquals("Did not return a json response", as.JSON, result);
		String stream = response.getContentAsString();
		System.out.println(stream);

		assertNotNull(stream);
		// assertNotEquals("",stream);
		JSONParser parse = new JSONParser();
		try {
			parse.parse(stream);
		} catch (ParseException e) {

			e.printStackTrace();
			fail("Not Valid Json");
		}

	}

}
