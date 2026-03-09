package com.fuse.extenderapi.unittests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.persistence.EntityManager;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.faction.elements.results.InventoryResult;
import com.faction.elements.utils.Log;
import com.faction.extender.AssessmentManager.Operation;
import com.fuse.dao.AppStore;
import com.fuse.dao.Assessment;
import com.fuse.dao.HibHelper;
import com.fuse.dao.User;
import com.fuse.dao.Verification;
import com.fuse.dao.VerificationItem;
import com.fuse.dao.Vulnerability;
import com.fuse.extenderapi.Extensions;


public class ExtensionAPIModuleTests {
	
	private EntityManager em;
	private Assessment assessment;
	private Vulnerability vuln;
	private User user;
	private AppStore app;
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
		
		File folder = new File("extra/test1");
		String fileName = folder.list()[0];
		FileInputStream fis = new FileInputStream("extra/test1/"+fileName);
		app = new AppStore();
		app.parseJar(fis);
		app.setEnabled(true);
		
		JSONObject configs = new JSONObject();
		JSONObject basic = new JSONObject();
		basic.put("type", "text");
		basic.put("value", "testValue");
		configs.put("testKey", basic.clone());
		
		basic.put("value", "testValue1");
		configs.put("testKey1", basic.clone());
		
		basic.put("value", "testValue2");
		configs.put("testKey2", basic.clone());
		app.setJSONConfig(configs);
		
		
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
	public void testGetConfigs(){
		HashMap<String,String> configs = app.getHashMapConfig();
		assertTrue(configs.get("testKey").equals("testValue"));
		assertTrue(configs.get("testKey1").equals("testValue1"));
		assertTrue(configs.get("testKey2").equals("testValue2"));
	}
	
	@Test
	public void testSetConfigs(){
		app.updateJSONConfig("testKey", "updatedValue");
		app.updateJSONConfig("testKey1", "updatedValue1");
		app.updateJSONConfig("testKey2", "updatedValue2");
		
		HashMap<String,String> updatedConfigs = app.getHashMapConfig();
		assertTrue(updatedConfigs.get("testKey").equals("updatedValue"));
		assertTrue(updatedConfigs.get("testKey1").equals("updatedValue1"));
		assertTrue(updatedConfigs.get("testKey2").equals("updatedValue2"));
	}

	@Test
	public void testAssessmentManagerExtension() throws InterruptedException, ExecutionException {
		Extensions ex = new Extensions(Extensions.EventType.ASMT_MANAGER);
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
	public void testAssessmentManagerConfigsAndLogs() throws InterruptedException, ExecutionException {
		Extensions ex = new Extensions(Extensions.EventType.ASMT_MANAGER);
		assertTrue(assessment.getVulns().get(0).getTracking().matches("^VID-[0-9]+"));
		CompletableFuture<Boolean> future = ex.execute(assessment, Operation.Finalize);
		while (!future.isDone()) {
			Thread.sleep(200);
		}
		Boolean result = future.get(); 
		assertTrue(result);
		em.close();
		em = HibHelper.getInstance().getEMF().createEntityManager();
		List<Log> logs = ex.getLogs();
		assertTrue(logs.size()>0);
		assertTrue(logs.get(0).getMessage().equals("testValue"));
		
	}
	
	@Test
	public void testVerificationManagerExtension() throws InterruptedException, ExecutionException {
		Extensions ex = new Extensions(Extensions.EventType.VER_MANAGER);
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
		Extensions ex = new Extensions(Extensions.EventType.VER_MANAGER);
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
		Extensions ex = new Extensions(Extensions.EventType.INVENTORY);
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
		if(em != null)
			em.close();
	}

}
