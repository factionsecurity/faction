package com.fuse.actions.unittests;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;

import org.apache.struts2.StrutsJUnit4TestCase;
import org.apache.struts2.StrutsSpringJUnit4TestCase;
import org.apache.struts2.StrutsTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.fuse.actions.Login;
import com.fuse.dao.HibHelper;
import com.fuse.dao.User;
import com.fuse.utils.AccessControl;
import com.opensymphony.xwork2.ActionProxy;

public class AuthenticationTests extends StrutsTestCase {
	
	private EntityManager em;
	private User user;
	
	
	@Test
	public void testLoginAsseessorSuccessTest() throws Exception {
		em = HibHelper.getInstance().getEMF().createEntityManager();
		user = new User();
		user.setUsername("admin");
		user.setPasshash(AccessControl.HashPass("admin", "password"));
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		em.persist(user);
		HibHelper.getInstance().commit();
		
		ActionProxy proxy = getActionProxy("/Login.action");
		Login login = (Login) proxy.getAction();
	    login.setUsername("admin");
	    login.setPassword("password");
	    String result = proxy.execute();
	    assertEquals("Did not Redirect to assesment Queue" , "assessorQueue", result);
	    User u = (User) request.getSession().getAttribute("user");
	    assertNotNull("User is not Null", u);
	    assertEquals("Not the right user","admin", u.getUsername());
		em.close();
	    
	}

}
