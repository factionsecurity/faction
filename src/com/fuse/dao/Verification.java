package com.fuse.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;

@Entity
public class Verification {
	
	@Transient
	 public static final String InAssessorQueue = "In Queue";
	@Transient
	 public static final String AssessorCompleted = "Assessor Completed";
	@Transient
	 public static final String RemediationCompleted = "Remediation Completed";
	@Transient
	 public static final String AssessorCancelled = "Assessor Cancelled";
	@Transient
	 public static final String RemdiationCancelled = "Remediation Cancelled";

	
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "verGen")
    @TableGenerator(
        name = "verGen",
        table = "verGenseq",
        pkColumnValue = "ver",
        valueColumnName = "nextVer",
        initialValue = 1,
        allocationSize = 1
    )
	private Long id;
	@ManyToOne(fetch = FetchType.EAGER)
	private Assessment assessment;
	private Date start;
	private Date end;
	private Date completed;
	@OneToMany(fetch = FetchType.EAGER)
	private List<CustomType> CustomFields;
	private String Notes;
	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL )
	private List<VerificationItem> verificationItems = new ArrayList<VerificationItem>();
	@ManyToOne(fetch = FetchType.EAGER)
	private User assessor;
	@ManyToOne(fetch = FetchType.EAGER)
	private User assignedRemediation;
	private Date remediationCompleted;
	private String workflowStatus;

	public User getAssessor() {
		return assessor;
	}
	public void setAssessor(User assessor) {
		this.assessor = assessor;
	}
	public User getAssignedRemediation() {
		return assignedRemediation;
	}
	public void setAssignedRemediation(User assignedRemediation) {
		this.assignedRemediation = assignedRemediation;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Assessment getAssessment() {
		return assessment;
	}
	public void setAssessment(Assessment assessment) {
		this.assessment = assessment;
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
	public List<CustomType> getCustomFields() {
		return CustomFields;
	}
	public void setCustomFields(List<CustomType> customFields) {
		CustomFields = customFields;
	}
	public String getNotes() {
		return Notes;
	}
	public void setNotes(String notes) {
		Notes = notes;
	}
	public List<VerificationItem> getVerificationItems() {
		return verificationItems;
	}
	public void setVerificationItems(List<VerificationItem> verificationItems) {
		this.verificationItems = verificationItems;
	}
	public Date getRemediationCompleted() {
		return remediationCompleted;
	}
	public void setRemediationCompleted(Date remediationCompleted) {
		this.remediationCompleted = remediationCompleted;
	}
	public String getWorkflowStatus() {
		return workflowStatus;
	}
	public void setWorkflowStatus(String workflowStatus) {
		this.workflowStatus = workflowStatus;
	}

	
	

}
