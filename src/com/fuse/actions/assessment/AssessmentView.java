package com.fuse.actions.assessment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpSession;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.json.simple.JSONObject;

import com.fuse.actions.FSActionSupport;
import com.fuse.dao.Assessment;
import com.fuse.dao.AuditLog;
import com.fuse.dao.BoilerPlate;
import com.fuse.dao.CheckListAnswers;
import com.fuse.dao.Comment;
import com.fuse.dao.CustomField;
import com.fuse.dao.CustomType;
import com.fuse.dao.Files;
import com.fuse.dao.Notification;
import com.fuse.dao.PeerReview;
import com.fuse.dao.RiskLevel;
import com.fuse.dao.User;
import com.fuse.dao.Vulnerability;
import com.fuse.dao.query.AssessmentQueries;
import com.fuse.dao.query.VulnerabilityQueries;
import com.faction.extender.AssessmentManager;
import com.fuse.extenderapi.Extensions;
import com.fuse.tasks.EmailThread;
import com.fuse.tasks.ReportGenThread;
import com.fuse.tasks.TaskQueueExecutor;
import com.fuse.utils.FSUtils;
import com.fuse.utils.History;

@Namespace("/portal")
@Result(name = "success", location = "/WEB-INF/jsp/assessment/Assessment.jsp", params = { "contentType", "text/html" })
public class AssessmentView extends FSActionSupport {
	private Assessment assessment;
	private String id;
	private String riskAnalysis;
	private String summary;
	private String update;
	private String aq;
	private String action = "";
	private List<Comment> comments;
	private boolean prSubmitted = false;
	private boolean prComplete = false;
	private boolean finalized = false;
	private boolean prAcceptedEdits = false;
	private List<Files> files;
	private List<History> history = new ArrayList<History>();;
	private List<Vulnerability> avulns = new ArrayList<Vulnerability>();
	private HashMap<Integer, Integer> counts = new HashMap();
	private Long prid = 0l;
	private String jsonResponse;
	private String icsFile;
	private List<RiskLevel> levels = new ArrayList();
	private List<CustomType> vulntypes = new ArrayList();
	private Boolean notowner;
	private List<BoilerPlate> summaryTemplates;
	private List<BoilerPlate> riskTemplates;
	LinkedHashMap<String, Integer> vulnMap = new LinkedHashMap<>();
	LinkedHashMap<String, Integer> catMap = new LinkedHashMap<>();

	@Action(value = "Assessment", results = { @Result(name = "ics", location = "/WEB-INF/jsp/assessment/ics.jsp"),
			@Result(name = "finerrorJson", location = "/WEB-INF/jsp/assessment/finerrorJson.jsp") })
	public String execute() throws NotSupportedException, SystemException {
		if (!(this.isAcassessor() || this.isAcmanager()))
			return AuditLog.notAuthorized(this, "User is not an Assessor or Manager", true);
		User user = this.getSessionUser();

		// Notification links use this to set the app and session.
		if (this.id != null && !this.id.equals("")) {
			this.setSession("asmtid", Long.parseLong(this.id));
		}

		if (this.getSession("asmtid") == null) {
			this.setSession("asmtid", Long.parseLong(this.id));
		} else {
			this.id = "" + this.getSession("asmtid");
		}

		Long lid = Long.parseLong(this.id);
		if (this.isAcmanager()) {
			assessment = AssessmentQueries.getAssessmentById(em, lid);
			User mgrs = assessment.getAssessor().stream().filter(u -> u.getId() == user.getId()).findFirst()
					.orElse(null);
			if (mgrs == null)
				this.notowner = true;

		} else {
			assessment = AssessmentQueries.getAssessmentByUserId(em, user.getId(), lid, AssessmentQueries.All);
		}

		if (assessment == null)
			return SUCCESS;
		

		levels = em.createQuery("from RiskLevel order by riskId").getResultList();

		vulntypes = em.createQuery("from CustomType where type = 1 and (deleted IS NULL or deleted = false)").getResultList();

		summaryTemplates = em.createQuery("from BoilerPlate where (user = :user or global = true) and type='summary' and active = true")
				.setParameter("user", user).getResultList();
		
		riskTemplates = em.createQuery("from BoilerPlate where (user = :user or global = true) and type='risk' and active = true")
				.setParameter("user", user).getResultList();

		history = this.createHistory(assessment, levels);

		avulns = (List<Vulnerability>) assessment.getVulns();
		for (int i = 0; i < 10; i++) {
			counts.put(i, 0);
		}
		for (Vulnerability v : avulns) {
			v.updateRiskLevels(em);
			if (v.getOverall() == null || v.getOverall() == -1l)
				continue;
			else
				counts.put(v.getOverall().intValue(), counts.get(v.getOverall().intValue()) + 1);

		}
		
		files = AssessmentQueries.getFilesByAssessmentId(em, assessment.getId());

		if (this.prEnabled) {
			if (assessment.getPeerReview() != null)
				comments = assessment.getPeerReview().getComments();
		}
		//fix deprecatednotes
		if(assessment.getNotes() != null && assessment.getNotebook().size() == 0) {
			assessment.upgradeNotes(em);
		}else if(assessment.getNotebook() == null || assessment.getNotebook().size() == 0) {
			assessment.upgradeNotes(em);
		}

		if (this.action != null && this.action.equals("finalize")) {
			if (!this.testToken(false))
				return this.ERRORJSON;
			
			if (this.isAssessmentBlocked(assessment, user)) {
				return this.ERRORJSON;
			}

			return finalizeAssessment(em, assessment, user);

		} else if (this.update != null && this.update.equals("true")) {
			if (!this.testToken(false))
				return this.ERRORJSON;
			if (assessment == null) {
				this._message = "Assessment does not exist.";
				return this.ERRORJSON;
			}
			
			if (this.isAssessmentBlocked(assessment, user)) {
				return this.ERRORJSON;
			}
			
			if (this.riskAnalysis != null)
				assessment.setRiskAnalysis(this.riskAnalysis);
			if (this.summary != null)
				assessment.setSummary(this.summary);
			AssessmentQueries.saveAssessment(this, em, assessment, "Assessment Summaries have been updated");

			return this.SUCCESSJSON;
		} else if (this.action.equals("ics")) {
			List<String> emails = new ArrayList<String>();
			for (String email : assessment.getDistributionList().split(";")) {
				emails.add(email);
			}
			String HTML = "Application Owners,<br><br>";
			HTML += "The purpose of this meeting is to discuss the vulnerabilities and risk issues discovered durring the assessment of <b><i>["
					+ assessment.getAppId() + "] " + assessment.getName() + "</i></b>.<br>";
			HTML += "Please feel free to invite anyone mising from the distribution list prior to the meeting.<br><br>";
			HTML += "The report will be distributed prior to the meeting. The data in the report is considered confidential and should be distributed only on a need-to-know basis.<br><br>";
			HTML += "Thanks,<br>";
			HTML += assessment.getAssessor().get(0).getTeam().getTeamName();
			icsFile = FSUtils.generateICSFile(emails, "",
					"Asssessment Review of [" + assessment.getAppId() + "] " + assessment.getName(), HTML);
			return "ics";
		}

		return SUCCESS;
	}
	

	private Long cfid;
	private String cfValue;

	@Action(value = "UpdateAsmtCF")
	public String UpdateAsmtCF() {
		if (!(this.isAcassessor() || this.isAcmanager()))
			return LOGIN;

		if (!this.testToken(false))
			return this.ERRORJSON;

		User user = this.getSessionUser();
		Long assessmentId = Long.parseLong(this.id);
		Assessment assessment = AssessmentQueries.getAssessmentByUserId(em, user.getId(), assessmentId,
				AssessmentQueries.OnlyNonCompleted);

		if (this.isAssessmentBlocked(assessment, user)) {
			return this.ERRORJSON;
		}
		
		if (assessment.getCustomFields() != null) {
			for (CustomField cf : assessment.getCustomFields()) {
				if (cf.getId().equals(this.cfid)) {
					cf.setValue(cfValue);
					AssessmentQueries.saveAssessment(this, em, assessment, "Assessment Custom Fields updated");

					return this.SUCCESSJSON;

				}
			}
		}
		return this.ERRORJSON;

	}
	
	@Action(value = "GetStats", results = {
			@Result(name = "assessmentStats", location = "/WEB-INF/jsp/assessment/assessmentStatsJson.jsp") })
	public String getAssessmentStats() {
		if (!(this.isAcassessor() || this.isAcmanager()))
			return LOGIN;
		User user = this.getSessionUser();
		Long assessmentId = (Long)this.getSession("asmtid");
		assessment = AssessmentQueries.getAssessmentByUserId(em, user.getId(), assessmentId, AssessmentQueries.All);
		if(assessment == null) {
			List<Vulnerability> vulns = new ArrayList();
			return "assessmentStats";
		}
		List<Vulnerability> vulns = assessment.getVulns();
		List<RiskLevel> levels = em
				.createQuery("from RiskLevel where risk IS NOT Null and risk != '' order by riskId")
				.getResultList();
		HashMap<Integer,String> levelMap = new HashMap<>();
		levels.forEach( (level) -> {
			levelMap.put(level.getRiskId(), level.getRisk());
			vulnMap.put(level.getRisk(), 0);
		});
		
		vulns.stream().forEach( (vuln) -> {
			String severity = levelMap.get(vuln.getOverall().intValue());
			if(severity == null) {
				int first = levelMap.keySet().stream().findFirst().get();
				severity = levelMap.get(first);
			}
			Integer count = vulnMap.get(severity);
			vulnMap.put(severity, ++count);
		});
		vulns.stream().forEach( (vuln) -> {
			String category;
			if(vuln.getCategory() == null && vuln.getDefaultVuln() != null) {
				category = vuln.getDefaultVuln().getCategory().getName();
			}else if(vuln.getCategory() == null) {
				category = "Uncategorized";
			}else {
				category = vuln.getCategory().getName(); 
			}
			Integer count = catMap.get(category);
			if(count == null) {
				catMap.put(category, 1);
			}else {
				catMap.put(category, ++count);
			}
		});
		
		return "assessmentStats";
		
	}

	@Action(value = "SetAssessment", results = {
			@Result(name = "redirect", type = "redirect", location = "Assessment") })
	public String setAssessment() {
		if (!(this.isAcassessor() || this.isAcmanager()))
			return LOGIN;
		User user = this.getSessionUser();

		this.setSession("asmtid", Long.parseLong(this.id));
		return "redirect";
	}

	@Action(value = "SendToPR", results = {
			@Result(name = "finerrorJson", location = "/WEB-INF/jsp/assessment/finerrorJson.jsp") })
	public String SendToPR() {
		if (!(this.isAcassessor()))
			return LOGIN;

		if (!this.testToken(false)) {
			JSONObject msg = new JSONObject();
			msg.put("errors", "Missing CSRF Token");
			msg.put("token", this.get_token());
			this.jsonResponse = msg.toJSONString();
			return "finerrorJson";
		}

		User user = this.getSessionUser();
		Long asmtId = SessionAsmtId();
		Assessment asmt = AssessmentQueries.getAssessmentByUserId(em, user.getId(), asmtId,
				AssessmentQueries.OnlyNonCompleted);

		if (this.isAssessmentBlocked(asmt, user)) {
			return this.ERRORJSON;
		}
		/// Are the checklists complete
		String result = this.IsCheckListComplete(asmt);
		if (result != null)
			return result;

		// Generate a new Report
		String host = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
		host += request.getContextPath();

		ReportGenThread reportThread = new ReportGenThread(host, asmt, asmt.getAssessor());
		TaskQueueExecutor.getInstance().execute(reportThread);

		// We are good to send to PR
		// Set the status to in PR
		// asmt.setStatus(Assessment.isInPR);
		asmt.setInPr();

		// Check for active PR Object... if not create one
		if (asmt.getPeerReview() == null) {
			asmt.setPeerReview(new PeerReview());
			asmt.getPeerReview().setCreated(new Date());
		}

		// Create a comments list
		if (asmt.getPeerReview().getComments() == null)
			asmt.getPeerReview().setComments(new ArrayList());

		// Comments will be stored with an empty user to indicate
		// that this is not an actual comment yet.
		//
		Comment newComment = new Comment();
		newComment.copyAssessment(asmt, true);
		newComment.setCommenter(null);
		asmt.getPeerReview().setAssessment(asmt);
		asmt.getPeerReview().getComments().add(newComment);
		asmt.getPeerReview().setCompleted(new Date(0));
		asmt.getPeerReview().setAcceptedEdits(new Date(0));
		AssessmentQueries.saveAll(this, asmt, em, "Assessment has been sent to Peer Review", newComment,
				asmt.getPeerReview(), asmt);

		return this.SUCCESSJSON;

	}

	private Map<String, String> jsonSuccessMessage;

	public Map<String, String> getJsonSuccessMessage() {
		return this.jsonSuccessMessage;
	}

	private boolean blockingPR(Long asmtId) {

		PeerReview prTemp = (PeerReview) em.createNativeQuery("{\"assessment_id\" : " + asmtId + "}", PeerReview.class)
				.getResultList().stream().findFirst().orElse(null);
		boolean prSubmitted = false;
		boolean prComplete = false;
		if (prTemp != null) {
			prSubmitted = true;
			if (prTemp.getCompleted() != null && prTemp.getCompleted().getTime() != 0) {
				prComplete = true;
			} else
				prComplete = false;
		}

		if (prSubmitted && !prComplete) {
			return true;
		} else
			return false;
	}
	private boolean isAssessmentBlocked(Assessment assessment, User user) {
		if (this.blockingPR(assessment.getId())) {
			this._message = "Assessment cannot be updated when in Peer Review.";
			return true; 
		}
		if( !assessment.isAcceptedEdits()) {
			this._message = "Assessment cannot be updated until the Peer Review's Edits are Accepted. <br><br>"
					+ "<a class='btn btn-primary' href='Assessment#tab_3'> Click Here to Accept Edits</a>";
			return true; 
		}
		
		if (assessment != null && assessment.getCompleted() != null) {
			this._message = "Vulnerability cannot be changed once the assessment is Finalized.";
			return true; 
		}

		if (!assessment.getAssessor().stream().anyMatch(u -> u.getId() == user.getId())) {
			this._message = "You Are not the owner of this Assessment";
			return true; 
		}
		return false;
		
	}


	private Long SessionAsmtId() {
		if (this.getSession("asmtid") == null) {
			this.setSession("asmtid", Long.parseLong(this.id));
		} else {
			this.id = "" + this.getSession("asmtid");
		}

		return Long.parseLong(this.id);
	}

	private String finalizeAssessment(EntityManager em, Assessment assessment, User user) {

		// Check to see if we can finalize the assessment
		if (this.prEnabled) {
			if (!assessment.isAcceptedEdits()) {
				JSONObject msg = new JSONObject();
				msg.put("errors", "You must accept changes to the Peer Review before you can close the assessment.");
				msg.put("token", this.get_token());
				this.jsonResponse = msg.toJSONString();
				return "finerrorJson";
			}

			String result = this.IsCheckListComplete(assessment);
			if (result != null)
				return result;

		}
		AssessmentQueries.removeImages(assessment);
		assessment.setCompleted(new Date());
		assessment.setFinalized();
		List<Vulnerability> vulns = assessment.getVulns();
		for (Vulnerability v : vulns) {
			v.setOpened(new Date());
		}
		List<Notification> notifiers = new ArrayList();
		for (User a : assessment.getAssessor()) {
			Notification n = new Notification();
			n.setAssessorId(a.getId());
			n.setCreated(new Date());
			n.setMessage(
					"Assessment <b>" + assessment.getName() + "</b> was finalized: <a href='DownloadReport?guid="
							+ assessment.getFinalReport().getFilename() + "'>Report</a>");
			notifiers.add(n);
		}
		
		AssessmentQueries.saveAll(this, assessment, em, "Assessment Finalized", assessment.getVulns(), assessment,
				notifiers);

		String email = "<b>Assessment Completed for " + assessment.getName() + " [ " + assessment.getAppId()
				+ " ] </b><br>";
		email += "<p>The assessment was completed by " + user.getFname() + " " + user.getLname() + " on " + new Date()
				+ "</p>";
		EmailThread emailThread = new EmailThread(assessment,
				"Assessment Completed for " + assessment.getName() + " [ " + assessment.getAppId() + " ]", email);
		TaskQueueExecutor.getInstance().execute(emailThread);
		
		// Run all Extensions
		Extensions amgr = new Extensions(Extensions.EventType.ASMT_MANAGER);
		amgr.execute(assessment, AssessmentManager.Operation.Finalize);
		
		return this.SUCCESSJSON;
	}

	// If we return null here then it means one of two things:
	// - No checklists were set up for this assessment
	// - The user has completed at least one checklist
	//
	// If the user has completed one checklist or none are associated
	// with an assessment then this is considered a 'pass' and
	// and processing should contine.
	//
	// if it failes then it will set the error message and return the
	// action result string.
	private String IsCheckListComplete(Assessment assessment) {

		List<CheckListAnswers> answers = assessment.getAnswers();
		if (answers == null || answers.size() == 0) {
			return null;
		}

		Map<String, Boolean> clFinish = new HashMap();
		// Set everything to true.. if one checklist has incomplete then we cannot
		// continue
		for (CheckListAnswers a : answers) {
			clFinish.put(a.getChecklist(), true);
		}
		for (CheckListAnswers a : answers) {
			if (a.getAnswer() == CheckListAnswers.Answer.Incomplete) {
				clFinish.put(a.getChecklist(), false);
			}
		}

		if (clFinish.values().contains(true)) {

		} else {
			JSONObject msg = new JSONObject();
			msg.put("errors", "You must complete at least one checklist.");
			msg.put("token", this.get_token());
			this.jsonResponse = msg.toJSONString();
			return "finerrorJson";
		}

		return null;
	}

	public Assessment getAssessment() {
		return assessment;
	}

	public void setAssessments(Assessment assessment) {
		this.assessment = assessment;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id.replace("app", "");
	}

	public String getRiskAnalysis() {
		return riskAnalysis;
	}

	public void setRiskAnalysis(String riskAnalysis) {
		this.riskAnalysis = riskAnalysis;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getUpdate() {
		return update;
	}

	public void setUpdate(String update) {
		this.update = update;
	}

	public String getActiveAQ() {
		return "active";
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public List<Comment> getComments() {
		return comments;
	}

	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}

	public boolean isPrComplete() {
		return prComplete;
	}

	public void setPrComplete(boolean prComplete) {
		this.prComplete = prComplete;
	}

	public boolean isPrSubmitted() {
		return prSubmitted;
	}

	public void setPrSubmitted(boolean prSubmitted) {
		this.prSubmitted = prSubmitted;
	}

	public boolean isFinalized() {
		return finalized;
	}

	public List<Files> getFiles() {
		return files;
	}

	public List<History> getHistory() {
		return history;
	}

	public List<Vulnerability> getAvulns() {
		return avulns;
	}

	public HashMap<Integer, Integer> getCounts() {
		return counts;
	}

	public Long getPrid() {
		return prid;
	}

	public String getJsonResponse() {
		return jsonResponse;
	}

	public List<RiskLevel> getLevels() {
		return levels;
	}

	public Boolean getPrAcceptedEdits() {
		return prAcceptedEdits;
	}

	public String getIcsFile() {
		return icsFile;
	}

	public List<CustomType> getVulntypes() {
		return vulntypes;
	}

	public void setCfid(Long cfid) {
		this.cfid = cfid;
	}

	public void setCfValue(String cfValue) {
		this.cfValue = cfValue;
	}
	public List<BoilerPlate> getRiskTemplates(){
		return riskTemplates;
	}
	public List<BoilerPlate> getSummaryTemplates(){
		return summaryTemplates;
	}


	private List<History> createHistory(Assessment a, List<RiskLevel> levels) {
		List<Vulnerability> vulns = VulnerabilityQueries.getVulnerabilitiesByAppId(em, a.getAppId(), levels, true);
		List<History> h = new ArrayList();
		for (Vulnerability v : vulns) {
			String assessors = "";
			for (User u : a.getAssessor()) {
				assessors += u.getFname() + " " + u.getLname() + "; ";
			}
			v.updateRiskLevels(em);
			Long currentId = v.getAssessmentId();
			Assessment currentAssessment = AssessmentQueries.getAssessmentById(em, currentId);
			String reportId = currentAssessment.getFinalReport() == null? null : currentAssessment.getFinalReport().getFilename();
			History obj = new History(v.getOpened(), v.getClosed(), v.getName(),
					reportId, v.getOverallStr(), assessors);
			h.add(obj);
		}
		return h;

	}

	public Boolean getNotowner() {
		return notowner;
	}

	
	public LinkedHashMap<String, Integer> getVulnMap(){
		return vulnMap;
	}
	public LinkedHashMap<String, Integer> getCatMap(){
		return catMap;
	}
	
	public List<String> getColors() {
		return new ArrayList<String>(Arrays.asList("#8E44AD", "#9B59B6", "#2C3E50", "#34495E", "#95A5A6", "#00a65a", "#39cccc", "#00c0ef", "#f39c12", "#dd4b39"));
	}
	
	
	   
	

}
