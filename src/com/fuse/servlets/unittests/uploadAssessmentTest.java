package com.fuse.servlets.unittests;

import static org.junit.Assert.*;

import java.io.IOException;

import javax.persistence.EntityManager;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Test;

import com.fuse.dao.CustomType;
import com.fuse.dao.HibHelper;
import com.fuse.dao.User;
import com.fuse.servlets.uploadAssessment;

public class uploadAssessmentTest extends uploadAssessment{

	@Test
	public void assessmentCreations() throws IOException {
		// Columns: AppID, AppName, Start Date (yyyyMMdd), Days, Type, Assessors, Campaign, Custom Fields
		// Row 1: valid, known assessor -> added with no warning.
		// Row 2: unknown assessor -> still added, but with a warning.
		// Row 3: invalid start date -> fatal error, not added.
		String csv = "AppID,AppName,Start Date,Days,Type,Assessors,Campaign,Custom Fields\n"
				+ "12345,PCI Assessment,20170709,5,Manual Ethical Hacking,Josh Summitt,2016 Assessments,{}\n"
				+ "264176,Web Services,20170709,5,Source Code,John Doe,2017 Assessments,\"{'appDesc':'Test','tenDot':'12345'}\"\n"
				+ "999,Bad Date App,NOTADATE,5,Source Code,Josh Summitt,2018 Assessments,{}\n";

		EntityManager em = HibHelper.getInstance().getEMF().createEntityManager();
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

		// The assessor referenced in the CSV must resolve to an existing user.
		User assessor = new User();
		assessor.setFname("Josh");
		assessor.setLname("Summitt");
		assessor.setUsername("jsummitt");
		assessor.setEmail("josh@example.com");
		em.persist(assessor);

		HibHelper.getInstance().commit();

		try {
			JSONObject result = this.createAssessment(csv, em);
			assertNotNull(result);

			JSONArray added = (JSONArray) result.get("added");
			JSONArray warnings = (JSONArray) result.get("warnings");
			JSONArray errors = (JSONArray) result.get("errors");

			assertNotNull(added);
			assertNotNull(warnings);
			assertNotNull(errors);

			// The two well-formed rows are added (the unknown assessor is non-fatal).
			assertEquals(2, added.size());
			JSONObject addedRow = (JSONObject) added.get(0);
			assertNotNull(addedRow.get("id"));
			assertEquals("PCI Assessment", addedRow.get("name"));

			// The unknown assessor on row 3 is a non-fatal warning, not an error.
			assertEquals(1, warnings.size());
			JSONObject warningRow = (JSONObject) warnings.get(0);
			assertEquals(3L, ((Number) warningRow.get("row")).longValue());
			assertNotNull(warningRow.get("message"));

			// The invalid date row (row 4) is a fatal error and is not added.
			assertEquals(1, errors.size());
			JSONObject errorRow = (JSONObject) errors.get(0);
			assertEquals(4L, ((Number) errorRow.get("row")).longValue());
			assertNotNull(errorRow.get("message"));
		} finally {
			em.close();
		}
	}
}
