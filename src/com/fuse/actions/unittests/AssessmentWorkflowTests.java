package com.fuse.actions.unittests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.apache.struts2.StrutsJUnit4TestCase;
import org.junit.Test;
import org.python.icu.util.Calendar;

import com.fuse.actions.Login;
import com.fuse.actions.admin.DefaultVulns;
import com.fuse.actions.assessment.AddVulnerability;
import com.fuse.actions.assessment.AssessmentView;
import com.fuse.actions.assessment.TrackChanges;
import com.fuse.actions.remediation.RemVulnData;
import com.fuse.actions.remediation.Remediation;
import com.fuse.actions.retests.VerificationQueue;
import com.fuse.actions.scheduling.EditAssessment;
import com.fuse.actions.scheduling.Engagement;
import com.fuse.dao.Assessment;
import com.fuse.dao.CustomField;
import com.fuse.dao.DefaultVulnerability;
import com.fuse.dao.HibHelper;
import com.fuse.dao.PeerReview;
import com.fuse.dao.SystemSettings;
import com.fuse.dao.User;
import com.fuse.dao.Verification;
import com.fuse.dao.Vulnerability;
import com.opensymphony.xwork2.ActionProxy;

public class AssessmentWorkflowTests extends StrutsJUnit4TestCase {

	private static Long holdit;
	private static String uuid = UUID.randomUUID().toString();
	private static Date now = new Date();
	private static long camp1 = 8l;
	private static int type1 = 4;
	// private static long verOption = 0l; // Send To Remediation
	// private static long verOption = 1l; // Close in Dev
	private static long verOption = 2l; // Close in Prod

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
	public void t000setUpVerificationOption() throws Exception {
		User u = login("admin", "password123");
		EntityManager em = HibHelper.getInstance().getEM();
		SystemSettings ss = (SystemSettings) em.createQuery("From SystemSettings").getResultList().stream().findFirst()
				.orElse(null);
		assertNotNull(ss);
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		ss.setVerificationOption(verOption); // Send To Remediation
		em.persist(ss);
		HibHelper.getInstance().commit();

	}

	@Test
	public void t001createAssessment() throws Exception {
		User u = login("admin", "password123");
		ActionProxy proxy = getActionProxy("/portal/Engagement");
		Engagement eng = (Engagement) proxy.getAction();
		assertNotNull("Engagment Action is null", eng);
		eng.set_token(eng.get_token());
		eng.setAction("createAssessment");
		eng.setAppid(uuid);
		eng.setAppName(uuid);
		eng.setSdate(now);
		eng.setEdate(now);
		eng.setAssessorId(new ArrayList<Integer>());
		eng.getAssessorId().add(((Long) u.getId()).intValue());
		eng.setRemId(((Long) u.getId()).intValue());
		eng.setEngId(((Long) u.getId()).intValue());
		eng.setCampId(camp1); // 2017 Assessments
		eng.setType(type1); // Manual Ethical hacking
		eng.setNotes(uuid);
		eng.setDistro("josh.summitt@fusesoft.co");
		eng.setCf(
				"[{\"id\" : 260, \"text\" : \"unittest\"},{\"id\" : 262, \"text\" : \"unittest\"},{\"id\" : 425, \"text\" : \"unittest\"},{\"id\" : 426, \"text\" : \"unittest\"}]");
		;
		String result = proxy.execute();
		assertEquals("Did not Redirect to assesment Queue", eng.SUCCESS, result);
		holdit = eng.getUnitTestId();

		// Should fail XSRF token
		proxy = getActionProxy("/portal/Engagement");
		eng = (Engagement) proxy.getAction();
		assertNotNull("Engagment Action is null", eng);
		eng.setAction("createAssessment");
		eng.setAppid("");
		eng.setAppName("");
		eng.setSdate(now);
		eng.setEdate(now);
		eng.setAssessorId(new ArrayList<Integer>());
		eng.getAssessorId().add(((Long) u.getId()).intValue());
		eng.setRemId(((Long) u.getId()).intValue());
		eng.setEngId(((Long) u.getId()).intValue());
		eng.setCampId(camp1); // 2017 Assessments
		eng.setType(type1); // Manual Ethical hacking
		eng.setNotes(uuid);
		eng.setDistro("josh.summitt@fusesoft.co");
		result = proxy.execute();
		// assertNotEquals("The assessment should have failed to add due to blank
		// fields" , eng.SUCCESS, result);

		// Test Blank Fields
		proxy = getActionProxy("/portal/Engagement");
		eng = (Engagement) proxy.getAction();
		eng.set_token(eng.get_token());
		assertNotNull("Engagment Action is null", eng);
		eng.setAction("createAssessment");
		eng.setAppid("");
		eng.setAppName("");
		eng.setSdate(now);
		eng.setEdate(now);
		eng.setAssessorId(new ArrayList<Integer>());
		eng.getAssessorId().add(((Long) u.getId()).intValue());
		eng.setRemId(((Long) u.getId()).intValue());
		eng.setEngId(((Long) u.getId()).intValue());
		eng.setCampId(camp1); // 2017 Assessments
		eng.setType(type1); // Manual Ethical hacking
		eng.setNotes(uuid);
		eng.setDistro("josh.summitt@fusesoft.co");
		result = proxy.execute();
		// assertNotEquals("The assessment should have failed to add due to blank
		// fields" , eng.SUCCESS, result);

	}

	@Test
	public void t002testData() {

		EntityManager em = HibHelper.getInstance().getEM();
		Assessment am = em.find(Assessment.class, holdit);
		assertNotNull("Could not find the created assessment", am);
		assertEquals(uuid, am.getName());
		assertEquals(uuid, am.getAppId());
		assertEquals(uuid, am.getAccessNotes());
		assertEquals(null, am.getNotes());
		// assertNotEquals(null, am.getAssessor());
		assertEquals(1, am.getAssessor().size());
		assertEquals("admin", am.getAssessor().get(0).getUsername());
		assertEquals("admin", am.getEngagement().getUsername());
		assertEquals("admin", am.getRemediation().getUsername());
		assertEquals((Long) camp1, am.getCampaign().getId());
		assertEquals((Long) Integer.toUnsignedLong(type1), am.getType().getId());
		assertEquals(null, am.getCompleted());
		assertEquals("josh.summitt@fusesoft.co", am.getDistributionList());
		assertEquals(now, am.getEnd());
		assertEquals(now, am.getStart());
		assertEquals(null, am.getFinalReport());
		// assertNotEquals(null,am.getGuid());
		assertEquals(null, am.getNotes());
		assertEquals(null, am.getRetestReport());
		assertEquals(0, am.getVulns().size());
		em.close();

	}

	@Test
	public void t003editAssessment() throws Exception {
		User u = login("admin", "password123");

		// Just Modify the assessemnt
		ActionProxy proxy = getActionProxy("/portal/EditAssessment");
		EditAssessment eng = (EditAssessment) proxy.getAction();
		assertNotNull("Engagment Action is null", eng);
		eng.set_token(eng.get_token());
		eng.setAction("update");
		eng.setAid(holdit.intValue());
		eng.setAppid("modified" + uuid);
		eng.setAppName("modified" + uuid);
		Calendar c = Calendar.getInstance();
		c.setTime(now);
		c.add(Calendar.DATE, 5);
		eng.setSdate(c.getTime());
		eng.setEdate(c.getTime());
		eng.setAssessorId(new ArrayList<Integer>());
		eng.getAssessorId().add(((Long) u.getId()).intValue());
		eng.getAssessorId().add(6);
		eng.setRemId(((Long) u.getId()).intValue());
		eng.setEngId(((Long) u.getId()).intValue());
		eng.setCampId(8l); // 2017 Assessments
		eng.setType(4); // Manual Ethical hacking
		eng.setNotes("modified" + uuid);
		eng.setDistro("josh.summitt@fusesoft.co; modified@fusesoft.co");
		String result = proxy.execute();
		assertEquals("Did not Redirect to assesment Queue", eng.SUCCESS, result);

		// Test Missing XSRF
		proxy = getActionProxy("/portal/EditAssessment");
		eng = (EditAssessment) proxy.getAction();
		assertNotNull("Engagment Action is null", eng);
		eng.setAction("update");
		eng.setAid(holdit.intValue());
		eng.setAppid("modified" + uuid);
		eng.setAppName("modified" + uuid);
		c = Calendar.getInstance();
		c.setTime(now);
		c.add(Calendar.DATE, 5);
		eng.setSdate(c.getTime());
		eng.setEdate(c.getTime());
		eng.setAssessorId(new ArrayList<Integer>());
		eng.getAssessorId().add(((Long) u.getId()).intValue());
		eng.getAssessorId().add(6);
		eng.setRemId(((Long) u.getId()).intValue());
		eng.setEngId(((Long) u.getId()).intValue());
		eng.setCampId(8l); // 2017 Assessments
		eng.setType(4); // Manual Ethical hacking
		eng.setNotes("modified" + uuid);
		eng.setDistro("josh.summitt@fusesoft.co; modified@fusesoft.co");
		result = proxy.execute();
		// assertNotEquals("XSRF token should have failed" , eng.SUCCESS, result);

		// Test Missing App ID or AppNameFields
		proxy = getActionProxy("/portal/EditAssessment");
		eng = (EditAssessment) proxy.getAction();
		assertNotNull("Engagment Action is null", eng);
		eng.set_token(eng.get_token());
		eng.setAction("update");
		eng.setAid(holdit.intValue());
		eng.setAppid("");
		eng.setAppName("");
		c = Calendar.getInstance();
		c.setTime(now);
		c.add(Calendar.DATE, 5);
		eng.setSdate(c.getTime());
		eng.setEdate(c.getTime());
		eng.setAssessorId(new ArrayList<Integer>());
		eng.getAssessorId().add(((Long) u.getId()).intValue());
		eng.getAssessorId().add(6);
		eng.setRemId(((Long) u.getId()).intValue());
		eng.setEngId(((Long) u.getId()).intValue());
		eng.setCampId(8l); // 2017 Assessments
		eng.setType(4); // Manual Ethical hacking
		eng.setNotes("modified" + uuid);
		eng.setDistro("josh.summitt@fusesoft.co; modified@fusesoft.co");
		result = proxy.execute();
		// assertNotEquals("XSRF token should have failed" , eng.SUCCESS, result);

		// Test Missing App ID or AppNameFields
		proxy = getActionProxy("/portal/EditAssessment");
		eng = (EditAssessment) proxy.getAction();
		assertNotNull("Engagment Action is null", eng);
		eng.set_token(eng.get_token());
		eng.setAction("update");
		eng.setAid(holdit.intValue());
		eng.setAppid("missing type");
		eng.setAppName("missing type");
		c = Calendar.getInstance();
		c.setTime(now);
		c.add(Calendar.DATE, 5);
		eng.setSdate(c.getTime());
		eng.setEdate(c.getTime());
		eng.setAssessorId(new ArrayList<Integer>());
		eng.getAssessorId().add(((Long) u.getId()).intValue());
		eng.getAssessorId().add(6);
		eng.setRemId(((Long) u.getId()).intValue());
		eng.setEngId(((Long) u.getId()).intValue());
		eng.setCampId(8l); // 2017 Assessments
		eng.setType(null); // Manual Ethical hacking
		eng.setNotes("modified" + uuid);
		eng.setDistro("josh.summitt@fusesoft.co; modified@fusesoft.co");
		result = proxy.execute();
		// assertNotEquals("XSRF token should have failed" , eng.SUCCESS, result);

	}

	@Test
	public void t004testData() {
		EntityManager em = HibHelper.getInstance().getEM();
		Assessment am = em.find(Assessment.class, holdit);
		assertNotNull("Could not find the created assessment", am);
		assertEquals("modified" + uuid, am.getName());
		assertEquals("modified" + uuid, am.getAppId());
		assertEquals("modified" + uuid, am.getAccessNotes());
		assertEquals(null, am.getNotes());
		// assertNotEquals(null, am.getAssessor());
		assertEquals(2, am.getAssessor().size());

		assertEquals("admin", am.getEngagement().getUsername());
		assertEquals("admin", am.getRemediation().getUsername());
		assertEquals((Long) camp1, am.getCampaign().getId());
		assertEquals((Long) Integer.toUnsignedLong(type1), am.getType().getId());
		assertEquals(null, am.getCompleted());
		assertEquals("josh.summitt@fusesoft.co; modified@fusesoft.co", am.getDistributionList());
		Calendar c = Calendar.getInstance();
		c.setTime(now);
		c.add(Calendar.DATE, 5);
		assertEquals(c.getTime(), am.getEnd());
		assertEquals(c.getTime(), am.getStart());
		assertEquals(null, am.getFinalReport());
		// assertNotEquals(null,am.getGuid());
		assertEquals(null, am.getNotes());
		assertEquals(null, am.getRetestReport());
		assertEquals(0, am.getVulns().size());
		em.close();

	}

	// TODO Test for adding vulns to an assessment not owned by the current user
	// TODO Category does not seem to get set for a custom vuln
	@Test
	public void t005addVulnerabilitiesandSummaries() throws Exception {
		User u = login("admin", "password123");

		// Just Modify the assessemnt
		ActionProxy proxy = getActionProxy("/portal/DefaultVulns");
		DefaultVulns dvulns = (DefaultVulns) proxy.getAction();
		assertNotNull("AddVulnerability Action is null", dvulns);
		dvulns.setTerms("xss");
		dvulns.setAction("json");

		String result = proxy.execute();
		assertEquals("Search Did not return results", "vulnsearch", result);
		List<DefaultVulnerability> searchedVulns = dvulns.getVulnerabilities();
		assertNotNull("Null Vuln list", searchedVulns);
		assertTrue("Size of vuln list is not right", searchedVulns.size() > 0);
		DefaultVulnerability vuln2add = searchedVulns.get(0);
		assertTrue(vuln2add.getName().toLowerCase().contains("xss"));

		proxy = getActionProxy("/portal/AddVulnerability");
		AddVulnerability addVuln = (AddVulnerability) proxy.getAction();
		addVuln.set_token(addVuln.get_token());
		addVuln.setAction("add");
		addVuln.setId(holdit);
		addVuln.setDefaultTitle(((Long) vuln2add.getId()).intValue());

		addVuln.setImpact(3l);
		addVuln.setLikelyhood(3l);
		addVuln.setOverall(3l);
		addVuln.setTitle(uuid);
		result = proxy.execute();
		assertEquals("Vuln was not added Successfully", "_json", result);

		/// Test adding w/o XSRF token
		proxy = getActionProxy("/portal/AddVulnerability");
		addVuln = (AddVulnerability) proxy.getAction();
		addVuln.setAction("add");
		addVuln.setId(holdit);
		addVuln.setDefaultTitle(((Long) vuln2add.getId()).intValue());

		addVuln.setImpact(3l);
		addVuln.setLikelyhood(3l);
		addVuln.setOverall(3l);
		addVuln.setTitle(uuid);
		result = proxy.execute();
		// assertNotEquals("Vuln should not be added w/out XSRF" , "successJson",
		// result);

		// add a custom vuln
		proxy = getActionProxy("/portal/AddVulnerability");
		AddVulnerability custVuln = (AddVulnerability) proxy.getAction();
		custVuln.set_token(custVuln.get_token());
		custVuln.setAction("add");
		custVuln.setId(holdit);
		custVuln.setDescription(uuid);
		custVuln.setRecommendation(uuid);
		custVuln.setId(holdit);
		custVuln.setImpact(3l);
		custVuln.setLikelyhood(3l);
		custVuln.setOverall(3l);
		custVuln.setTitle("Custom-" + uuid);
		result = proxy.execute();
		assertEquals("Custom Vuln was not added Successfully", "_json", result);

		// Edit a vulnerability
		proxy = getActionProxy("/portal/AddVulnerability");
		AddVulnerability editVuln = (AddVulnerability) proxy.getAction();
		editVuln.set_token(editVuln.get_token());
		editVuln.setAction("update");
		editVuln.setVulnid(custVuln.getUnittestVulnid());
		editVuln.setDescription("Updated" + uuid);
		editVuln.setRecommendation("Updated" + uuid);
		editVuln.setImpact(4l);
		editVuln.setOverall(4l);
		editVuln.setLikelyhood(4l);
		editVuln.setTitle("Updated-" + uuid);
		result = proxy.execute();
		assertEquals("Edit Vuln was not added Successfully", editVuln.SUCCESSJSON, result);

		// update assessment summaries and notes
		proxy = getActionProxy("/portal/Assessment");
		AssessmentView av = (AssessmentView) proxy.getAction();
		av.set_token(av.get_token());
		av.setId("" + holdit);
		av.setUpdate("true");
		av.setSummary(uuid);
		av.setRiskAnalysis(uuid);
		av.setNotes(uuid);
		result = proxy.execute();
		assertEquals("Assessment Info was not added Successfully", "successJson", result);

		// add a custom vuln missing title
		proxy = getActionProxy("/portal/AddVulnerability");
		custVuln = (AddVulnerability) proxy.getAction();
		custVuln.set_token(custVuln.get_token());
		custVuln.setAction("add");
		custVuln.setId(holdit);
		custVuln.setDescription(uuid);
		custVuln.setRecommendation(uuid);
		custVuln.setId(holdit);
		custVuln.setImpact(3l);
		custVuln.setLikelyhood(3l);
		custVuln.setOverall(3l);
		custVuln.setTitle("");
		result = proxy.execute();
		// assertNotEquals("Vuld should be added if missing a title" , "successJson",
		// result);

		// add a custom vuln missing overall severity rating
		proxy = getActionProxy("/portal/AddVulnerability");
		custVuln = (AddVulnerability) proxy.getAction();
		custVuln.set_token(custVuln.get_token());
		custVuln.setAction("add");
		custVuln.setId(holdit);
		custVuln.setDescription(uuid);
		custVuln.setRecommendation(uuid);
		custVuln.setId(holdit);
		custVuln.setImpact(3l);
		custVuln.setLikelyhood(3l);
		custVuln.setOverall(null);
		custVuln.setTitle("no sev rating");
		result = proxy.execute();
		// assertNotEquals("Vuld should be added if missing a title" , "successJson",
		// result);

	}

	@Test
	public void t006testData() {
		EntityManager em = HibHelper.getInstance().getEM();
		Assessment am = em.find(Assessment.class, holdit);
		assertNotNull("Could not find the created assessment", am);
		assertEquals(uuid, am.getNotes());
		assertEquals(uuid, am.getSummary());
		assertEquals(uuid, am.getRiskAnalysis());
		int checks = 0;
		for (Vulnerability v : am.getVulns()) {
			if (v.getName().equals("Custom-" + uuid))
				checks++;
			if (v.getName().equals("Updated-" + uuid))
				checks++;
		}
		assertEquals("Vuln names not updated Correctly", 2, checks);
		em.close();
	}

	@Test
	public void t007add4ExploitSteps() {
		// fail("Not Implemented");
	}

	@Test
	public void t008deleteStep() {
		// fail("Not Implemented");
	}

	@Test
	public void t009reOderSteps() {
		// fail("Not Implemented");
	}

	@Test
	public void t010deleteVulnerability() {
		// fail("Not Implemented");
	}

	@Test
	public void t011deleteVulnerabilityWithSteps() {
		// fail("Not Implemented");
	}

	@Test
	public void t012upload2Files() {
		// fail("Not Implemented");
	}

	@Test
	public void t013deleteFile() {
		// fail("Not Implemented");
	}

	@Test
	public void t014testUpdateAsmtCF() throws Exception {

		User u = login("admin", "password123");

		ActionProxy proxy = getActionProxy("/portal/UpdateAsmtCF");
		AssessmentView av = (AssessmentView) proxy.getAction();
		assertNotNull("Assessment View was null", av);
		EntityManager em = HibHelper.getInstance().getEM();
		Assessment a = em.find(Assessment.class, this.holdit);
		List<CustomField> cfs = a.getCustomFields();
		if (cfs == null || cfs.size() == 0)
			fail("No Custom Fields for this assessment");

		av.set_token(av.get_token());
		av.setId("" + holdit);
		av.setCfid(cfs.get(0).getId());

		String uuidStr = uuid.toString();
		av.setCfValue(uuidStr);

		String result = proxy.execute();
		assertEquals("Error in Response", av.SUCCESSJSON, result);
		String value = av.getAssessment().getCustomFields().get(0).getValue();
		assertEquals("Custom Values do not match", uuidStr, value);

		// Test missing XSRF token
		proxy = getActionProxy("/portal/UpdateAsmtCF");
		av = (AssessmentView) proxy.getAction();
		av.setId("106");
		av.setCfid(516l);
		result = proxy.execute();
		// assertNotEquals("Error in Response", av.SUCCESSJSON, result);

		System.out.println(result);

	}

	@Test
	public void t015generateReport() throws Exception {
		User u = login("admin", "password123");
		ActionProxy proxy = getActionProxy("/portal/Assessment");
		AssessmentView av = (AssessmentView) proxy.getAction();
		av.setId("" + this.holdit);
		av.setAction("genreport");
		String result = proxy.execute();
		assertTrue(result == "successJson");
		Thread.sleep(30 * 1000); // wait 30 seconds for report to be generated.

	}

	@Test
	public void t016getReport() throws Exception {

		User u = login("admin", "password123");
		ActionProxy proxy = getActionProxy("/portal/Assessment");
		AssessmentView av = (AssessmentView) proxy.getAction();
		String result = proxy.execute();
		Assessment am = av.em.find(Assessment.class, holdit);
		assertNotNull(am.getFinalReport());
		assertNotNull(am.getFinalReport().getBase64EncodedPdf());

	}

	@Test
	public void t017downloadReport() {
		// fail("Not Implemented");
	}

	@Test
	public void t018submit4PR() throws Exception {
		User u = login("admin", "password123");
		ActionProxy proxy = getActionProxy("/portal/Assessment");
		AssessmentView av = (AssessmentView) proxy.getAction();
		av.set_token(av.get_token());
		av.setId("" + this.holdit);
		av.setAction("prsubmit");
		String result = proxy.execute();
		assertTrue(result == "successJson");

	}

	@Test
	public void t019completePR() throws Exception {
		User u = login("admin", "password123");
		ActionProxy proxy = getActionProxy("/portal/TrackChanges");
		TrackChanges pr = (TrackChanges) proxy.getAction();
		EntityManager em = HibHelper.getInstance().getEM();
		List<PeerReview> reviews = em.createQuery("From PeerReview").getResultList();
		PeerReview r = reviews.get(reviews.size() - 1);
		assertTrue(r.getAssessment().getId().longValue() == this.holdit);
		pr.setPrid(r.getId());
		pr.setSummary("test");
		pr.setRisk("test");
		pr.setSum_notes("test");
		pr.setRisk_notes("test");
		pr.setVuln_desc(new HashMap());
		pr.setVuln_desc_notes(new HashMap());
		for (Vulnerability vuln : r.getAssessment().getVulns()) {
			pr.getVuln_desc().put("" + vuln.getId(), vuln.getDescription());
			pr.getVuln_desc_notes().put("" + vuln.getId(), "Notes");
		}
		pr.setAction("completePR");
		String result = proxy.execute();
		assertTrue(result == "completeJSON");

	}

	@Test
	public void t020acceptPR() throws Exception {
		User u = login("admin", "password123");
		ActionProxy proxy = getActionProxy("/portal/TrackChanges");
		TrackChanges pr = (TrackChanges) proxy.getAction();
		EntityManager em = HibHelper.getInstance().getEM();
		List<PeerReview> reviews = em.createQuery("From PeerReview").getResultList();
		PeerReview r = reviews.get(reviews.size() - 1);
		assertTrue(r.getAssessment().getId().longValue() == this.holdit);
		pr.setPrid(r.getId());
		pr.setSummary("test");
		pr.setAction("complete");
		pr.setRisk("test");
		pr.setSum_notes("test");
		pr.setRisk_notes("test");
		pr.setVuln_desc(new HashMap());
		pr.setVuln_desc_notes(new HashMap());
		for (Vulnerability vuln : r.getAssessment().getVulns()) {
			pr.getVuln_desc().put("" + vuln.getId(), vuln.getDescription());
			pr.getVuln_desc_notes().put("" + vuln.getId(), "Notes");
		}
		String result = proxy.execute();
		assertTrue(result == pr.SUCCESSJSON);

	}

	@Test
	public void t021testFinalizeAssessment() throws Exception {
		User u = login("admin", "password123");
		ActionProxy proxy = getActionProxy("/portal/Assessment");
		AssessmentView av = (AssessmentView) proxy.getAction();
		av.set_token(av.get_token());
		av.setId("" + this.holdit);
		av.setAction("finalize");
		String result = proxy.execute();
		assertTrue(result == "successJson");

	}

	@Test
	public void t022testAddVerification() throws Exception {
		User u = login("admin", "password123");
		ActionProxy proxy = getActionProxy("/portal/Remediation");
		Remediation rem = (Remediation) proxy.getAction();
		EntityManager em = HibHelper.getInstance().getEM();
		Assessment a = em.find(Assessment.class, this.holdit);
		rem.setAction("create");
		rem.setAsmtId("" + this.holdit);
		rem.setVulnids(new HashMap());
		rem.getVulnids().put(0l, a.getVulns().get(0).getId());
		rem.setAssessorId(u.getId());
		rem.setRemId(u.getId());
		rem.setEdate(new Date());
		rem.setSdate(new Date());
		rem.setDistro("josh.summitt@fusesoft.co");
		rem.setNotes("Credentials go here.");
		String result = proxy.execute();
		assertTrue(result == rem.SUCCESSJSON);

	}

	@Test
	public void t023testFailVerification() throws Exception {
		User u = login("admin", "password123");
		ActionProxy proxy = getActionProxy("/portal/Verifications");
		VerificationQueue vq = (VerificationQueue) proxy.getAction();
		EntityManager em = HibHelper.getInstance().getEM();
		List<Verification> vs = em.createQuery("From Verification").getResultList();
		Verification v = vs.get(vs.size() - 1);
		assertTrue(v.getAssessment().getId().longValue() == this.holdit);
		vq.setVer(v.getId());
		vq.setPass(0l);
		vq.setNotes("Notes about Failed Verification");
		vq.setAction("submit");
		String result = proxy.execute();
		assertTrue(result == vq.SUCCESSJSON);

	}

	@Test
	public void t024testPassOnClosedVerification() throws Exception {
		User u = login("admin", "password123");
		ActionProxy proxy = getActionProxy("/portal/Verifications");
		VerificationQueue vq = (VerificationQueue) proxy.getAction();
		EntityManager em = HibHelper.getInstance().getEM();
		List<Verification> vs = em.createQuery("From Verification").getResultList();
		Verification v = vs.get(vs.size() - 1);
		if (verOption > 0l)
			assertFalse(v.getAssessment().getId().longValue() == this.holdit);
		else {
			assertTrue(v.getAssessment().getId().longValue() == this.holdit);
			vq.setVer(v.getId());
			vq.setPass(0l);
			vq.setNotes("Notes about Failed Verification");
			vq.setAction("submit");
			String result = proxy.execute();
			assertTrue(result == vq.ERRORJSON);
		}

	}

	@Test
	public void t025testCancelVerification() throws Exception {
		User u = login("admin", "password123");
		ActionProxy proxy = getActionProxy("/portal/RemVulnData");
		RemVulnData rem = (RemVulnData) proxy.getAction();
		EntityManager em = HibHelper.getInstance().getEM();
		List<Verification> vs = em.createQuery("From Verification").getResultList();
		Verification v = vs.get(vs.size() - 1);
		if (verOption > 0l)
			assertFalse(v.getAssessment().getId().longValue() == this.holdit);
		else {
			assertTrue(v.getAssessment().getId().longValue() == this.holdit);
			rem.setVulnId(v.getVerificationItems().get(0).getVulnerability().getId());
			rem.setVerId(v.getId());
			rem.setNote("Cancel assessment Test note");
			rem.setAction("closeVerification");
			String result = proxy.execute();
			assertTrue(result == "successJson");
		}

	}

	@Test
	public void t026testCreateAgainVerification() throws Exception {

		User u = login("admin", "password123");
		ActionProxy proxy = getActionProxy("/portal/Remediation");
		Remediation rem = (Remediation) proxy.getAction();
		EntityManager em = HibHelper.getInstance().getEM();
		Assessment a = em.find(Assessment.class, this.holdit);
		rem.setAction("create");
		rem.setAsmtId("" + this.holdit);
		rem.setVulnids(new HashMap());
		rem.getVulnids().put(0l, a.getVulns().get(0).getId());
		rem.setAssessorId(u.getId());
		rem.setRemId(u.getId());
		rem.setEdate(new Date());
		rem.setSdate(new Date());
		rem.setDistro("josh.summitt@fusesoft.co");
		rem.setNotes("Credentials go here.");
		String result = proxy.execute();

		if (verOption == 0l)
			assertTrue(result == rem.SUCCESSJSON);
		else if (verOption == 1l)
			assertTrue(result == rem.SUCCESSJSON);
		else if (verOption == 2l)
			assertTrue(result == rem.SUCCESSJSON);

	}

	@Test
	public void t028testPassVerification() throws Exception {
		User u = login("admin", "password123");
		ActionProxy proxy = getActionProxy("/portal/Verifications");
		VerificationQueue vq = (VerificationQueue) proxy.getAction();
		EntityManager em = HibHelper.getInstance().getEM();
		List<Verification> vs = em.createQuery("From Verification").getResultList();
		Verification v = vs.get(vs.size() - 1);
		assertTrue(v.getAssessment().getId().longValue() == this.holdit);
		vq.setVer(v.getId());
		vq.setPass(0l);
		vq.setNotes("Notes about Failed Verification");
		vq.setAction("submit");
		String result = proxy.execute();

		assertTrue(result == vq.SUCCESSJSON);

	}

	@Test
	public void t029testCloseInDev() throws Exception {
		User u = login("admin", "password123");
		ActionProxy proxy = getActionProxy("/portal/RemVulnData");
		RemVulnData rem = (RemVulnData) proxy.getAction();
		EntityManager em = HibHelper.getInstance().getEM();
		List<Verification> vs = em.createQuery("From Verification").getResultList();
		Verification v = vs.get(vs.size() - 1);
		if (verOption > 0l)
			assertFalse(v.getAssessment().getId().longValue() == this.holdit);
		else {
			assertTrue(v.getAssessment().getId().longValue() == this.holdit);

			rem.setVerId(v.getId());
			rem.setVulnId(v.getVerificationItems().get(0).getVulnerability().getId());
			rem.setNote("Cancel assessment Test note");
			rem.setAction("closeInDev");
			String result = proxy.execute();
			assertTrue(result == rem.SUCCESSJSON);
		}

	}

	@Test
	public void t030testCloseInProd() throws Exception {
		User u = login("admin", "password123");
		ActionProxy proxy = getActionProxy("/portal/RemVulnData");
		RemVulnData rem = (RemVulnData) proxy.getAction();
		EntityManager em = HibHelper.getInstance().getEM();
		List<Verification> vs = em.createQuery("From Verification").getResultList();
		Verification v = vs.get(vs.size() - 1);
		if (verOption > 0l)
			assertFalse(v.getAssessment().getId().longValue() == this.holdit);
		else {
			assertTrue(v.getAssessment().getId().longValue() == this.holdit);

			rem.setVerId(v.getId());
			rem.setVulnId(v.getVerificationItems().get(0).getVulnerability().getId());
			rem.setNote("Cancel assessment Test note");
			rem.setAction("closeInProd");
			String result = proxy.execute();
			assertTrue(result == rem.SUCCESSJSON);
		}

	}

	@Test
	public void t031deleteAssessment() throws Exception {
		User u = login("admin", "password123");
		// Test delete w/o XSRF token
		ActionProxy proxy = getActionProxy("/portal/Engagement");
		Engagement eng = (Engagement) proxy.getAction();
		eng.setAction("delete");
		eng.setAppid("" + holdit);
		String result = proxy.execute();
		// assertNotEquals("Did not Redirect to assesment Queue" , "messageJSON",
		// result);

		// Test with XSRF Token
		proxy = getActionProxy("/portal/Engagement");
		eng = (Engagement) proxy.getAction();
		eng.set_token(eng.get_token());
		eng.setAction("delete");
		eng.setAppid("" + holdit);
		result = proxy.execute();
		assertEquals("Did not Redirect to assesment Queue", "messageJSON", result);

	}

	@Test
	public void t032forcedeleteAssessment() throws Exception {

		// This is just to clean up
		User u = login("admin", "password123");
		ActionProxy proxy = getActionProxy("/portal/Engagement");
		Engagement eng = (Engagement) proxy.getAction();
		eng.forceDelete = true;
		eng.set_token(eng.get_token());
		eng.setAction("delete");
		eng.setAppid("" + holdit);
		String result = proxy.execute();
		assertEquals("Did not Redirect to assesment Queue", eng.SUCCESSJSON, result);

	}

}
