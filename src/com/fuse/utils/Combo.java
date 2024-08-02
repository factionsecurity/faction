package com.fuse.utils;

import com.fuse.dao.Assessment;
import com.fuse.dao.Vulnerability;

public class Combo {
	public Assessment assessment;
	public Vulnerability vuln;
	public boolean isVer = false;
	public Assessment getAssessment() {
		return this.assessment;
	}
	public Vulnerability getVulnerability() {
		return this.vuln;
	}

}