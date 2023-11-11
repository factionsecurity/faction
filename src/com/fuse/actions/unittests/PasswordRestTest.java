package com.fuse.actions.unittests;

import static org.junit.Assert.*;

import org.apache.struts2.StrutsJUnit4TestCase;
import org.junit.Test;

import com.fuse.actions.Login;
import com.fuse.dao.User;
import com.opensymphony.xwork2.ActionProxy;

public class PasswordRestTest extends  StrutsJUnit4TestCase{

	@Test
	public void resetPasstest() throws Exception {
		ActionProxy proxy = getActionProxy("/reset.action");
		Login login = (Login) proxy.getAction();
	    login.setUsername("test");
	    String result = proxy.execute();
	    System.out.println(result);
	}

}
