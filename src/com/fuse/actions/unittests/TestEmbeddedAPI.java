package com.fuse.actions.unittests;

import static org.junit.Assert.*;

import javax.persistence.EntityManager;

import org.junit.Test;


import com.fuse.dao.HibHelper;
import com.fuse.dao.Verification;

import com.faction.extender.VerificationManager;
import com.fuse.extenderapi.Extensions;

public class TestEmbeddedAPI {

	@Test
	public void test() {
		/*try{
			Extensions amgr = new Extensions();
			EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
			Assessment am = em.find(Assessment.class, 310l);
			amgr.execute(em, am, AssessmentManager.Operation.Create);
			assertTrue(am.getName().equals("Updated by API"));
			assertTrue(am.getVulns().get(0).getTracking().equals("Updated by API"));
			 am = em.find(Assessment.class, 310l);
			amgr.execute(em, am, AssessmentManager.Operation.Delete);
			assertTrue(am.getName().equals("Updated by API"));
			assertTrue(am.getVulns().get(0).getTracking().equals("Updated by API"));
			amgr.execute(em, am, AssessmentManager.Operation.Finalize);
			assertTrue(am.getName().equals("Updated by API"));
			assertTrue(am.getVulns().get(0).getTracking().equals("Updated by API"));
			 am = em.find(Assessment.class, 310l);
			amgr.execute(em, am, AssessmentManager.Operation.PeerReviewAccepted);
			assertTrue(am.getName().equals("Updated by API"));
			assertTrue(am.getVulns().get(0).getTracking().equals("Updated by API"));
			 am = em.find(Assessment.class, 310l);
			amgr.execute(em, am, AssessmentManager.Operation.PeerReviewCompleted);
			assertTrue(am.getName().equals("Updated by API"));
			assertTrue(am.getVulns().get(0).getTracking().equals("Updated by API"));
			 am = em.find(Assessment.class, 310l);
			amgr.execute(em, am, AssessmentManager.Operation.PeerReviewCreated);
			assertTrue(am.getName().equals("Updated by API"));
			assertTrue(am.getVulns().get(0).getTracking().equals("Updated by API"));
			 am = em.find(Assessment.class, 310l);
			amgr.execute(em, am, AssessmentManager.Operation.Update);
			assertTrue(am.getName().equals("Updated by API"));
			assertTrue(am.getVulns().get(0).getTracking().equals("Updated by API"));
			
			em.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}*/
	}
	
	@Test
	public void testVerification() {
		try{
			Extensions amgr = new Extensions(Extensions.EventType.VER_MANAGER);
			EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
			Verification ver = em.find(Verification.class, 189l);
			amgr.execute(em, ver, VerificationManager.Operation.Assigned);
			amgr.execute(em, ver, VerificationManager.Operation.Cancel);
			ver.getVerificationItems().get(0).setNotes("These are some notes PASS");
			amgr.execute(em, ver, VerificationManager.Operation.PASS);
			ver.getVerificationItems().get(0).setNotes("These are some notes FAIL");
			amgr.execute(em, ver, VerificationManager.Operation.FAIL);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
}
