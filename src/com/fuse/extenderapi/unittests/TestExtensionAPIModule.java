package com.fuse.extenderapi.unittests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
import com.fuse.dao.AppStore;
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
	private AppStore app;
	private Verification verification;
	private VerificationItem item;
	
	@Before
	public final void setUp() throws IOException {
		new File("/tmp/modules").mkdirs();	
		em = HibHelper.getInstance().getEMF().createEntityManager();
		user = new User();
		user.setUsername("test.user");
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		em.persist(user);
		HibHelper.getInstance().commit();
		
		File folder = new File("extra/test1");
		String fileName = folder.list()[0];
		FileInputStream fis = new FileInputStream("extra/test1/"+fileName);
		app = new AppStore();
		app.parseJar(fis);
		app.setEnabled(true);
		
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
		em.persist(app);
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
		Extensions ex = new Extensions(Extensions.EventType.ASMT_MANAGER, "/tmp/modules");
		assertTrue(assessment.getVulns().get(0).getTracking().matches("^VID-[0-9]+"));
		CompletableFuture<Boolean> future = ex.execute(assessment, Operation.Finalize);
		while (!future.isDone()) {
			Thread.sleep(200);
		}
		Boolean result = future.get(); 
		assertTrue(result);
		em.close();
		em = HibHelper.getInstance().getEMF().createEntityManager();
		Assessment updatedAssessment = em.find(Assessment.class, assessment.getId());
		System.out.println(updatedAssessment.getVulns().get(0).getTracking());
		assertTrue(updatedAssessment.getVulns().get(0).getTracking().equals("TEST-123"));
	}
	
	@Test
	public void testVerificationManagerExtension() throws InterruptedException, ExecutionException {
		Extensions ex = new Extensions(Extensions.EventType.VER_MANAGER, "/tmp/modules");
		CompletableFuture<Boolean> future =ex.execute(verification, com.faction.extender.VerificationManager.Operation.FAIL);
		while (!future.isDone()) {
			Thread.sleep(200);
		}
		Boolean result = future.get(); 
		assertTrue(result);
		em.close();
		em = HibHelper.getInstance().getEMF().createEntityManager();
		vuln = em.find(Vulnerability.class, vuln.getId());
		assertTrue(vuln.getDescription().contains("Updated By Fail"));
	}
	
	@Test
	public void testVulnerabilityManagerExtension() throws InterruptedException, ExecutionException {
		Extensions ex = new Extensions(Extensions.EventType.VER_MANAGER, "/tmp/modules");
		assertTrue(assessment.getVulns().get(0).getTracking().matches("^VID-[0-9]+"));
		CompletableFuture<Boolean> future =ex.execute(assessment, vuln, com.faction.extender.VulnerabilityManager.Operation.Update);
		while (!future.isDone()) {
			Thread.sleep(200);
		}
		Boolean result = future.get(); 
		assertTrue(result);
		em.close();
		em = HibHelper.getInstance().getEMF().createEntityManager();
		vuln = em.find(Vulnerability.class, vuln.getId());
		assertTrue(vuln.getDescription().contains("Updated By Update"));
	}
	
	@Test
	public void testApplicationInventoryExtension() {
		Extensions ex = new Extensions(Extensions.EventType.INVENTORY, "/tmp/modules");
		List<InventoryResult> results = ex.execute("fakeid", "fakeName");
		InventoryResult result1 = results.stream().filter( i -> i.getApplicationId().equals("1234")).findFirst().orElse(null);
		assertTrue(result1 != null);
		InventoryResult result2 = results.stream().filter( i -> i.getApplicationId().equals("1235")).findFirst().orElse(null);
		assertTrue(result2 != null);
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
		app = em.find(AppStore.class, app.getId());
		em.remove(item);
		em.remove(verification);
		em.remove(assessment);
		em.remove(vuln);
		em.remove(user);
		em.remove(app);
		HibHelper.getInstance().commit();
		File dir = new File("/tmp/modules");
		File [] tmpFiles = dir.listFiles();
		for(File file : tmpFiles) {
			file.delete();
		}
		if(em != null)
			em.close();
	}

}
