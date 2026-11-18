package com.fuse.unittests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.junit.Test;

import com.fuse.dao.Assessment;
import com.fuse.dao.Permissions;
import com.fuse.dao.Teams;
import com.fuse.dao.User;
import com.fuse.dao.query.AssessmentQueries;

/**
 * An assessor can open and update any assessment their Access Level covers, so
 * they can take over work a teammate can't finish:
 * "All Assessments" covers every assessment, "Team Assessments" covers the
 * team's, and "Only Assessments Owned by User" covers only their own open ones.
 */
public class AssessmentEditAccessTest {

	private static final Teams TEAM_A = team(1L);
	private static final Teams TEAM_B = team(2L);

	private final User ownerInA = user(10L, TEAM_A, Permissions.AccessLevelUserOnly, true, false);
	private final Assessment teamAWork = assessment(ownerInA);

	@Test
	public void assignedAssessor_CanEdit() {
		assertTrue(AssessmentQueries.canEditAssessment(ownerInA, teamAWork));
	}

	@Test
	public void allAssessmentsAssessor_CanEditAnotherTeamsAssessment() {
		User other = user(20L, TEAM_B, Permissions.AccessLevelAllData, true, false);
		assertTrue(AssessmentQueries.canEditAssessment(other, teamAWork));
	}

	@Test
	public void unsetAccessLevel_MeansAllAssessments() {
		User other = user(21L, TEAM_B, null, true, false);
		assertTrue(AssessmentQueries.canEditAssessment(other, teamAWork));
	}

	@Test
	public void teamAssessor_CanEditTheirTeamsAssessment() {
		User teammate = user(30L, TEAM_A, Permissions.AccessLevelTeamOnly, true, false);
		assertTrue(AssessmentQueries.canEditAssessment(teammate, teamAWork));
	}

	@Test
	public void teamAssessor_CannotEditAnotherTeamsAssessment() {
		User outsider = user(31L, TEAM_B, Permissions.AccessLevelTeamOnly, true, false);
		assertFalse(AssessmentQueries.canEditAssessment(outsider, teamAWork));
	}

	@Test
	public void ownOnlyAssessor_CannotEditSomeoneElsesAssessment() {
		User teammate = user(40L, TEAM_A, Permissions.AccessLevelUserOnly, true, false);
		assertFalse(AssessmentQueries.canEditAssessment(teammate, teamAWork));
	}

	@Test
	public void userWithoutAssessorRole_CannotEditUnassignedAssessment() {
		User manager = user(50L, TEAM_A, Permissions.AccessLevelAllData, false, true);
		assertFalse(AssessmentQueries.canEditAssessment(manager, teamAWork));
	}

	@Test
	public void finalizedAssessment_IsNotEditableByAnyone() {
		teamAWork.setCompleted(new Date());
		User other = user(20L, TEAM_B, Permissions.AccessLevelAllData, true, false);
		assertFalse(AssessmentQueries.canEditAssessment(ownerInA, teamAWork));
		assertFalse(AssessmentQueries.canEditAssessment(other, teamAWork));
	}

	@Test
	public void ownOnlyAssessor_CanStillOpenTheirOwnFinalizedAssessment() {
		teamAWork.setCompleted(new Date());
		assertTrue(AssessmentQueries.canOpenAssessment(ownerInA, teamAWork));
	}

	@Test
	public void allAssessmentsAssessor_CanOpenAnotherTeamsAssessment() {
		User other = user(20L, TEAM_B, Permissions.AccessLevelAllData, true, false);
		assertTrue(AssessmentQueries.canOpenAssessment(other, teamAWork));
	}

	@Test
	public void teamAssessor_CannotOpenAnotherTeamsAssessment() {
		User outsider = user(31L, TEAM_B, Permissions.AccessLevelTeamOnly, true, false);
		assertFalse(AssessmentQueries.canOpenAssessment(outsider, teamAWork));
	}

	@Test
	public void ownOnlyAssessor_CannotOpenSomeoneElsesAssessment() {
		User teammate = user(40L, TEAM_A, Permissions.AccessLevelUserOnly, true, false);
		assertFalse(AssessmentQueries.canOpenAssessment(teammate, teamAWork));
	}

	@Test
	public void nullsAreNotEditable() {
		assertFalse(AssessmentQueries.canEditAssessment(ownerInA, null));
		assertFalse(AssessmentQueries.canEditAssessment(null, teamAWork));
	}

	private static Teams team(Long id) {
		Teams t = new Teams();
		t.setId(id);
		return t;
	}

	private static User user(long id, Teams team, Integer accessLevel, boolean assessor, boolean manager) {
		Permissions p = new Permissions();
		p.setAdmin(false);
		p.setEngagement(false);
		p.setExecutive(false);
		p.setRemediation(false);
		p.setAssessor(assessor);
		p.setManager(manager);
		p.setAccessLevel(accessLevel);
		User u = new User();
		u.setId(id);
		u.setTeam(team);
		u.setPermissions(p);
		return u;
	}

	private static Assessment assessment(User... assessors) {
		Assessment a = new Assessment();
		a.setId(100L);
		a.setAssessor(new ArrayList<User>(Arrays.asList(assessors)));
		return a;
	}
}
