package com.fuse.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import javax.persistence.Version;

import com.fuse.utils.FSUtils;

@Entity
public class Assessment {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "assGen")
	@TableGenerator(name = "assGen", table = "assGenseq", pkColumnValue = "ass", valueColumnName = "nextAss", initialValue = 1, allocationSize = 1)
	private Long id;
	@Version
	private Integer version;
	private String summary;
	private String riskAnalysis;
	private String name;
	@ManyToOne
	private User engagement;
	@OneToMany(fetch = FetchType.EAGER)
	private List<User> assessor;
	@ManyToOne
	private User remediation;
	private String appId;
	private Date start;
	private Date end;
	private Date completed;
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CustomField> CustomFields;
	private String Notes;
	private String DistributionList;
	private String AccessNotes;
	@ManyToOne
	private AssessmentType type;
	@OneToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST })
	private FinalReport finalReport;
	@OneToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST })
	private FinalReport retestReport;
	@ManyToOne
	private Campaign campaign;
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CheckListAnswers> answers = new ArrayList();

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Vulnerability> vulns = new ArrayList();

	private String pr_sum_notes;
	private String pr_risk_notes;
	private String guid = UUID.randomUUID().toString();
	private Boolean peerreview;
	private String status;
	private Integer workflow = 0;

	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private PeerReview peerReview;

	private Boolean notesLock = false;
	@ManyToOne
	private User notes_locked_by;
	private Date notes_lock_time;
	private Boolean summary_lock = false;
	@ManyToOne
	private User summary_locked_by;
	private Date summary_lock_time;
	private Boolean risk_lock = false;
	@ManyToOne
	private User risk_locked_by;
	private Date risk_lock_time;
	private Boolean cvss31=false;
	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		summary = FSUtils.sanitizeHTML(summary);
		this.summary = summary;
	}

	public String getRiskAnalysis() {

		return this.riskAnalysis;
	}

	public void setRiskAnalysis(String riskAnalysis) {
		riskAnalysis = FSUtils.sanitizeHTML(riskAnalysis);
		this.riskAnalysis = riskAnalysis;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public User getEngagement() {
		return engagement;
	}

	public void setEngagement(User engagement) {
		this.engagement = engagement;
	}

	public List<User> getAssessor() {
		return assessor;
	}

	public void setAssessor(List<User> assessor) {
		this.assessor = (List<User>) assessor;
	}

	public List<Vulnerability> getVulns() {
		return this.vulns;
	}

	public void setVulns(List<Vulnerability> vulns) {
		this.vulns = (List<Vulnerability>) vulns;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public Date getCompleted() {
		return completed;
	}

	public void setCompleted(Date completed) {
		this.completed = completed;
	}

	public List<CustomField> getCustomFields() {
		return CustomFields;
	}

	public void setCustomFields(List<CustomField> customFields) {
		CustomFields = customFields;
	}

	public String getNotes() {
		return Notes;
	}

	public void setNotes(String notes) {
		notes = FSUtils.sanitizeHTML(notes);
		Notes = notes;
	}

	public String getDistributionList() {
		return DistributionList;
	}

	public void setDistributionList(String distributionList) {
		DistributionList = distributionList;
	}

	public String getAccessNotes() {
		return AccessNotes;
	}

	public void setAccessNotes(String accessNotes) {
		AccessNotes = accessNotes;
	}

	public User getRemediation() {
		return remediation;
	}

	public void setRemediation(User remediation) {
		this.remediation = remediation;
	}

	public AssessmentType getType() {
		return type;
	}

	public void setType(AssessmentType type) {
		this.type = type;
	}

	public FinalReport getFinalReport() {
		return finalReport;
	}

	public void setFinalReport(FinalReport finalReport) {
		this.finalReport = finalReport;
	}

	public Campaign getCampaign() {
		return campaign;
	}

	public void setCampaign(Campaign campaign) {
		this.campaign = campaign;
	}

	@Transient
	public String getPr_sum_notes() {
		return pr_sum_notes;
	}

	@Transient
	public void setPr_sum_notes(String pr_sum_notes) {
		pr_sum_notes = FSUtils.sanitizeHTML(pr_sum_notes);
		this.pr_sum_notes = pr_sum_notes;
	}

	@Transient
	public String getPr_risk_notes() {
		return pr_risk_notes;
	}

	@Transient
	public void setPr_risk_notes(String pr_risk_notes) {
		pr_risk_notes = FSUtils.sanitizeHTML(pr_risk_notes);
		this.pr_risk_notes = pr_risk_notes;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public FinalReport getRetestReport() {
		return retestReport;
	}

	public void setRetestReport(FinalReport retestReport) {
		this.retestReport = retestReport;
	}

	public List<CheckListAnswers> getAnswers() {
		return answers;
	}

	public void setAnswers(List<CheckListAnswers> answers) {
		this.answers = answers;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public Boolean getPeerreview() {
		return peerreview;
	}

	public void setPeerreview(Boolean peerreview) {
		this.peerreview = peerreview;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public PeerReview getPeerReview() {
		return peerReview;
	}

	public void setPeerReview(PeerReview peerReview) {
		this.peerReview = peerReview;
	}

	public Integer getWorkflow() {
		return workflow;
	}

	public void setWorkflow(Integer workflow) {
		this.workflow = workflow;
	}

	public Boolean isNotesLock() {
		return notesLock != null && notesLock;
	}
	public Boolean getNotesLock() {
		return notesLock != null && notesLock;
	}

	public void setNotesLock(Boolean notesLock) {
		this.notesLock = notesLock;
	}

	public User getNotesLockBy() {
		return notes_locked_by;
	}

	public void setNotesLockBy(User notes_locked_by) {
		this.notes_locked_by = notes_locked_by;
	}

	public Date getNotesLockAt() {
		return notes_lock_time;
	}

	public void setNotesLockAt(Date notes_lock_time) {
		this.notes_lock_time = notes_lock_time;
	}

	public Boolean isSummaryLock() {
		return summary_lock != null && summary_lock;
	}
	public Boolean getSummaryLock() {
		return summary_lock != null && summary_lock;
	}

	public void setSummaryLock(Boolean notes_lock) {
		this.summary_lock = notes_lock;
	}

	public User getSummaryLockBy() {
		return summary_locked_by;
	}

	public void setSummaryLockBy(User notes_locked_by) {
		this.summary_locked_by = notes_locked_by;
	}

	public Date getSummaryLockAt() {
		return summary_lock_time;
	}

	public void setSummaryLockAt(Date notes_lock_time) {
		this.summary_lock_time = notes_lock_time;
	}

	public Boolean isRiskLock() {
		return risk_lock != null && risk_lock;
	}
	public Boolean getRiskLock() {
		return risk_lock != null && risk_lock;
	}

	public void setRiskLock(Boolean notes_lock) {
		this.risk_lock = notes_lock;
	}

	public User getRiskLockBy() {
		return risk_locked_by;
	}

	public void setRiskLockBy(User notes_locked_by) {
		this.risk_locked_by = notes_locked_by;
	}

	public Date getRiskLockAt() {
		return risk_lock_time;
	}

	public void setRiskLockAt(Date notes_lock_time) {
		this.risk_lock_time = notes_lock_time;
	}
	
	public Boolean isCvss31() {
		return cvss31 !=null && cvss31;
	}
	
	public void setCvss31(Boolean cvss31) {
		this.cvss31 = cvss31;
	}

	@Transient
	public void setInPr() {
		this.workflow = 1;
	}

	@Transient
	public void setPrComplete() {
		this.workflow = 2;
	}

	@Transient
	public void setAcceptedEdits() {
		this.workflow = 3;
	}

	@Transient
	public void setFinalized() {
		this.workflow = 4;
	}

	@Transient
	public boolean isFinalized() {
		if (this.workflow != null && this.workflow == 4)
			return true;
		else
			return false;
	}

	@Transient
	public boolean isAcceptedEdits() {
		if (this.workflow != null && this.workflow == 3)
			return true;
		else
			return false;
	}

	@Transient
	public boolean isPrComplete() {
		if (this.workflow != null && this.workflow == 2)
			return true;
		else
			return false;
	}

	@Transient
	public boolean isInPr() {
		if (this.workflow != null && this.workflow == 1)
			return true;
		else
			return false;
	}
	
}
