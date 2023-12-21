package com.fuse.extenderapi;

import static org.junit.Assert.*;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import org.junit.Test;

import com.faction.extender.AssessmentManager.Operation;
import com.fuse.dao.Assessment;
import com.fuse.dao.Vulnerability;


public class TestAIModule {
	

	@Test
	public void test() {
	
		Extensions ex = new Extensions(Extensions.EventType.ASMT_MANAGER);
		Assessment assessment = new Assessment();
		Vulnerability vuln = new Vulnerability();
		assessment.setName("Test Assessment");
		assessment.setId(1l);
		assessment.setStart(new Date());
		assessment.setEnd(new Date());
		vuln.setAssessmentId(1l);
		vuln.setId(1l);
		vuln.setOverall(1l);
		vuln.setLikelyhood(1l);
		vuln.setImpact(1l);
		vuln.setName("Test Vuln");
		vuln.setDescription("Test Desc");
		vuln.setRecommendation("Test Rec");
		vuln.setDetails("Test Details");
		List<Vulnerability> vulns = new ArrayList<>();
		vulns.add(vuln);
		assessment.setVulns(vulns);
		ex.execute(null, assessment, Operation.Finalize);
		
	}

}
