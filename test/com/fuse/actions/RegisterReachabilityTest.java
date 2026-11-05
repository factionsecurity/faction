package com.fuse.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.struts2.junit.StrutsJUnit4TestCase;
import org.junit.Test;

import com.opensymphony.xwork2.ActionProxy;

/**
 * Password reset and invitation links are opened by people without a session,
 * so Register must live in the root namespace, the only one (besides /sso) the
 * AccessControlInterceptor lets through unauthenticated. It sat in /portal from
 * 1.8.8, which redirected every emailed link to the login page.
 */
public class RegisterReachabilityTest extends StrutsJUnit4TestCase<Register> {

	@Test
	public void registerIsMappedInTheRootNamespace() throws Exception {
		ActionProxy proxy = getActionProxy("/Register");
		assertNotNull("/Register must resolve", proxy);
		assertEquals("/", proxy.getNamespace());
		assertEquals(Register.class, proxy.getAction().getClass());
	}
}
