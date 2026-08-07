package com.fuse.actions.retests;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;

import com.fuse.actions.FSActionSupport;
import com.fuse.dao.Assessment;
import com.fuse.dao.AuditLog;
import com.fuse.dao.FinalReport;
import com.fuse.dao.HibHelper;
import com.fuse.dao.Notification;
import com.fuse.dao.RiskLevel;
import com.fuse.dao.SystemSettings;
import com.fuse.dao.User;
import com.fuse.dao.Verification;
import com.fuse.dao.VerificationItem;
import com.fuse.dao.VulnNotes;
import com.fuse.dao.Vulnerability;
import com.faction.extender.VerificationManager;
import com.fuse.extenderapi.Extensions;
import com.fuse.tasks.EmailThread;
import com.fuse.tasks.TaskQueueExecutor;
import com.fuse.utils.FSUtils;
import com.opensymphony.xwork2.interceptor.annotations.Before;

@Namespace("/portal")
@Result(name = "success", location = "/WEB-INF/jsp/retests/VerificationQueue.jsp")
public class VerificationQueue extends FSActionSupport {

	private List<Verification> verifications;
	private Verification verification;
	private Long id;
	private String action = "";
	private Long ver = -1l;
	private Long vid = -1l;
	private String notes;
	private Long pass = -1l;
	// Per-retest close-environment chosen by the assessor: "dev", "staging",
	// "prod", or "" (none). When set on a passing retest it takes precedence
	// over the system-wide SystemSettings.verificationOption.
	private String closeEnv;
	// When a retest fails, the assessor chooses whether to close the
	// verification entirely ("close", default) or send it back to the
	// remediation team ("remediate").
	private String failAction;
	// New overall severity (RiskLevel.riskId) chosen by the assessor during the
	// retest. When it differs from the vulnerability's current overall, the
	// assessor must supply a severityNote explaining the change.
	private Long overall;
	private String severityNote;
	private List<RiskLevel>levels = new ArrayList<>();
	private List<FinalReport> reports = new ArrayList<>();
	private User user;
	
	
	@Before(priority=1)
	public String authorization() {
		if (!(this.isAcassessor() || this.isAcmanager())) {
			return AuditLog.notAuthorized(this, "User is not an Assessor or Manager", true);
		}
		user = this.getSessionUser();
		return null;
		
	}

	@Action(value = "Verifications", results = {
			@Result(name = "verification", location = "/WEB-INF/jsp/retests/Verification.jsp") })
	public String execute() {
		verifications = (List<Verification>) em
				.createQuery("from Verification v where v.assessor = :id and v.workflowStatus = :wf1 ")
				.setParameter("id", user).setParameter("wf1", Verification.InAssessorQueue).getResultList();
		levels = em.createQuery("from RiskLevel order by riskId").getResultList();

		if (id != null) {
			for (Verification v : verifications) {
				if (v.getId().longValue() == this.id.longValue()) {
					verification = v;
					verification.getVerificationItems().get(0).getVulnerability().updateRiskLevels(em);
					break;
				}
			}
			Assessment assessment = verification.getAssessment();
			if(assessment.getFinalReport() != null) {
				reports.add(assessment.getFinalReport());
			}
			if(assessment.getRetestReport() != null) {
				reports.add(assessment.getRetestReport());
			}

			return "verification";
		}

		return SUCCESS;
	}
	
	@Action(value = "CompleteVerification")
	public String completeVerification() {
		verifications = (List<Verification>) em
				.createQuery("from Verification v where v.assessor = :id and v.workflowStatus = :wf1 ")
				.setParameter("id", user).setParameter("wf1", Verification.InAssessorQueue).getResultList();
		levels = em.createQuery("from RiskLevel order by riskId").getResultList();
		for (Verification v : verifications) {
			if (v.getId().longValue() == ver.longValue()) {
				if (v.getCompleted() != null && v.getCompleted().getTime() != 0l)
					return this.ERRORJSON;

				HibHelper.getInstance().preJoin();
				em.joinTransaction();

			String vnote = "";
			String envClosed = null;
			boolean closeVerificationOnFail = false;
			VerificationItem vi = v.getVerificationItems().get(0);
			Long originalOverall = vi.getVulnerability().getOverall();

			// Validate severity change before touching the transaction: if the
			// assessor changed the severity they must supply an annotation.
			String severityError = validateSeverityChange(originalOverall, overall, severityNote);
			if (severityError != null) {
				return this.ERRORJSON;
			}

				SystemSettings ss = (SystemSettings) em.createQuery("From SystemSettings").getResultList().stream()
						.findFirst().orElse(null);

				Long verOption = 0l;
				if (ss != null && ss.getVerificationOption() != null)
					verOption = ss.getVerificationOption();

			if (pass == 1l) {
				vi.setPass(true);
				vi.getVulnerability().setStatus(Vulnerability.StatusPassedRetest);
				// The assessor may explicitly close the finding in a specific
				// environment when saving the retest. That choice takes
				// precedence over the system-wide verificationOption default.
				envClosed = applyCloseEnvironment(vi.getVulnerability(), closeEnv);
				if (envClosed != null) {
					vnote = "<span style=color:green > Issue Passed Verification and Closed in the "
							+ envClosed + " Environment.</span><br>" + notes;
				} else if (verOption == 1l) {
					vi.getVulnerability().setDevClosed(new Date());
					vi.getVulnerability().setStatus(Vulnerability.StatusClosedInDev);
					vnote = "<span style=color:green > Issue Passed Verification in the Development Environment.</span><br>"
							+ notes;
				} else if (verOption == 2l) {
					vi.getVulnerability().setClosed(new Date());
					vi.getVulnerability().setStatus(Vulnerability.StatusClosed);
					vnote = "<span style=color:green > Issue Passed Verification in the Production Environment.</span><br>"
							+ notes;

				} else if (verOption == 3l) {
					// TODO add API info here
					// vi.getVulnerability().setClosed(new Date());
					// vnote = "<span style=color:green > Issue Passed Verification in the
					// Production Environment.</span><br>" + notes;

				} else
					vnote = "<span style=color:green > Issue Passed Verification </span><br>" + notes;

			} else if (pass == 0l) {
				vi.setPass(false);
				vi.getVulnerability().setStatus(Vulnerability.StatusFailedRetest);
				if ("remediate".equalsIgnoreCase(failAction)) {
					vnote = "<span style=color:red > Issue Failed Verification - sent to remediation team.</span><br>" + notes;
				} else {
					closeVerificationOnFail = true;
					vnote = "<span style=color:red > Issue Failed Verification - verification closed.</span><br>" + notes;
				}
			} else {

				return "errorJson";

			}
		// Apply the assessor's severity change (if any) and prepend the
		// orange label + changeSev table + annotation to the recorded note.
		String severityChangeNote = applySeverityChange(vi.getVulnerability(), originalOverall,
				overall, levels, severityNote);
		if (severityChangeNote != null) {
			vnote = "<small class=\"label pull-left bg-yellow\">Vulnerability Severity Changed</small><br><br>"
					+ severityChangeNote + "<br>" + vnote;
		}
			v.setCompleted(new Date());
			v.setWorkflowStatus(Verification.AssessorCompleted);

			vi.setNotes(notes);
			VulnNotes vn = new VulnNotes();
			vn.setCreatorObj(user);
			vn.setCreator(user.getId());
			vn.setCreated(new Date());
			vn.setNote(vnote);
			vn.setUuid("nodelete");
			vn.setVulnId(vi.getVulnerability().getId());
			Notification notif = new Notification();
			notif.setAssessorId(user.getId());
			notif.setCreated(new Date());
			notif.setMessage("Verification Completed for <b>" + vi.getVulnerability().getName()
					+ "</b>: <a href='../portal/DownloadReport?aid=" + v.getAssessment().getId()
					+ "&retest=true'>Retest Report</a>");
			em.persist(notif);
			em.persist(vn);
			// A finding closed by the assessor (explicit env, or a failed
			// retest where the assessor chose to close the verification) or
			// auto-closed by the system verificationOption (dev/prod) is done
			// with remediation.
			if (envClosed != null || closeVerificationOnFail || verOption == 1l || verOption == 2l) {
				v.setWorkflowStatus(Verification.RemediationCompleted);
				// em.remove(vi);
				// em.remove(v);
			}
				em.persist(vi);
				em.persist(vi.getVulnerability());
				em.persist(v);

				Assessment a = em.find(Assessment.class, vi.getVulnerability().getAssessmentId());

				
				String status = "Passed";
				if (!vi.isPass())
					status = "Failed";
				AuditLog.audit(this, "Issue " + status + " verification.", AuditLog.UserAction,
						AuditLog.CompVulnerability, vi.getVulnerability().getId(), false);
				HibHelper.getInstance().commit();

				String email = "<h2> ReTest for : " + vi.getVulnerability().getName() + "[ "
						+ vi.getVulnerability().getOverallStr() + " ] </h2>";
				email += "<p> The vulnerability was ";
				if (vi.isPass()) {
					email += "<span color='green'><b>Passed</b></span>";
				} else {
					email += "<span color='red'><b>Failed</b></span>";
				}
				email += " by " + user.getFname() + " " + user.getLname() + ".<br/><br/>";
				email += "<u>Additional Information:</u><br/>";
				email += this.notes;

				String Subject = "ReTest " + status + " for " + a.getAppId() + " - " + a.getName() + " - "
						+ vi.getVulnerability().getName() + " [" + vi.getVulnerability().getTracking() + "]";

				EmailThread emailThread = new EmailThread(a, Subject, email);
				TaskQueueExecutor.getInstance().execute(emailThread);
				
				// Run all Extensions
				Extensions vmgr = new Extensions(Extensions.EventType.VER_MANAGER);
				if (vi.isPass())
					vmgr.execute(v, VerificationManager.Operation.PASS);
				else
					vmgr.execute(v, VerificationManager.Operation.FAIL);

				return "successJson";

			}
		}
		return "errorJson";
		
	}

	/**
	 * Applies the assessor's chosen close environment to a vulnerability for a
	 * passing retest. Sets the appropriate closed date and status and returns the
	 * environment's display label, or {@code null} if no environment was chosen.
	 * Pure (noersistence): testable without a DB.
	 */
	protected static String applyCloseEnvironment(Vulnerability vuln, String closeEnv) {
		if (vuln == null || closeEnv == null) {
			return null;
		}
		switch (closeEnv.trim().toLowerCase()) {
			case "dev":
				vuln.setDevClosed(new Date());
				vuln.setStatus(Vulnerability.StatusClosedInDev);
				return "Development";
			case "staging":
				vuln.setStagingClosed(new Date());
				vuln.setStatus(Vulnerability.StatusClosedInStaging);
				return "Staging";
			case "prod":
				vuln.setClosed(new Date());
				vuln.setStatus(Vulnerability.StatusClosed);
				return "Production";
			default:
				return null;
		}
	}

	/**
	 * Validates that an annotation was supplied when the assessor changed the
	 * severity. Returns an error message if the severity changed but the note is
	 * blank, or {@code null} if validation passes (no change, or change + note).
	 * Pure: testable without a DB.
	 */
	protected static String validateSeverityChange(Long originalOverall, Long newOverall, String severityNote) {
		if (newOverall == null || newOverall.equals(originalOverall)) {
			return null;
		}
		if (severityNote == null || severityNote.trim().isEmpty()) {
			return "An annotation is required when changing the severity.";
		}
		return null;
	}

	/**
	 * Resolves a RiskLevel.riskId to its display label using the supplied levels.
	 */
	private static String riskLabel(Long riskId, List<RiskLevel> levels) {
		if (riskId == null || levels == null) {
			return "Unassigned";
		}
		for (RiskLevel level : levels) {
			if (riskId.intValue() == level.getRiskId()) {
				String name = level.getRisk();
				return name == null || name.isEmpty() ? "Unassigned" : name;
			}
		}
		return "Unassigned";
	}

	/**
	 * Applies the assessor's severity change to the vulnerability and returns an
	 * HTML table (matching the remediation team's changeSev format) showing the
	 * previous and new severity, followed by the assessor's annotation. Returns
	 * {@code null} if the severity was not changed. Pure: testable without a DB.
	 */
	protected static String applySeverityChange(Vulnerability vuln, Long originalOverall, Long newOverall,
			List<RiskLevel> levels, String severityNote) {
		if (vuln == null || newOverall == null || newOverall.equals(originalOverall)) {
			return null;
		}
		String oldStr = riskLabel(originalOverall, levels);
		vuln.setOverall(newOverall);
		String newStr = riskLabel(newOverall, levels);
		String table = "<table class=chSevTable>"
				+ "<tr><td></td><td><b>Previous</b></td><td><b>New</b></td></tr>"
				+ "<tr><td><b>Severity:</b></td><td>" + oldStr + "</td><td>" + newStr + "</td></tr>"
				+ "</table>";
		String note = severityNote == null ? "" : severityNote.trim();
		return table + note;
	}

	@Action(value = "CancelVerification")
	public String cancelVerification() {
		if (!(this.isAcassessor() || this.isAcmanager())) {
			return AuditLog.notAuthorized(this, "User is not an Assessor or Manager", true);
		}
		User user = this.getSessionUser();
		Verification v = (Verification) em
				.createQuery("from Verification v where v.assessor = :user and v.id = :id and v.workflowStatus = :wf1 ")
				.setParameter("user", user).setParameter("id", this.ver)
				.setParameter("wf1", Verification.InAssessorQueue).getResultList().stream().findFirst().orElse(null);
		if (v == null)
			return this.ERRORJSON;

		Long vulnId = v.getVerificationItems().get(0).getVulnerability().getId();
		VulnNotes note = new VulnNotes();
		note.setVulnId(vulnId);
		note.setNote(
				"<small class=\"label pull-left bg-blue\">Verification was cancelled by the assessor</small><br><br>"
						+ FSUtils.sanitizeHTML(this.notes));
		note.setCreatorObj(user);
		note.setCreated(new Date());
		note.setUuid("nodelete");
		v.setWorkflowStatus(Verification.AssessorCancelled);
		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		em.persist(note);
		em.persist(v);
		HibHelper.getInstance().commit();
		return this.SUCCESSJSON;

	}

	public String getActiveVerification() {
		return "active";
	}

	public List<Verification> getVerifications() {
		return this.verifications;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Verification getVerification() {
		return verification;
	}

	public void setVerification(Verification verification) {
		this.verification = verification;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public Long getVer() {
		return ver;
	}

	public void setVer(Long ver) {
		this.ver = ver;
	}

	public Long getVid() {
		return vid;
	}

	public void setVid(Long vid) {
		this.vid = vid;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public Long getPass() {
		return pass;
	}

	public void setPass(Long pass) {
		this.pass = pass;
	}

	public String getCloseEnv() {
		return closeEnv;
	}

	public void setCloseEnv(String closeEnv) {
		this.closeEnv = closeEnv;
	}

	public String getFailAction() {
		return failAction;
	}

	public void setFailAction(String failAction) {
		this.failAction = failAction;
	}

	public Long getOverall() {
		return overall;
	}

	public void setOverall(Long overall) {
		this.overall = overall;
	}

	public String getSeverityNote() {
		return severityNote;
	}

	public void setSeverityNote(String severityNote) {
		this.severityNote = severityNote;
	}

	public List<RiskLevel> getLevels() {
		return levels;
	}
	public List<FinalReport> getReports(){
		return this.reports;
	}
}
