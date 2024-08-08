package com.fuse.unittests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.struts2.StrutsJUnit4TestCase;
import org.junit.Test;

import com.fuse.actions.Login;
import com.fuse.actions.remediation.OpenVulns;
import com.fuse.dao.User;
import com.opensymphony.xwork2.ActionProxy;

public class OpenVulnsTest extends StrutsJUnit4TestCase {

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
	public void test() throws Exception {
		User u = login("admin", "password123");
		ActionProxy proxy = getActionProxy("/portal/OpenVulns");
		OpenVulns ov = (OpenVulns) proxy.getAction();
		ov.setAppId("");
		ov.setAppname("");
		ov.setTracking("");
		ov.setOpen("true");
		ov.setClosed("true");
		ov.setLength(50);
		ov.setStart(0);
		for (int i = 0; i < 6; i++) {
			ov.getRisk().add("true");
		}
		ov.setAction("get");

		String result = proxy.execute();
		System.out.println("Count: " + ov.getCount());
		System.out.println(ov.getCombos().size());

		proxy = getActionProxy("/portal/OpenVulns");
		ov = (OpenVulns) proxy.getAction();
		ov.setAppId("");
		ov.setAppname("");
		ov.setTracking("");
		ov.setOpen("true");
		ov.setClosed("true");
		ov.setLength(50);
		ov.setStart(50);
		for (int i = 0; i < 6; i++) {
			ov.getRisk().add("true");
		}
		ov.setAction("get");

		result = proxy.execute();
		System.out.println("Count: " + ov.getCount());
		System.out.println(ov.getCombos().size());

		proxy = getActionProxy("/portal/OpenVulns");
		ov = (OpenVulns) proxy.getAction();
		ov.setAppId("");
		ov.setAppname("");
		ov.setTracking("");
		ov.setOpen("true");
		ov.setClosed("true");
		ov.setLength(50);
		ov.setStart(0);
		for (int i = 0; i < 6; i++) {
			ov.getRisk().add("true");
		}
		ov.setAction("get");

		result = proxy.execute();
		System.out.println("Count: " + ov.getCount());
		System.out.println(ov.getCombos().size());

		proxy = getActionProxy("/portal/OpenVulns");
		ov = (OpenVulns) proxy.getAction();
		ov.setAppId("");
		ov.setAppname("");
		ov.setTracking("");
		ov.setOpen("true");
		ov.setClosed("true");
		ov.setTracking("123track");
		ov.setLength(50);
		ov.setStart(0);
		for (int i = 0; i < 6; i++) {
			ov.getRisk().add("true");
		}
		ov.setAction("get");

		result = proxy.execute();
		System.out.println("Count: " + ov.getCount());
		System.out.println(ov.getCombos().size());

	}

}
