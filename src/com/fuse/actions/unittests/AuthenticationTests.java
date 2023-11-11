package com.fuse.actions.unittests;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.StrutsJUnit4TestCase;
import org.apache.struts2.StrutsTestCase;
import org.apache.struts2.dispatcher.HttpParameters;
import org.apache.struts2.dispatcher.Parameter;
import org.junit.Test;

import com.fuse.actions.Login;
import com.fuse.dao.User;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionProxy;

public class AuthenticationTests extends  StrutsJUnit4TestCase {
	

	
	@Test
	public void testLoginAsseessorSuccessTest() throws Exception {
	
		ActionProxy proxy = getActionProxy("/Login.action");
		Login login = (Login) proxy.getAction();
	    login.setUsername("admin");
	    login.setPassword("password");
	    String result = proxy.execute();
	    assertEquals("Did not Redirect to assesment Queue" , "assessorQueue", result);
	    User u = (User) request.getSession().getAttribute("user");
	    assertNotNull("User is not Null", u);
	    assertEquals("Not the right user","admin", u.getUsername());
	    
	
		
	}
	
	
	
	

}
