package com.fuse.extenderapi.unittests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.persistence.EntityManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.faction.extender.AssessmentManager.Operation;
import com.faction.extender.InventoryResult;
import com.fuse.dao.Assessment;
import com.fuse.dao.HibHelper;
import com.fuse.dao.User;
import com.fuse.dao.Verification;
import com.fuse.dao.VerificationItem;
import com.fuse.dao.Vulnerability;
import com.fuse.extenderapi.Extensions;
import com.fuse.extenderapi.Extensions.EventType;


public class TestExtensionAPIModule {
	
	private EntityManager em;
	private Assessment assessment;
	private Vulnerability vuln;
	private User user;
	
	@Before
	public final void setUp() {
		em = HibHelper.getInstance().getEMF().createEntityManager();
		user = new User();
		user.setUsername("test.user");
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		em.persist(user);
		HibHelper.getInstance().commit();
		
		assessment = new Assessment();
		vuln = new Vulnerability();
		assessment.setName("Test Assessment");
		assessment.setStart(new Date());
		assessment.setEnd(new Date());
		assessment.setAssessor(Arrays.asList(user));
		assessment.setRemediation(user);
		assessment.setEngagement(user);
		vuln.setOverall(1l);
		vuln.setLikelyhood(1l);
		vuln.setImpact(1l);
		vuln.setName("Test Vuln");
		vuln.setDescription("Test Desc");
		vuln.setRecommendation("Test Rec");
		vuln.setDetails("Test Details");
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		em.persist(assessment);
		HibHelper.getInstance().commit();
		
		vuln.setAssessmentId(assessment.getId());
		assessment.getVulns().add(vuln);
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		em.persist(vuln);
		em.persist(assessment);
		HibHelper.getInstance().commit();
	}

	@Test
	public void testAssessmentManagerExtension() throws InterruptedException, ExecutionException {
		Extensions ex = new Extensions(Extensions.EventType.ASMT_MANAGER, "extra/test");
		assertTrue(assessment.getVulns().get(0).getTracking().matches("^VID-[0-9]+"));
		CompletableFuture<Boolean> future = ex.execute(em, assessment, Operation.Finalize);
		while (!future.isDone()) {
			Thread.sleep(200);
		}
		Boolean result = future.get(); 
		assertTrue(result);
		System.out.println(assessment.getVulns().get(0).getTracking());
		assertTrue(assessment.getVulns().get(0).getTracking().equals("TEST-123"));
	}
	
	@Test
	public void testVerificationManagerExtension() throws InterruptedException, ExecutionException {
		Extensions ex = new Extensions(Extensions.EventType.VER_MANAGER, "extra/test");
		Verification verification = new Verification();
		verification.setAssessment(assessment);
		verification.setStart(new Date());
		verification.setEnd(new Date());
		verification.setAssessor(null);
		VerificationItem item = new VerificationItem();
		item.setVulnerability(vuln);
		verification.getVerificationItems().add(item);
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		em.persist(verification);
		HibHelper.getInstance().commit();
		
		CompletableFuture<Boolean> future =ex.execute(em, verification, com.faction.extender.VerificationManager.Operation.FAIL);
		while (!future.isDone()) {
			Thread.sleep(200);
		}
		Boolean result = future.get(); 
		assertTrue(result);
		assertTrue(vuln.getDescription().contains("Updated By Fail"));
	}
	
	@Test
	public void testVulnerabilityManagerExtension() throws InterruptedException, ExecutionException {
		Extensions ex = new Extensions(Extensions.EventType.VER_MANAGER, "extra/test");
		assertTrue(assessment.getVulns().get(0).getTracking().matches("^VID-[0-9]+"));
		CompletableFuture<Boolean> future =ex.execute(em, assessment, vuln, com.faction.extender.VulnerabilityManager.Operation.Update);
		while (!future.isDone()) {
			Thread.sleep(200);
		}
		Boolean result = future.get(); 
		assertTrue(result);
		assertTrue(vuln.getDescription().contains("Updated By Update"));
	}
	
	@Test
	public void testApplicationInventoryExtension() {
		Extensions ex = new Extensions(Extensions.EventType.INVENTORY, "extra/test");
		List<InventoryResult> results = ex.execute("fakeid", "fakeName");
		InventoryResult result1 = results.stream().filter( i -> i.getApplicationId().equals("1234")).findFirst().orElse(null);
		assertTrue(result1 != null);
		InventoryResult result2 = results.stream().filter( i -> i.getApplicationId().equals("1235")).findFirst().orElse(null);
		assertTrue(result2 != null);
	}
	
	@After
	public final void tearDown() {
		if(em != null)
			em.close();
	}

}
