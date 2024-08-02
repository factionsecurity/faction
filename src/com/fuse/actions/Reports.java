package com.fuse.actions;

import javax.servlet.http.HttpSession;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import com.opensymphony.xwork2.interceptor.annotations.Before;

import com.fuse.dao.Assessment;
import com.fuse.dao.AuditLog;
import com.fuse.dao.Vulnerability;
import com.fuse.dao.query.AssessmentQueries;
import com.fuse.tasks.ReportGenThread;
import com.fuse.tasks.TaskQueueExecutor;

import java.util.GregorianCalendar;
import java.util.Calendar;
import java.util.Date;

@Namespace("/portal")
public class Reports extends FSActionSupport{
	
	private Assessment assessment;
	private Long vid; //vulnid
	private Long aid; //assessment id
	private Boolean retest = false;
	
	@Before(priority=1)
	public String authorization() {
		 if(this.isAcassessor() || this.isAcremediation()) { 
			 AuditLog.notAuthorized( this,
				 "Invalid Access to Generate Reports", true);
			 return LOGIN; 
		 }else {
			 return null;
		 }
	}
	
	@Action(value = "GenReport")
	public String generateReport() {
		if(vid != null) {
			Vulnerability vuln = em.find(Vulnerability.class, vid);
			aid = vuln.getAssessorId();
		}
		assessment = em.find(Assessment.class, aid);
		if (assessment != null && assessment.getCompleted() != null)
			return "errorJson";

		if (!AssessmentQueries.checkForReportTemplates(em, assessment, retest)) {
			this._message = "There are no report templates for this assessment. Contact your administrator.";
			return this.ERRORJSON;
		}

		String host = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
		host += request.getContextPath();

		HttpSession session = request.getSession();
		if (assessment.getFinalReport() != null && assessment.getFinalReport().getGentime() != null) {
			session.setAttribute("reportDate|"+aid, assessment.getFinalReport().getGentime());
		} else {
			Calendar dummy = new GregorianCalendar(1980, 1, 1); // Dummy Data
			session.setAttribute("reportDate|"+aid, dummy.getTime());
		}

		ReportGenThread reportThread = new ReportGenThread(host, assessment, assessment.getAssessor(), retest);
		TaskQueueExecutor.getInstance().execute(reportThread);

		return this.SUCCESSJSON;
	}
	
	@Action(value = "CheckStatus", results = {
			@Result(name = "success202", type = "httpheader", params = { "status", "202" }),
			@Result(name = "success200", location = "/WEB-INF/jsp/assessment/SuccessMessageJSON.jsp")

	})
	public String checkStatus() {
		if (!(this.isAcassessor() || this.isAcmanager())) {
			return LOGIN;
		}
		HttpSession session = request.getSession();
		Date lastDate = (Date) session.getAttribute("reportDate|" + aid);
		if (lastDate == null)
			return ERROR;

		Assessment assessment = em.find(Assessment.class, aid);
		if(retest) {
			if (assessment.getRetestReport() == null || assessment.getRetestReport().getGentime().equals(lastDate)) {
				return "success202";
			}
			this._message = "" + assessment.getRetestReport().getGentime();
			
		}
		else{
			if (assessment.getFinalReport() == null || assessment.getFinalReport().getGentime().equals(lastDate)) {
				return "success202";
			}
			this._message = "" + assessment.getFinalReport().getGentime();
		}

		return "success200";

	}
	
	public void setVid(Long vid) {
		this.vid = vid;
	}
	public void setAid(Long aid) {
		this.aid = aid;
	}
	public void setRetest(Boolean retest) {
		this.retest = retest;
	}

}
