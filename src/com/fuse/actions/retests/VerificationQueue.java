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
import com.fuse.dao.HibHelper;
import com.fuse.dao.Notification;
import com.fuse.dao.RiskLevel;
import com.fuse.dao.SystemSettings;
import com.fuse.dao.User;
import com.fuse.dao.Verification;
import com.fuse.dao.VerificationItem;
import com.fuse.dao.VulnNotes;
import com.faction.extender.VerificationManager;
import com.fuse.extenderapi.Extensions;
import com.fuse.tasks.EmailThread;
import com.fuse.tasks.TaskQueueExecutor;
import com.fuse.utils.FSUtils;

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
	private List<RiskLevel>levels = new ArrayList();

	@Action(value = "Verifications", results = {
			@Result(name = "verification", location = "/WEB-INF/jsp/retests/Verification.jsp") })
	public String execute() {
		if (!(this.isAcassessor() || this.isAcmanager())) {
			return AuditLog.notAuthorized(this, "User is not an Assessor or Manager", true);
		}
		User user = this.getSessionUser();

		verifications = (List<Verification>) em
				.createQuery("from Verification v where v.assessor = :id and v.workflowStatus = :wf1 ")
				.setParameter("id", user).setParameter("wf1", Verification.InAssessorQueue).getResultList();
		levels = em.createQuery("from RiskLevel order by riskId").getResultList();

		if (action.startsWith("submit")) {

			for (Verification v : verifications) {
				if (v.getId().longValue() == ver.longValue()) {
					if (v.getCompleted() != null && v.getCompleted().getTime() != 0l)
						return this.ERRORJSON;

					HibHelper.getInstance().preJoin();
					em.joinTransaction();

					String vnote = "";
					VerificationItem vi = v.getVerificationItems().get(0);

					SystemSettings ss = (SystemSettings) em.createQuery("From SystemSettings").getResultList().stream()
							.findFirst().orElse(null);

					Long verOption = 0l;
					if (ss != null && ss.getVerificationOption() != null)
						verOption = ss.getVerificationOption();

					if (pass == 1l) {
						vi.setPass(true);
						if (verOption == 1l) {
							vi.getVulnerability().setDevClosed(new Date());
							vnote = "<span style=color:green > Issue Passed Verification in the Development Environment.</span><br>"
									+ notes;
						} else if (verOption == 2l) {
							vi.getVulnerability().setClosed(new Date());
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
						vnote = "<span style=color:red > Issue Failed Verification </span><br>" + notes;
					} else {

						return "errorJson";

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
							+ "</b>: <a href='../service/Report.pdf?retest=true&id=" + v.getAssessment().getId()
							+ "'>Retest Report</a>");
					em.persist(notif);
					em.persist(vn);
					if (verOption == 1l || verOption == 2l) {
						v.setWorkflowStatus(Verification.RemediationCompleted);
						// em.remove(vi);
						// em.remove(v);
					}
					em.persist(vi);
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

		} else if (id != null) {
			for (Verification v : verifications) {
				if (v.getId().longValue() == this.id.longValue()) {
					verification = v;
					verification.getVerificationItems().get(0).getVulnerability().updateRiskLevels(em);
					break;
				}
			}

			return "verification";
		}

		return SUCCESS;
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

	public List<RiskLevel> getLevels() {
		return levels;
	}
}
