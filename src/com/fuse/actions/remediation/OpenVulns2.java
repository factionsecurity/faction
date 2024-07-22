package com.fuse.actions.remediation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;

import com.fuse.actions.FSActionSupport;
import com.fuse.dao.Assessment;
import com.fuse.dao.Verification;
import com.fuse.dao.Vulnerability;
import com.fuse.dao.query.VulnerabilityQueries;
import com.fuse.utils.FSUtils;
import com.mongodb.BasicDBObject;

@Namespace("/portal")
@Result(name = "success", location = "/WEB-INF/jsp/remediation/OpenVulns.jsp")
public class OpenVulns2 extends FSActionSupport {

	private List<String> risk = new ArrayList();
	private List<Object> junk = new ArrayList();

	@Action(value = "OpenVulns2", results = {
			@Result(name = "vulnsJson", location = "/WEB-INF/jsp/remediation/vulnsJson.jsp") })
	public String execute() {
		
		Map<String, String[]> map= this.request.getParameterMap();
		System.out.println(map.get("order[0][column]")[0]);
		System.out.println(map.get("order[0][dir]")[0]);

		return "vulnsJson";

	}
	public List<String> getRisk() {
		return risk;
	}
	public List<Object> getJunk() {
		return junk;
	}
	
	public class Junk {
		public String test;
		public String getTest() {
			return test;
		}
		public void setTest(String test) {
			this.test = test;
		}
		
	}
}

