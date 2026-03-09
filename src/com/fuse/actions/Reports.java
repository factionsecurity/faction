package com.fuse.actions;

import javax.servlet.http.HttpSession;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import com.opensymphony.xwork2.interceptor.annotations.Before;

import com.fuse.dao.Assessment;
import com.fuse.dao.AuditLog;
import com.fuse.dao.FinalReport;
import com.fuse.dao.User;
import com.fuse.dao.Verification;
import com.fuse.dao.Vulnerability;
import com.fuse.dao.query.AssessmentQueries;
import com.fuse.reporting.GenerateReport;
import com.fuse.tasks.ReportGenThread;
import com.fuse.tasks.TaskQueueExecutor;

import java.util.GregorianCalendar;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Namespace("/portal")
public class Reports extends FSActionSupport {

	private Assessment assessment;
	private Long vid; // vulnid
	private Long aid; // assessment id
	private Boolean retest = false;
	private String test;
	private String type;
	private String team;
	private String filename;
	private String contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
	private String guid;
	private InputStream reportStream;

	@Before(priority = 1)
	public String authorization() {
		/// TODO: Need Better Authorization here
		if ( !(this.isAcassessor() || this.isAcremediation() || this.isAcadmin())) {
			AuditLog.notAuthorized(this, "Invalid Access to Reports", true);
			return LOGIN;
		} else {
			return null;
		}
	}

	@Action(value = "GenReport")
	public String generateReport() {
		if (vid != null) {
			Vulnerability vuln = em.find(Vulnerability.class, vid);
			aid = vuln.getAssessorId();
		}
		assessment = em.find(Assessment.class, aid);
		
		// Prevent generating a report on a finalized assessment
		if (assessment != null && assessment.getCompleted() != null && (retest == null || retest == false))
			return "errorJson";

		if (!AssessmentQueries.checkForReportTemplates(em, assessment, retest)) {
			this._message = "There are no report templates for this assessment. Contact your administrator.";
			return this.ERRORJSON;
		}

		String host = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
		host += request.getContextPath();

		HttpSession session = request.getSession();
		if (assessment.getFinalReport() != null && assessment.getFinalReport().getGentime() != null) {
			session.setAttribute("reportDate|" + aid, assessment.getFinalReport().getGentime());
		} else {
			Calendar dummy = new GregorianCalendar(1980, 1, 1); // Dummy Data
			session.setAttribute("reportDate|" + aid, dummy.getTime());
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
		HttpSession session = request.getSession();
		Date lastDate = (Date) session.getAttribute("reportDate|" + aid);
		if (lastDate == null)
			return ERROR;

		Assessment assessment = em.find(Assessment.class, aid);
		if (retest) {
			if (assessment.getRetestReport() == null || assessment.getRetestReport().getGentime().equals(lastDate)) {
				return "success202";
			}
			this._message = "" + assessment.getRetestReport().getGentime();

		} else {
			if (assessment.getFinalReport() == null || assessment.getFinalReport().getGentime().equals(lastDate)) {
				return "success202";
			}
			this._message = "" + assessment.getFinalReport().getGentime();
		}

		return "success200";

	}

	@Action(value = "DownloadReport", results = { 
			@Result(
					name = "report", 
					type = "stream", 
					params = { 
							"inputName", "reportStream",
							"contentType", "${contentType}", 
							"bufferSize", "1024", 
							"contentDisposition", "attachment;filename=\"${filename}\"" 
							}
					) 
			}
	)
	public String downloadReport() {

		User user = this.getSessionUser();
		filename = "Report.";

		if (test == null) {

			String b64Rpt = "";
			Assessment assessment = null;
			FinalReport finalreport = null;
			if (aid != null) {
				assessment = AssessmentQueries.getAssessment(em, user, aid);
				boolean found = false;
				for (User u : assessment.getAssessor()) {
					if (u.getId() == user.getId()) {
						found = true;
						break;
					}
				}
				if (!found) { // check if authorized via verifications
					String mongo = "{ 'assessment_id' : " + assessment.getId() + "}";
					mongo = mongo.replace("'", "\"");
					Verification ver = (Verification) em
							.createNativeQuery(mongo, Verification.class)
							.getResultList()
							.stream().findFirst()
							.orElse(null);
					if (ver != null && ver.getAssessor().getId() == user.getId()) {
						found = true;
					}

				}
				if (retest) {
					// All reports must be placed in a queue to prevent running the server out of
					// memory
					ReportGenThread reportThread = new ReportGenThread("", assessment, assessment.getAssessor(), true);
					TaskQueueExecutor.getInstance().execute(reportThread);
					// wait for it to complete
					int breakit = 0;
					while (!reportThread.complete && breakit < 2 * 20) { // wait 20 seconds
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
							return ERROR;
						}
						breakit++;
					}
					b64Rpt = reportThread.getReport();

				} else {
					finalreport = assessment.getFinalReport();
					b64Rpt = finalreport.getBase64EncodedPdf();
				}
			} else if (guid != null) {

				finalreport = (FinalReport) em.createQuery("from FinalReport where filename = :guid")
						.setParameter("guid", guid).getResultList().stream().findFirst().orElse(null);
				b64Rpt = finalreport.getBase64EncodedPdf();

			} else {
				return ERROR;
			}

			byte[] report;
			try {
				if (b64Rpt == null || b64Rpt.equals("")) {
					return ERROR;
				}

				report = Base64.getDecoder().decode(b64Rpt.getBytes());

				if (finalreport.getRetest())
					filename = "Retest " + filename;
				if (assessment != null)
					filename = assessment.getName() + " - " + assessment.getType().getType() + " " + filename;
				else {
					String query = "{ 'finalReport_id' : " + finalreport.getId() + "}";
					Assessment tmpAsmt = (Assessment) em.createNativeQuery(query, Assessment.class).getResultList()
							.stream().findFirst().orElse(null);
					if (tmpAsmt == null) {
						query = "{ 'retestReport_id' : " + finalreport.getId() + "}";
						tmpAsmt = (Assessment) em.createNativeQuery(query, Assessment.class).getResultList().stream()
								.findFirst().orElse(null);
					}
					if (tmpAsmt != null)
						filename = tmpAsmt.getName() + " - " + tmpAsmt.getType().getType() + " " + filename;
				}
				
				if(report.length > 3 && report[1] == (byte)'P' && report[2] == (byte)'D' && report[3] == (byte)'F') {
					contentType = "application/pdf";
					filename +="pdf";
						
				}else {
					filename +="docx";
				}
				reportStream = new ByteArrayInputStream(report);
				return "report";
			} catch (Exception e) {

				e.printStackTrace();
				return ERROR;
			}
		} else {

			String retest = request.getParameter("retest");
			boolean rt = false;
			if (retest != null && retest.equals("true"))
				rt = true;

			GenerateReport genReport = new GenerateReport();
			byte[] bytes = genReport.testDocxPage(em, Long.parseLong(team), Long.parseLong(type), rt);
			if (bytes == null || bytes.length == 0) {
				return ERROR;
			}
			filename = "Report.docx";
			if(bytes.length > 3 && bytes[1] == (byte)'P' && bytes[2] == (byte)'D' && bytes[3] == (byte)'F') {
				contentType = "application/pdf";
				filename = "Report.pdf";
					
			}
			reportStream = new ByteArrayInputStream(bytes);
			return "report";
		}

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

	public void setTeam(String team) {
		this.team = team;
	}

	public void setTest(String test) {
		this.test = test;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getFilename() {
		return this.filename;
	}
	public String getContentType() {
		return this.contentType;
	}
	public InputStream getReportStream() {
		return this.reportStream;
	}


}
