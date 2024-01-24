package com.fuse.extenderapi.unittests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.persistence.EntityManager;

import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.faction.elements.results.InventoryResult;
import com.faction.extender.AssessmentManager.Operation;
import com.fuse.dao.AppStore;
import com.fuse.dao.Assessment;
import com.fuse.dao.HibHelper;
import com.fuse.dao.User;
import com.fuse.dao.Verification;
import com.fuse.dao.VerificationItem;
import com.fuse.dao.Vulnerability;
import com.fuse.extenderapi.Extensions;


public class ModuleOrderTests {
	
	private EntityManager em;
	private Assessment assessment;
	private Vulnerability vuln;
	private User user;
	private AppStore app1;
	private AppStore app2;
	private Verification verification;
	private VerificationItem item;
	
	@Before
	public final void setUp() throws IOException, ParseException {
		em = HibHelper.getInstance().getEMF().createEntityManager();
		user = new User();
		user.setUsername("test.user");
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		em.persist(user);
		HibHelper.getInstance().commit();
		
		FileInputStream fis = new FileInputStream("extra/test2/Extension1.jar");
		app1 = new AppStore();
		app1.parseJar(fis);
		app1.setEnabled(true);
		app1.setOrder(1);
		fis.close();
		
		FileInputStream fis2 = new FileInputStream("extra/test2/Extension2.jar");
		app2 = new AppStore();
		app2.parseJar(fis2);
		app2.setEnabled(true);
		app2.setOrder(2);
		fis2.close();
		
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
		em.persist(app1);
		em.persist(app2);
		HibHelper.getInstance().commit();
		
		vuln.setAssessmentId(assessment.getId());
		assessment.getVulns().add(vuln);
		
		verification = new Verification();
		verification.setAssessment(assessment);
		verification.setStart(new Date());
		verification.setEnd(new Date());
		verification.setAssessor(null);
		item = new VerificationItem();
		item.setVulnerability(vuln);
		verification.getVerificationItems().add(item);
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		em.persist(item);
		em.persist(verification);
		em.persist(vuln);
		em.persist(assessment);
		HibHelper.getInstance().commit();
	}

	@Test
	public void testAssessmentManagerExtension() throws InterruptedException, ExecutionException {
		Extensions ex = new Extensions(Extensions.EventType.ASMT_MANAGER);
		assertTrue(assessment.getVulns().get(0).getTracking().matches("^VID-[0-9]+"));
		CompletableFuture<Boolean> future = ex.execute(assessment, Operation.Finalize);
		Boolean result = future.get(); 
		assertTrue(result);
		em.close();
		
		em = HibHelper.getInstance().getEMF().createEntityManager();
		Assessment updatedAssessment = em.find(Assessment.class, assessment.getId());
		System.out.println(updatedAssessment.getVulns().get(0).getTracking());
		assertTrue(updatedAssessment.getVulns().get(0).getTracking().equals("TEST-123-Extension2"));
	
		swapOrder();
		
		Extensions ex2 = new Extensions(Extensions.EventType.ASMT_MANAGER);
		CompletableFuture<Boolean> future2 = ex2.execute(updatedAssessment, Operation.Finalize);
		Boolean result2 = future2.get(); 
		assertTrue(result2);
		em.close();
		
		em = HibHelper.getInstance().getEMF().createEntityManager();
		Assessment updatedAssessment2 = em.find(Assessment.class, assessment.getId());
		assertTrue(updatedAssessment2.getVulns().get(0).getTracking().equals("TEST-123"));
		
	}
	
	@Test
	public void testVerificationManagerExtension() throws InterruptedException, ExecutionException {
		Extensions ex = new Extensions(Extensions.EventType.VER_MANAGER);
		CompletableFuture<Boolean> future =ex.execute(verification, com.faction.extender.VerificationManager.Operation.FAIL);
		Boolean result = future.get(); 
		assertTrue(result);
		em.close();
		em = HibHelper.getInstance().getEMF().createEntityManager();
		vuln = em.find(Vulnerability.class, vuln.getId());
		assertTrue(vuln.getDescription().equals("Updated By Fail-Extension2"));
		
		swapOrder();
		
		Extensions ex2 = new Extensions(Extensions.EventType.ASMT_MANAGER);
		CompletableFuture<Boolean> future2 =ex2.execute(verification, com.faction.extender.VerificationManager.Operation.FAIL);
		Boolean result2 = future2.get(); 
		assertTrue(result2);
		em.close();
		
		em = HibHelper.getInstance().getEMF().createEntityManager();
		Vulnerability vuln2 = em.find(Vulnerability.class, vuln.getId());
		assertTrue(vuln2.getDescription().equals("Updated By Fail"));
		
	}
	
	@Test
	public void testVulnerabilityManagerExtension() throws InterruptedException, ExecutionException {
		Extensions ex = new Extensions(Extensions.EventType.VULN_MANAGER);
		assertTrue(assessment.getVulns().get(0).getTracking().matches("^VID-[0-9]+"));
		CompletableFuture<Boolean> future =ex.execute(assessment, vuln, com.faction.extender.VulnerabilityManager.Operation.Update);
		Boolean result = future.get(); 
		assertTrue(result);
		em.close();
		em = HibHelper.getInstance().getEMF().createEntityManager();
		vuln = em.find(Vulnerability.class, vuln.getId());
		assertTrue(vuln.getDescription().equals("Updated By Update-Extension2"));
		
		swapOrder();
		
		Extensions ex2 = new Extensions(Extensions.EventType.VULN_MANAGER);
		CompletableFuture<Boolean> future2 =ex2.execute(assessment, vuln, com.faction.extender.VulnerabilityManager.Operation.Update);
		Boolean result2 = future2.get(); 
		assertTrue(result2);
		em.close();
		
		em = HibHelper.getInstance().getEMF().createEntityManager();
		vuln = em.find(Vulnerability.class, vuln.getId());
		assertTrue(vuln.getDescription().equals("Updated By Update"));
	}
	
	@Test
	public void testApplicationInventoryExtension() {
		Extensions ex = new Extensions(Extensions.EventType.INVENTORY);
		List<InventoryResult> results = ex.execute("fakeid", "fakeName");
		for(InventoryResult result : results) {
			System.out.println(result.getApplicationId());
		}
		assertTrue(results.get(0).getApplicationId().equals("1234"));
		assertTrue(results.get(1).getApplicationId().equals("1235"));
		assertTrue(results.get(2).getApplicationId().equals("1234-Extension2"));
		assertTrue(results.get(3).getApplicationId().equals("1235-Extension2"));
		
		swapOrder();
		
		Extensions ex2 = new Extensions(Extensions.EventType.INVENTORY);
		List<InventoryResult> results2 = ex2.execute("fakeid", "fakeName");
		assertTrue(results2.get(0).getApplicationId().equals("1234-Extension2"));
		assertTrue(results2.get(1).getApplicationId().equals("1235-Extension2"));
		assertTrue(results2.get(2).getApplicationId().equals("1234"));
		assertTrue(results2.get(3).getApplicationId().equals("1235"));
	}
	
	@After
	public final void tearDown() {
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		item = em.find(VerificationItem.class, item.getId());
		verification = em.find(Verification.class, verification.getId());
		assessment = em.find(Assessment.class, assessment.getId());
		vuln = em.find(Vulnerability.class, vuln.getId());
		user = em.find(User.class, user.getId());
		app1 = em.find(AppStore.class, app1.getId());
		app2 = em.find(AppStore.class, app2.getId());
		em.remove(item);
		em.remove(verification);
		em.remove(assessment);
		em.remove(vuln);
		em.remove(user);
		em.remove(app1);
		em.remove(app2);
		HibHelper.getInstance().commit();
		if(em != null)
			em.close();
	}
	
	public void swapOrder() {
		app1 = em.find(AppStore.class, app1.getId());
		app2 = em.find(AppStore.class, app2.getId());
		app1.setOrder(2);
		app2.setOrder(1);
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		em.persist(app1);
		em.persist(app2);
		HibHelper.getInstance().commit();
		em.close();
		em = HibHelper.getInstance().getEMF().createEntityManager();
	}

}
