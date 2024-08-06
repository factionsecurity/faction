package com.fuse.actions.remediation;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

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
		
		verifications = em.createQuery("from Verification where completed != :completed").setParameter("completed", new Date(0)).getResultList();
		List<User>users = em.createQuery("from User").getResultList();
		levels = em.createQuery("from RiskLevel order by riskId").getResultList();
		for(User u : users){
			if(u.getPermissions().isRemediation())
				remusers.add(u);
			if(u.getPermissions().isAssessor())
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
		if(assessment.getFinalReport() != null) {
			reports.add(assessment.getFinalReport());
		}
		if(assessment.getRetestReport() != null) {
			reports.add(assessment.getRetestReport());
		}
		
		
		
		if(action.equals("create")){
			Assessment a = AssessmentQueries.getAssessmentById(em, asmtId);
			
			String vulnidStr = "";
			boolean first = true;
			List<Long> vids = new ArrayList();
			for(Long vulnid : this.vulnids.values()){
				
				for(Vulnerability v : a.getVulns()){
					if(v.getId() == vulnid && v.getClosed() != null && v.getClosed().getTime() != 0l){
						this.message = v.getName() + " is marked closed in prod and cannot be rescheduled";
						return this.ERRORJSON;
					}
				}
				vids.add(vulnid);
			}
			
			if(VulnerabilityQueries.isVulnInVerification(em, vids)){
				this.message = "Verification Already Scheduled.";
				return ERRORJSON;
			}
			
			
			
			User asr = getUser(users, assessorId);
			User rem = getUser(users, remId);
			a.setDistributionList(this.distro);
			//System.out.println(this.vulnids.get("0"));
			boolean errors = false;
			for(Long vulnid : this.vulnids.values()){
				Verification ver = new Verification();
				
				ver.setStart(start);
				ver.setEnd(end);
				ver.setAssessment(a);
				ver.setAssessor(asr);
				ver.setAssignedRemediation(rem);
				
				ver.setCompleted(new Date(0));
				ver.setNotes(notes);
				ver.setWorkflowStatus(Verification.InAssessorQueue);
				VerificationItem vi = new VerificationItem();
				for(Vulnerability v : a.getVulns()){
					if(v.getId() == vulnid){
						if(v.getClosed() != null && v.getClosed().getTime() == 0l){
							this.message += "Vulnerability Has been Closed (" + v.getName() + ").<br/>";
							errors=true;
							break;
						}else{
							vi.setVulnerability(v);
							break;
						}
					}
				}
				if(!errors){
					ver.getVerificationItems().add(vi);
					VulnerabilityQueries.saveAll(this, ver.getVerificationItems().get(0).getVulnerability(), em, "Create Verification", a, ver, vi);
					//HibHelper.getInstance().preJoin();
					//em.joinTransaction();
					//em.persist(a);
					//em.persist(ver);
					//em.persist(vi);
					//HibHelper.getInstance().commit();
				}
			}
			String appName = a.getAppId() + " - " + a.getName();
			String HTML = "A new Verification has been assigned for <b>" + appName + "</b>.<br>"
					+ "The following issue(s) require verification:<br><br><ul>";
			for(Long vulnid : this.vulnids.values()){
				for(Vulnerability v : a.getVulns()){
					if(v.getId() == vulnid){
						HTML += "<li>" + v.getName() +"</li>";
						break;
					}
				}
			}
			HTML+= "</ul><br><br>";
			EmailThread emailThread = new EmailThread(a, "Verification assigned for " +appName, HTML);
			TaskQueueExecutor.getInstance().execute(emailThread);
			if(errors){
				return this.ERRORJSON;
			}else{
				return SUCCESSJSON;
			}
		}else if (action.equals("update")){
			//HibHelper.getInstance().preJoin();
			//em.joinTransaction();
			Verification ver = VulnerabilityQueries.getVerificationById(em, verId);
			Assessment a = ver.getAssessment();
			User asr = getUser(users, assessorId);
			User rem = getUser(users, remId);
			ver.setStart(start);
			ver.setEnd(end);
			ver.setAssessor(asr);
			ver.setAssignedRemediation(rem);
			ver.setNotes(notes);
			a.setDistributionList(distro);
			//em.persist(ver);
			//em.persist(a);
			VulnerabilityQueries.saveAll(this, ver.getVerificationItems().get(0).getVulnerability(), em, "Update Verification", ver, a);
			//HibHelper.getInstance().commit();
			
			String appName = ver.getAssessment().getAppId() + " - " + ver.getAssessment().getName();
			String HTML = "A Verificaiton has been updated for <b>" + appName + "</b>.<br>"
					+ "The following issue has been updated:<br><br><h2>" + ver.getVerificationItems().get(0).getVulnerability().getName() + "</h2><br><br>";
			EmailThread emailThread = new EmailThread(ver, "Verification updated for " +appName, HTML);
			TaskQueueExecutor.getInstance().execute(emailThread);
			
		}else if(action.equals("addVuln")){
			//HibHelper.getInstance().preJoin();
			//em.joinTransaction();
			
			Verification ver = VulnerabilityQueries.getVerificationById(em, verId);
			Assessment a = ver.getAssessment();
			VerificationItem vi = new VerificationItem();
			for(Vulnerability v : a.getVulns()){
				if(v.getId() == vulnId){
					vi.setVulnerability(v);
					break;
				}
			}
			ver.getVerificationItems().add(vi);
			VulnerabilityQueries.saveAll(this, vi.getVulnerability(), em, "Adding Vulns to Verification", vi, ver);
			//em.persist(vi);
			//em.persist(ver);
			//HibHelper.getInstance().commit();
			return SUCCESSJSON;
					
		}else if (action.equals("dateSearch")){
			List<Verification> dv = em
					.createQuery("from Verification as a where (a.start <= :start and a.end > :start) or (a.start <= :end and a.end > :end)")
					.setParameter("start", sdate)
					.setParameter("end", edate)
					.getResultList();
			List<OOO> ooo = em.createQuery("from OOO as a where (a.start <= :start and a.end > :start) or (a.start <= :end and a.end > :end)")
					.setParameter("start", sdate)
					.setParameter("end", edate)
					.getResultList();
			for(User u : users){
				for(Verification v : dv){
					if(u.getId() == v.getAssessor().getId()){
						u.setVerificationCount(u.getVerificationCount()+1);
					}
				}
			}
			for(User u : users){
				for(OOO o : ooo){
					if(u.getId() == o.getUser().getId()){
						u.setOOOCount(u.getOOOCount()+1);
					}
				}
			}
			//session.close();
			return "verificationJson";
	
		}else if(action.equals("updateOpenDate")){
			Vulnerability v = em.find(Vulnerability.class, vulnId);
			Date opened = v.getOpened();
			v.setOpened(this.start);
			VulnerabilityQueries.saveVulnerability(this, em, v, "Verification Open Date Moved. Was " + opened);
			return SUCCESSJSON;
			
		}/*else
			session.close();*/
		
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
	
	
	
	
	
	
	
	
	
	

}