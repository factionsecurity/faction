package com.fuse.unittests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;

import org.apache.struts2.StrutsJUnit4TestCase;
import org.junit.Test;

import com.fuse.actions.Login;
import com.fuse.actions.scheduling.Engagement;
import com.fuse.dao.Assessment;
import com.fuse.dao.User;
import com.opensymphony.xwork2.ActionProxy;

public class EngagementTests extends StrutsJUnit4TestCase {

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
	public void t1TestAssessmentSearching() throws Exception {
		login("admin", "password123");
		ActionProxy proxy = getActionProxy("/portal/Engagement.action");
		Engagement eng = (Engagement) proxy.getAction();

		eng.setAction("search");
		eng.setEngId(2);

		String result = proxy.execute();
		assertEquals("searchJSON", result);
		assertNotNull(eng.getAssessments());
		// assertNotEquals(0, eng.getAssessments().size());
		for (Assessment a : eng.getAssessments())
			System.out.println(a.getName() + ": " + a.getEngagement().getUsername());

		proxy = getActionProxy("/portal/Engagement.action");
		eng = (Engagement) proxy.getAction();

		eng.setAction("search");
		eng.setAssessorId(new ArrayList<Integer>());
		eng.getAssessorId().add(2);

		result = proxy.execute();
		assertEquals("searchJSON", result);
		assertNotNull(eng.getAssessments());
		// assertNotEquals(0, eng.getAssessments().size());
		for (Assessment a : eng.getAssessments())
			System.out.println(a.getName() + ": " + a.getEngagement().getUsername());

		proxy = getActionProxy("/portal/Engagement.action");
		eng = (Engagement) proxy.getAction();

		eng.setAction("search");
		eng.setAppid("264176");

		result = proxy.execute();
		assertEquals("searchJSON", result);
		assertNotNull(eng.getAssessments());
		// assertNotEquals(0, eng.getAssessments().size());
		for (Assessment a : eng.getAssessments())
			System.out.println(a.getName() + ": " + a.getEngagement().getUsername());

		proxy = getActionProxy("/portal/Engagement.action");
		eng = (Engagement) proxy.getAction();

		eng.setAction("search");
		eng.setAppName("Test");

		result = proxy.execute();
		assertEquals("searchJSON", result);
		assertNotNull(eng.getAssessments());
		// assertNotEquals(0, eng.getAssessments().size());
		for (Assessment a : eng.getAssessments())
			System.out.println(a.getName() + ": " + a.getEngagement().getUsername());

		proxy = getActionProxy("/portal/Engagement.action");
		eng = (Engagement) proxy.getAction();

		eng.setAction("search");

		result = proxy.execute();
		assertEquals("searchJSON", result);
		assertNotNull(eng.getAssessments());
		// assertNotEquals(0, eng.getAssessments().size());
		for (Assessment a : eng.getAssessments())
			System.out.println(a.getName() + ": " + a.getEngagement().getUsername());

	}

}
