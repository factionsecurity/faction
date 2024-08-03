package com.fuse.actions.retests;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;

import com.fuse.actions.FSActionSupport;
import com.fuse.dao.AuditLog;
import com.fuse.dao.Files;
import com.fuse.dao.FinalReport;
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
	private String appType;
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
	private Boolean isPass;
	private String badges;
	private List<FinalReport>reports = new ArrayList<>(); 

	@Action(value = "VerificationEdit")
	public String execute() {
		if (!this.isAcremediation())
			return AuditLog.notAuthorized(this, "User is not on the Remdiation Team", true);

		Verification v = em.find(Verification.class, searchId);
		this.appId = v.getAssessment().getAppId();
		this.appName = v.getAssessment().getName();
		this.appType = v.getAssessment().getType().getType();
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
		this.isPass = v.getCompleted().getTime() != 0? v.getVerificationItems().get(0).isPass(): null;
		
		if(v.getAssessment().getFinalReport() != null) {
			reports.add(v.getAssessment().getFinalReport());
		}
		if(v.getAssessment().getRetestReport() != null) {
			FinalReport retestReport = v.getAssessment().getRetestReport();
			retestReport.setRetest(true);
			reports.add(retestReport);
		}

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
		
		this.badges = this.createBadges(v);

		return SUCCESS;
	}
	
	private String addBadge(String title, String color, String icon) {
		return String.format("<small class=\"badge badge-%s\"><i class=\"fa %s\"></i>%s</small>",
				color,
				icon,
				title);
	}
	
	private Date getVerificationWarning(Date end,int days){
		Calendar dueDate =  Calendar.getInstance();
		dueDate.setTime(end);
		dueDate.add(Calendar.DAY_OF_YEAR, - days);
		return dueDate.getTime();
	}
	
	private Date getWarning(EntityManager em, Date start, int Level){
		RiskLevel level = (RiskLevel)em.createQuery("from RiskLevel where riskId = :id")
				.setParameter("id", Level).getResultList()
				.stream().findFirst().orElse(null);
		if(level.getDaysTillWarning() == null)
			return null;
		Calendar dueDate =  Calendar.getInstance();
		dueDate.setTime(start);
		dueDate.add(Calendar.DAY_OF_YEAR, level.getDaysTillWarning());
		return dueDate.getTime();
	}
	private Date getDue(EntityManager em, Date start, int Level){
		RiskLevel level = (RiskLevel)em.createQuery("from RiskLevel where riskId = :id")
				.setParameter("id", Level).getResultList()
				.stream().findFirst().orElse(null);
		if(level.getDaysTillDue() == null)
			return null;
		Calendar dueDate =  Calendar.getInstance();
		dueDate.setTime(start);
		dueDate.add(Calendar.DAY_OF_YEAR, level.getDaysTillDue());
		return dueDate.getTime();
	}
	
	private String createBadges(Verification verification) {
		String badges = "";
		if(verification.getWorkflowStatus().equals(Verification.AssessorCancelled)) {
			badges += this.addBadge("Verification Cancelled", "yellow", "fa-times");
		}else {
			
			if(verification.getEnd().getTime() < (new Date().getTime())) {
				badges += this.addBadge("Verification Past Due", "red", "fa-calendar");
			}
			else if(verification.getEnd().getTime() < (this.getVerificationWarning(new Date(), 2)).getTime() ) {
				badges += this.addBadge("Verification Almost Due", "yellow", "fa-calendar");
			}
			
			if(verification.getVerificationItems().get(0).isPass()) {
				badges += this.addBadge("Verification Passed", "green", "fa-check");
			}else if(verification.getCompleted() != null && verification.getCompleted().getTime() > 0) {
				badges += this.addBadge("Verification Failed", "red", "fa-times");
			}else{
				badges += this.addBadge("In Retest", "green", "fa-calendar");
			}
		}
		Vulnerability tmpVuln = verification.getVerificationItems().get(0).getVulnerability();
		Date DueDate = this.getDue(em, tmpVuln.getOpened(), tmpVuln.getOverall().intValue());
		Date WarnDate = this.getWarning(em, tmpVuln.getOpened(), tmpVuln.getOverall().intValue());
		String pattern = "MM/dd/yyyy";
		SimpleDateFormat format = new SimpleDateFormat(pattern);
		
		String dueDateString = format.format(DueDate);
		
		if(DueDate != null && DueDate.getTime() <= (new Date()).getTime()){
			badges += this.addBadge("Vulnerability Past Due (" + dueDateString +")", "red", "fa-bug");
		}
		else if(WarnDate != null && WarnDate.getTime() <= (new Date()).getTime()){
			badges += this.addBadge("Vulnerability Approaching Due Date (" + dueDateString +")", "yellow", "fa-bug");
		}
		return badges;
		
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
	
	public Boolean isPass() {
		return this.isPass;
	}
	
	public String getBadges() {
		return this.badges;
	}
	public String getAppType() {
		return this.appType;
	}
	public List<FinalReport> getReports(){
		return this.reports;
	}

}
