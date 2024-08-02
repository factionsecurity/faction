package com.fuse.utils;

import java.util.Date;

public class History {
	private Date opened;
	private Date closed;
	private String vuln;
	private String report;
	private String severity;
	private String assessor;

	public History(Date opened, Date closed, String vuln, String Report, String severity, String assessor) {
		this.opened = opened;
		this.closed = closed;
		this.vuln = vuln;
		this.report = Report != null ? Report.replace("/tmp/", "") : null;
		this.severity = severity;
		this.assessor = assessor;
	}

	public Date getOpened() {
		return opened;
	}

	public void setOpened(Date opened) {
		this.opened = opened;
	}

	public Date getClosed() {
		return closed;
	}

	public void setClosed(Date closed) {
		this.closed = closed;
	}

	public String getVuln() {
		return vuln;
	}

	public void setVuln(String vuln) {
		this.vuln = vuln;
	}

	public String getReport() {
		return report;
	}

	public void setReport(String report) {
		this.report = report;
	}

	public String getSeverity() {
		return severity;
	}

	public void setSeverity(String severity) {
		this.severity = severity;
	}

	public String getAssessor() {
		return assessor;
	}

	public void setAssessor(String assessor) {
		this.assessor = assessor;
	}
	

}
