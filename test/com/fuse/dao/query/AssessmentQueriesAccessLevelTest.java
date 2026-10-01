package com.fuse.dao.query;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fuse.api.MongoTestBase;
import com.fuse.dao.Assessment;
import com.fuse.dao.HibHelper;
import com.fuse.dao.Permissions;
import com.fuse.dao.Teams;
import com.fuse.dao.User;

/**
 * The per-user Access Level (Admin > Users) decides which assessments a
 * non-manager assessor sees in the queue and in app/name search:
 * "All Assessments" sees every assessment, "Team Assessments" sees the
 * team's, "Only Assessments Owned by User" sees their own.
 */
public class AssessmentQueriesAccessLevelTest extends MongoTestBase {

	private static EntityManagerFactory emf;
	private static String tag;
	private static Teams teamA, teamB;
	private static User allInA, teamOnlyInA, ownOnlyInB;
	private static Assessment ownedByAllInA, ownedByTeamOnlyInA, ownedByOwnOnlyInB;

	@BeforeClass
	public static void seed() {
		emf = HibHelper.getInstance().getEMF();
		org.junit.Assume.assumeNotNull("EntityManagerFactory unavailable - skipping integration test", emf);
		tag = UUID.randomUUID().toString().substring(0, 8);
		EntityManager em = emf.createEntityManager();
		try {
			teamA = team(em, "A-" + tag);
			teamB = team(em, "B-" + tag);
			allInA = assessor(em, "all-" + tag, teamA, Permissions.AccessLevelAllData);
			teamOnlyInA = assessor(em, "team-" + tag, teamA, Permissions.AccessLevelTeamOnly);
			ownOnlyInB = assessor(em, "own-" + tag, teamB, Permissions.AccessLevelUserOnly);
			ownedByAllInA = assessment(em, "asmt-all-" + tag, allInA);
			ownedByTeamOnlyInA = assessment(em, "asmt-team-" + tag, teamOnlyInA);
			ownedByOwnOnlyInB = assessment(em, "asmt-own-" + tag, ownOnlyInB);
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
			for (Object o : new Object[] { ownedByAllInA, ownedByTeamOnlyInA, ownedByOwnOnlyInB, allInA, teamOnlyInA,
					ownOnlyInB, teamA, teamB }) {
				if (o != null)
					remove(em, o);
			}
		} finally {
			em.close();
		}
	}

	@Test
	public void allAssessmentsLevelSeesEveryAssessmentInQueue() {
		assertEquals(names(ownedByAllInA, ownedByTeamOnlyInA, ownedByOwnOnlyInB), queueFor(allInA));
	}

	@Test
	public void teamLevelSeesTeamAssessmentsInQueue() {
		assertEquals(names(ownedByAllInA, ownedByTeamOnlyInA), queueFor(teamOnlyInA));
	}

	@Test
	public void ownOnlyLevelSeesOnlyOwnAssessmentsInQueue() {
		assertEquals(names(ownedByOwnOnlyInB), queueFor(ownOnlyInB));
	}

	@Test
	public void allAssessmentsLevelSeesEveryAssessmentInAppSearch() {
		assertEquals(names(ownedByAllInA, ownedByTeamOnlyInA, ownedByOwnOnlyInB), searchFor(allInA));
	}

	@Test
	public void ownOnlyLevelSeesOnlyOwnAssessmentsInAppSearch() {
		assertEquals(names(ownedByOwnOnlyInB), searchFor(ownOnlyInB));
	}

	@Test
	public void allAssessmentsLevelWithOnlyMineSeesOwnAssessmentsInQueue() {
		assertEquals(names(ownedByAllInA), queueFor(allInA, true));
	}

	@Test
	public void allAssessmentsLevelWithoutOnlyMineSeesEveryAssessmentInQueue() {
		assertEquals(names(ownedByAllInA, ownedByTeamOnlyInA, ownedByOwnOnlyInB), queueFor(allInA, false));
	}

	@Test
	public void teamLevelWithOnlyMineSeesOwnAssessmentsInQueue() {
		assertEquals(names(ownedByTeamOnlyInA), queueFor(teamOnlyInA, true));
	}

	// ---- helpers ----

	private static List<String> queueFor(User u) {
		EntityManager em = emf.createEntityManager();
		try {
			return tagged(AssessmentQueries.getAllAssessments(em, u, AssessmentQueries.OnlyNonCompleted));
		} finally {
			em.close();
		}
	}

	private static List<String> queueFor(User u, boolean onlyMine) {
		EntityManager em = emf.createEntityManager();
		try {
			return tagged(AssessmentQueries.getAllAssessments(em, u, AssessmentQueries.OnlyNonCompleted, onlyMine));
		} finally {
			em.close();
		}
	}

	private static List<String> searchFor(User u) {
		EntityManager em = emf.createEntityManager();
		try {
			return tagged(AssessmentQueries.getAssessmentsByAppDesc(em, u, "APP-" + tag, null,
					AssessmentQueries.OnlyNonCompleted));
		} finally {
			em.close();
		}
	}

	private static List<String> tagged(List<Assessment> asmts) {
		return asmts.stream().map(Assessment::getName).filter(n -> n != null && n.endsWith(tag)).sorted()
				.collect(Collectors.toList());
	}

	private static List<String> names(Assessment... asmts) {
		List<String> out = new ArrayList<>();
		for (Assessment a : asmts)
			out.add(a.getName());
		java.util.Collections.sort(out);
		return out;
	}

	private static Teams team(EntityManager em, String name) {
		Teams t = new Teams();
		t.setTeamName(name);
		persist(em, t);
		return t;
	}

	private static User assessor(EntityManager em, String name, Teams team, Integer accessLevel) {
		Permissions p = new Permissions();
		p.setAssessor(true);
		p.setManager(false);
		p.setAccessLevel(accessLevel);
		User u = new User();
		u.setUsername(name);
		u.setFname(name);
		u.setLname("test");
		u.setEmail(name + "@example.test");
		u.setTeam(team);
		u.setPermissions(p);
		persist(em, u);
		return u;
	}

	private static Assessment assessment(EntityManager em, String name, User assessor) {
		Assessment a = new Assessment();
		a.setName(name);
		a.setAppId("APP-" + tag);
		List<User> assessors = new ArrayList<>();
		assessors.add(assessor);
		a.setAssessor(assessors);
		a.setStart(new Date());
		a.setEnd(new Date());
		persist(em, a);
		return a;
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
