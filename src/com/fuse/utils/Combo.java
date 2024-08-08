package com.fuse.utils;

import java.util.ArrayList;
import java.util.List;

import com.fuse.dao.Assessment;
import com.fuse.dao.FinalReport;
import com.fuse.dao.Vulnerability;

public class Combo {
	public Assessment assessment;
	public Vulnerability vuln;
	public List<FinalReport>reports = new ArrayList<>();
	public boolean isVer = false;
	public Assessment getAssessment() {
		return this.assessment;
	}
	public Vulnerability getVulnerability() {
		return this.vuln;
	}
	public List<FinalReport> getReports(){
		return this.reports;
	}

}