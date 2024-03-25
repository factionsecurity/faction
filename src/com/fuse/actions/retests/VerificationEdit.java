package com.fuse.actions.retests;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;

import com.fuse.actions.FSActionSupport;
import com.fuse.dao.AuditLog;
import com.fuse.dao.Files;
import com.fuse.dao.RiskLevel;
import com.fuse.dao.SystemSettings;
import com.fuse.dao.User;
import com.fuse.dao.Verification;
import com.fuse.dao.VulnNotes;
import com.fuse.dao.Vulnerability;

@Namespace("/portal")
@Result(name = "success", location = "/WEB-INF/jsp/remediation/VerificationEdit.jsp")
public class VerificationEdit extends FSActionSupport {

	private String appId;
	private String appName;
	private Date start;
	private Date end;
	private Long remId;
	private String note;
	private String distro;
	private Long asId;
	private List<User> assessors = new ArrayList<User>();
	private List<User> remediators;
	private List<User> remusers = new ArrayList<User>();;
	private List<VulnNotes> notes;
	private List<Files> files;
	private Long searchId;
	private String startStr;
	private String endStr;
	private Long vulnId;
	private boolean verForm = true;
	private Vulnerability vuln;
	private Long verOption;
	private List<RiskLevel> levels = new ArrayList();

	@Action(value = "VerificationEdit")
	public String execute() {
		if (!this.isAcremediation())
			return AuditLog.notAuthorized(this, "User is not on the Remdiation Team", true);

		// Session session = HibHelper.getSessionFactory().openSession();
		// Verification v = (Verification) session.createQuery("from Verification where
		// id = :id").setLong("id", searchId).uniqueResult();
		Verification v = em.find(Verification.class, searchId);
		// v.getVerificationItems().get(0).getVulnerability().updateRiskLevels();
		this.appId = v.getAssessment().getAppId();
		this.appName = v.getAssessment().getName();
		this.start = v.getStart() == null ? new Date(0) : v.getStart();
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		this.startStr = sdf.format(this.start);
		this.end = v.getEnd() == null ? new Date(0) : v.getEnd();
		this.endStr = sdf.format(this.end);
		this.remId = v.getAssignedRemediation().getId();
		this.note = v.getNotes();
		this.distro = v.getAssessment().getDistributionList();
		this.vulnId = v.getVerificationItems().get(0).getVulnerability().getId();
		this.asId = v.getAssessor().getId();
		this.vuln = v.getVerificationItems().get(0).getVulnerability();
		this.levels = em.createQuery("from RiskLevel order by riskId").getResultList();
		this.files = (List<Files>) em.createQuery("from Files where entityId = :eid and type = :type")
				.setParameter("eid", this.vulnId).setParameter("type", Files.VERIFICATION).getResultList();

		List<User> users = em.createQuery("from User").getResultList();
		for (User u : users) {
			if (u.getPermissions().isAssessor())
				assessors.add(u);
			if (u.getPermissions().isRemediation())
				remusers.add(u);
		}

		SystemSettings ss = (SystemSettings) em.createQuery("from SystemSettings").getResultList().stream().findFirst()
				.orElse(null);
		if (ss != null && ss.getVerificationOption() != null)
			this.verOption = ss.getVerificationOption();
		else
			this.verOption = 0l;

		// session.close();

		return SUCCESS;
	}

	public String getAppId() {
		return appId;
	}

	public String getAppName() {
		return appName;
	}

	public Date getStart() {
		return start;
	}

	public Date getEnd() {
		return end;
	}

	public Long getRemId() {
		return remId;
	}

	public String getNote() {
		return note;
	}

	public String getDistro() {
		return distro;
	}

	public Long getAsId() {
		return asId;
	}

	public List<User> getAssessors() {
		return assessors;
	}

	public List<User> getRemediators() {
		return remediators;
	}

	public List<VulnNotes> getNotes() {
		return notes;
	}

	public List<Files> getFiles() {
		return files;
	}

	public Long getSearchId() {
		return searchId;
	}

	public void setSearchId(Long searchId) {
		this.searchId = searchId;
	}

	public String getStartStr() {
		return startStr;
	}

	public String getEndStr() {
		return endStr;
	}

	public List<User> getRemusers() {
		return remusers;
	}

	public Long getVulnId() {
		return vulnId;
	}

	public boolean isVerForm() {
		return verForm;
	}

	public Vulnerability getVuln() {
		return vuln;
	}

	public Long getVerOption() {
		return verOption;
	}

	public List<RiskLevel> getLevels() {
		return levels;
	}

}
