package com.fuse.servlets.unittests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.text.ParseException;

import javax.persistence.EntityManager;

import org.junit.Test;

import com.fuse.dao.CustomType;
import com.fuse.dao.HibHelper;
import com.fuse.servlets.uploadAssessment;

public class uploadAssessmentTest extends uploadAssessment{

	@Test
	public void assessmentCreations() {
		String csv = "\n12345,20170709,5,Manual Ethical Hacking,Josh Summitt,2016 Assessments,{}\n"
				+ "264176,20170709,5,meh,Josh Summitt,2017 Assessments,\"{'appDesc':'Test','tenDot':'12345'}\"\n";
		EntityManager em =HibHelper.getInstance().getEMF().createEntityManager();
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		CustomType appDesc = new CustomType();
		appDesc.setKey("appDesc");
		appDesc.setVariable("appDesc");
		em.persist(appDesc);
		CustomType tenDot = new CustomType();
		tenDot.setKey("tenDot");
		tenDot.setVariable("tenDot");
		em.persist(tenDot);
		HibHelper.getInstance().commit();
		
		
		try{
			try {
				this.createAssessment(csv, em);
			} catch (org.json.simple.parser.ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (NumberFormatException | IOException | ParseException e) {
			
			e.printStackTrace();
			fail("Exception");
		}finally{
			em.close();
		}		
	}
}