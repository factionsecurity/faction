package com.fuse.servlets;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fuse.api.MongoTestBase;
import com.fuse.dao.Assessment;
import com.fuse.dao.Files;
import com.fuse.dao.HibHelper;
import com.fuse.dao.Permissions;
import com.fuse.dao.Teams;
import com.fuse.dao.User;
import com.fuse.dao.Verification;

/**
 * /service/fileUpload must apply object-level authorization: a stored file is
 * reachable only by users who may access the assessment (or verification) it
 * belongs to, using the same rules as opening that assessment.
 */
public class FileAccessTest extends MongoTestBase {

	private static EntityManagerFactory emf;
	private static String tag;
	private static Teams teamA, teamB;
	private static User ownerA, teammateA, strangerB, managerB, remediationB;
	private static Assessment asmt;
	private static Verification verification;
	private static Files asmtFile, verFile;

	@BeforeClass
	public static void seed() {
		emf = HibHelper.getInstance().getEMF();
		org.junit.Assume.assumeNotNull("EntityManagerFactory unavailable - skipping integration test", emf);
		tag = UUID.randomUUID().toString().substring(0, 8);
		EntityManager em = emf.createEntityManager();
		try {
			teamA = team(em, "A-" + tag);
			teamB = team(em, "B-" + tag);
			ownerA = user(em, "owner-" + tag, teamA, false, false, Permissions.AccessLevelUserOnly);
			teammateA = user(em, "mate-" + tag, teamA, false, false, Permissions.AccessLevelTeamOnly);
			strangerB = user(em, "stranger-" + tag, teamB, false, false, Permissions.AccessLevelUserOnly);
			managerB = user(em, "manager-" + tag, teamB, true, false, Permissions.AccessLevelAllData);
			remediationB = user(em, "rem-" + tag, teamB, false, true, Permissions.AccessLevelUserOnly);
			asmt = new Assessment();
			asmt.setName("asmt-" + tag);
			asmt.setAppId("APP-" + tag);
			List<User> assessors = new ArrayList<>();
			assessors.add(ownerA);
			asmt.setAssessor(assessors);
			asmt.setStart(new Date());
			asmt.setEnd(new Date());
			persist(em, asmt);
			verification = new Verification();
			verification.setAssessment(asmt);
			verification.setAssessor(ownerA);
			verification.setAssignedRemediation(remediationB);
			persist(em, verification);
			asmtFile = file(em, Files.ASSESSMENT, asmt.getId());
			verFile = file(em, Files.VERIFICATION, verification.getId());
		} finally {
			em.close();
		}
	}

	@AfterClass
	public static void cleanup() {
		if (emf == null)
			return;
		EntityManager em = emf.createEntityManager();
		try {
			for (Object o : new Object[] { asmtFile, verFile, verification, asmt, ownerA, teammateA, strangerB, managerB,
					remediationB, teamA, teamB })
				if (o != null)
					remove(em, o);
		} finally {
			em.close();
		}
	}

	@Test
	public void assessmentFileFollowsAssessmentAccess() {
		EntityManager em = emf.createEntityManager();
		try {
			assertTrue("assessor on the assessment", FileAccess.canAccess(em, ownerA, asmtFile));
			assertTrue("same team with team-level access", FileAccess.canAccess(em, teammateA, asmtFile));
			assertTrue("manager with all-data access", FileAccess.canAccess(em, managerB, asmtFile));
			assertFalse("user-only assessor from another team", FileAccess.canAccess(em, strangerB, asmtFile));
		} finally {
			em.close();
		}
	}

	@Test
	public void verificationFileFollowsVerificationAccess() {
		EntityManager em = emf.createEntityManager();
		try {
			assertTrue("verification assessor", FileAccess.canAccess(em, ownerA, verFile));
			assertTrue("assigned remediation user", FileAccess.canAccess(em, remediationB, verFile));
			assertTrue("manager", FileAccess.canAccess(em, managerB, verFile));
			assertFalse("unrelated user-only user", FileAccess.canAccess(em, strangerB, verFile));
		} finally {
			em.close();
		}
	}

	@Test
	public void attachingToAnAssessmentNeedsAccessToIt() {
		EntityManager em = emf.createEntityManager();
		try {
			assertTrue(FileAccess.canAttachToAssessment(em, ownerA, asmt.getId()));
			assertFalse(FileAccess.canAttachToAssessment(em, strangerB, asmt.getId()));
			assertFalse("unknown assessment", FileAccess.canAttachToAssessment(em, managerB, -1L));
		} finally {
			em.close();
		}
	}

	@Test
	public void missingFileIsNeverAccessible() {
		EntityManager em = emf.createEntityManager();
		try {
			assertFalse(FileAccess.canAccess(em, managerB, null));
		} finally {
			em.close();
		}
	}

	private static Files file(EntityManager em, String type, Long entityId) {
		Files f = new Files();
		f.setUuid(UUID.randomUUID().toString());
		f.setName("f-" + tag + ".txt");
		f.setContentType("text/plain");
		f.setType(type);
		f.setEntityId(entityId);
		f.setRealFile("hello".getBytes());
		persist(em, f);
		return f;
	}

	private static Teams team(EntityManager em, String name) {
		Teams t = new Teams();
		t.setTeamName(name);
		persist(em, t);
		return t;
	}

	private static User user(EntityManager em, String name, Teams team, boolean manager, boolean remediation, Integer level) {
		Permissions p = new Permissions();
		p.setAssessor(!remediation);
		p.setManager(manager);
		p.setRemediation(remediation);
		p.setEngagement(false);
		p.setAdmin(false);
		p.setExecutive(false);
		p.setAccessLevel(level);
		User u = new User();
		u.setUsername(name);
		u.setFname(name);
		u.setLname("t");
		u.setEmail(name + "@example.test");
		u.setTeam(team);
		u.setPermissions(p);
		persist(em, u);
		return u;
	}

	private static void persist(EntityManager em, Object entity) {
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		em.persist(entity);
		HibHelper.getInstance().commit();
	}

	private static void remove(EntityManager em, Object entity) {
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		em.remove(em.contains(entity) ? entity : em.merge(entity));
		HibHelper.getInstance().commit();
	}
}
