package com.fuse.actions.remediation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;

import com.fuse.actions.FSActionSupport;
import com.fuse.dao.Assessment;
import com.fuse.dao.FinalReport;
import com.fuse.dao.HibHelper;
import com.fuse.dao.OOO;
import com.fuse.dao.RiskLevel;
import com.fuse.dao.User;
import com.fuse.dao.Verification;
import com.fuse.dao.VerificationItem;
import com.fuse.dao.Vulnerability;
import com.fuse.dao.query.AssessmentQueries;
import com.fuse.dao.query.VulnerabilityQueries;
import com.fuse.tasks.EmailThread;
import com.fuse.tasks.TaskQueueExecutor;
import com.fuse.utils.FSUtils;
import com.opensymphony.xwork2.interceptor.annotations.Before;


@Namespace("/portal")
@Result(name="success",location="/WEB-INF/jsp/remediation/RemediationSchedule.jsp")
public class RemediationSchedule extends FSActionSupport{
	
	private List<Verification> verifications;
	private String action="";
	private List<User> remusers = new ArrayList<User>();
	private List<User> assessors = new ArrayList<User>();
	private Date start;
	private Date end;
	private Long asmtId;
	private String appName;
	private String appType;
	private String appId;
	private Long assessorId;
	private Long remId;
	private String notes;
	private Long verId;
	private Long vulnId;
	private String distro;
	private Date sdate;
	private Date edate;
	private HashMap<Long,Long>vulnids;
	private String vulnName;
	private List<RiskLevel>levels = new ArrayList();
	private String message; // error message;
	private User user;
	private List<Vulnerability> vulns;
	private Vulnerability vuln;
	private List<FinalReport> reports = new ArrayList<>();
	private Map<Long,List<String>> status = new HashMap<>();
	private Map<String,String> controls = new HashMap<>();
	
	
	@Before
	public String authorize() {
		if(!this.isAcremediation()) {
			return LOGIN;
		}
		user = this.getSessionUser();
		return null;
		
	}
	
	
    @Action(value="RemediationSchedule", results={
			@Result(name="verificationJson",location="/WEB-INF/jsp/remediation/dateSearchJson.jsp")
		})
	public String execute(){
		
		verifications = em
				.createQuery("from Verification where workflowStatus not in ('Remediation Completed', 'Remediation Cancelled')").getResultList();
		List<User>users = em.createQuery("from User").getResultList();
		levels = em.createQuery("from RiskLevel order by riskId").getResultList();
		for(User u : users){
			if(u.getPermissions() != null && u.getPermissions().isRemediation())
				remusers.add(u);
			if(u.getPermissions() != null && u.getPermissions().isAssessor())
				assessors.add(u);
		}
		
		vuln = em.find(Vulnerability.class, vulnId);
		Assessment assessment = em.find(Assessment.class, vuln.getAssessmentId());
		appName = assessment.getName();
		appType = assessment.getType().getType();
		appId = assessment.getAppId();
		asmtId = assessment.getId();
		
		vulns = assessment.getVulns();
		for(Vulnerability v : vulns) {
			v.updateRiskLevels(em);
		}
		
		
		
		vulns.stream().forEach( v -> {
			if(status.get(v.getId()) == null) {
				status.put(v.getId(), new ArrayList<String>());
			}
			if(v.getDevClosed()!= null) {
				status.get(v.getId()).add(FSUtils.addBadge("Closed Dev", "blue", "fa-check"));
				controls.put("closed|"+v.getId(), "true");
			}
			if(v.getClosed()!= null) {
				status.get(v.getId()).add(FSUtils.addBadge("Closed Prod", "green", "fa-check"));
				controls.put("closed|"+v.getId(), "true");
			}
			String pattern = "MM/dd/yyyy";
			SimpleDateFormat format = new SimpleDateFormat(pattern);
			Verification verification = verifications
					.stream()
					.filter( ver -> ver
							.getVerificationItems()
							.get(0)
							.getVulnerability()
							.getId() == v.getId())
					.findFirst()
					.orElse(null);
			if(verification !=null) {
				controls.put("cancel|"+v.getId(), "true");
				controls.put("verId|"+v.getId(), ""+verification.getId());
				controls.put("verStart|"+v.getId(), format.format(verification.getStart()));
				controls.put("verEnd|"+v.getId(), format.format(verification.getEnd()));
				controls.put("verAssessor|"+v.getId(), ""+verification.getAssessor().getId());
				controls.put("verRemediation|"+v.getId(), ""+verification.getAssignedRemediation().getId());
				controls.put("verDistro|"+v.getId(), ""+assessment.getDistributionList());
				status.get(v.getId()).add(FSUtils.addBadge("In Retest", "green", "fa-calendar"));
				if(verification.getEnd().getTime() < (new Date().getTime())) {
					status.get(v.getId()).add( FSUtils.addBadge("Verification Past Due", "red", "fa-calendar"));
				}
				else if(verification.getEnd().getTime() < (FSUtils.getWarn(new Date(), 2)).getTime() ) {
					status.get(v.getId()).add(FSUtils.addBadge("Verification Almost Due", "yellow", "fa-calendar"));
				}
				if(verification.getWorkflowStatus().equals(Verification.AssessorCompleted)) {
					if(verification.getVerificationItems().get(0).isPass()) {
						status.get(v.getId()).add(FSUtils.addBadge("Retest Passed", "green", "fa-check"));
					}
					if(!verification.getVerificationItems().get(0).isPass()) {
						status.get(v.getId()).add(FSUtils.addBadge("Retest Failed", "red", "fa-times"));
					}
				}
				if(verification.getWorkflowStatus().equals(Verification.AssessorCancelled)) {
						status.get(v.getId()).add(FSUtils.addBadge("Assessor Canceled", "yellow", "fa-times"));
				}
			}
			Date DueDate = FSUtils.getDue(em, v.getOpened(), v.getOverall().intValue());
			Date WarnDate = FSUtils.getWarning(em, v.getOpened(), v.getOverall().intValue());
			if(DueDate != null ) {
				String dueDateString = format.format(DueDate);
				
				if(DueDate != null && DueDate.getTime() <= (new Date()).getTime()){
					status.get(v.getId()).add(FSUtils.addBadge("Vulnerability Past Due (" + dueDateString +")", "red", "fa-bug"));
				}
				else if(WarnDate != null && WarnDate.getTime() <= (new Date()).getTime()){
					status.get(v.getId()).add(FSUtils.addBadge("Vulnerability Approaching Due Date (" + dueDateString +")", "yellow", "fa-bug"));
				}
			}
			
		});
		
		
		if(assessment.getFinalReport() != null) {
			reports.add(assessment.getFinalReport());
		}
		if(assessment.getRetestReport() != null) {
			reports.add(assessment.getRetestReport());
		}
		
		return SUCCESS;
	}
	public String getActiveRemSearch(){
		return "active";
	}
	public List<Verification> getVerifications() {
		return verifications;
	}
	public void setVerifications(List<Verification> verifications) {
		this.verifications = verifications;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	private User getUser(List<User> users, Long id){
		for(User u : users){
			if(u.getId() == id){
				return u;
			}
		}
		return null;
	}
	public List<User> getRemusers() {
		return remusers;
	}
	public void setRemusers(List<User> remusers) {
		this.remusers = remusers;
	}
	public List<User> getAssessors() {
		return assessors;
	}
	public void setAssessors(List<User> assessors) {
		this.assessors = assessors;
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
	public Long getAssessorId() {
		return assessorId;
	}
	public void setAssessorId(Long assessorId) {
		this.assessorId = assessorId;
	}
	public Long getRemId() {
		return remId;
	}
	public void setRemId(Long remId) {
		this.remId = remId;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	public Long getVerId() {
		return verId;
	}
	public void setVerId(Long verId) {
		this.verId = verId;
	}
	public Long getVulnId() {
		return vulnId;
	}
	public void setVulnId(Long vulnId) {
		this.vulnId = vulnId;
	}
	public String getDistro() {
		return distro;
	}
	public void setDistro(String distro) {
		this.distro = distro;
	}
	public Date getSdate() {
		return sdate;
	}
	public void setSdate(Date sdate) {
		this.sdate = sdate;
	}
	public Date getEdate() {
		return edate;
	}
	public void setEdate(Date edate) {
		this.edate = edate;
	}
	public Long getAsmtId() {
		return asmtId;
	}
	public void setVulnids(HashMap<Long,Long> vulnids) {
		this.vulnids = vulnids;
	}
	public HashMap<Long, Long> getVulnids() {
		return vulnids;
	}
	public String getVulnName() {
		return vulnName;
	}
	public void setVulnName(String vulnName) {
		this.vulnName = vulnName;
	}
	public List<RiskLevel> getLevels() {
		return levels;
	}
	public String getMessage() {
		return message;
	}
	public List<Vulnerability> getVulns() {
		return vulns;
	}
	public Vulnerability getVuln() {
		return this.vuln;
	}
	
	public String getAppId() {
		return this.appId;
	}
	public String getAppName() {
		return this.appName;
	}
	public List<FinalReport>getReports(){
		return this.reports;
	}
	public String getAppType() {
		return this.appType;
	}
	public Map<Long,List<String>> getStatus(){
		return this.status;
	}
	public Map<String,String> getControls(){
		return this.controls;
	}
	
	
	
	
	
	
	
	
	
	

}