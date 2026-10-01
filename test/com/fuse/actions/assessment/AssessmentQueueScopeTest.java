package com.fuse.actions.assessment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.fuse.dao.Permissions;
import com.fuse.dao.User;

/**
 * The assessment queue offers an "Only my assessments" checkbox, checked by
 * default, to non-managers whose access level reaches beyond their own work.
 * Managers keep the full queue; user-only accounts are always limited to their own.
 */
public class AssessmentQueueScopeTest {

	private static User user(boolean manager, Integer accessLevel) {
		Permissions p = new Permissions();
		p.setManager(manager);
		p.setAssessor(true);
		p.setAccessLevel(accessLevel);
		User u = new User();
		u.setPermissions(p);
		return u;
	}

	@Test
	public void nonManagerWithAllAssessmentsGetsTheToggleAndDefaultsToOwn() {
		User u = user(false, Permissions.AccessLevelAllData);
		assertTrue(AssessmentQueue.canWidenQueue(u));
		assertTrue("no parameter means only mine", AssessmentQueue.restrictToMine(u, null));
		assertTrue(AssessmentQueue.restrictToMine(u, Boolean.TRUE));
		assertFalse("unticking the box widens the queue", AssessmentQueue.restrictToMine(u, Boolean.FALSE));
	}

	@Test
	public void nonManagerWithTeamAssessmentsGetsTheToggleAndDefaultsToOwn() {
		User u = user(false, Permissions.AccessLevelTeamOnly);
		assertTrue(AssessmentQueue.canWidenQueue(u));
		assertTrue(AssessmentQueue.restrictToMine(u, null));
		assertFalse(AssessmentQueue.restrictToMine(u, Boolean.FALSE));
	}

	@Test
	public void userOnlyLevelHasNoToggleAndIsAlwaysOwn() {
		User u = user(false, Permissions.AccessLevelUserOnly);
		assertFalse(AssessmentQueue.canWidenQueue(u));
		assertTrue(AssessmentQueue.restrictToMine(u, null));
		assertTrue("cannot widen past the access level", AssessmentQueue.restrictToMine(u, Boolean.FALSE));
	}

	@Test
	public void managerHasNoToggleAndSeesTheFullQueue() {
		User u = user(true, Permissions.AccessLevelAllData);
		assertFalse(AssessmentQueue.canWidenQueue(u));
		assertFalse(AssessmentQueue.restrictToMine(u, null));
		assertFalse("the parameter is ignored for managers", AssessmentQueue.restrictToMine(u, Boolean.TRUE));
	}
}
