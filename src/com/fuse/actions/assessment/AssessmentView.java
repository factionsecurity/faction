package com.fuse.actions.assessment;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
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
import com.fuse.dao.HibHelper;
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
import com.opensymphony.xwork2.ActionContext;

@Namespace("/portal")
@Result(name = "success", location = "/WEB-INF/jsp/assessment/Assessment.jsp", params = { "contentType", "text/html" })
public class AssessmentView extends FSActionSupport {
	private Assessment assessment;
	private String id;
	private String riskAnalysis;
	private String summary;
	private String notes;
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
	private User user;
	private List<BoilerPlate> summaryTemplates;
	private List<BoilerPlate> riskTemplates;

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
	
		//This fixes an image issue
		String content = assessment.getSummary();
		if(content != null && content.endsWith("div>")) {
			content = content + "<p><br/></p>";
			assessment.setSummary(content);
		}
		content = assessment.getRiskAnalysis();
		if(content != null && content.endsWith("div>")) {
			content = content + "<p><br/></p>";
			assessment.setRiskAnalysis(content);
		}
		content = assessment.getNotes();
		if(content != null && content.endsWith("div>")) {
			content = content + "<p><br/></p>";
			assessment.setNotes(content);
		}

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

		if (this.action != null && this.action.equals("genreport")) {

			if (assessment != null && assessment.getCompleted() != null)
				return "errorJson";

			if (!AssessmentQueries.checkForReportTemplates(em, assessment)) {
				this._message = "There are no report templates for this assessment. Contact your administrator.";
				return this.ERRORJSON;
			}

			String host = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
			host += request.getContextPath();

			HttpSession session = ServletActionContext.getRequest().getSession();
			if (assessment.getFinalReport() != null && assessment.getFinalReport().getGentime() != null) {
				session.setAttribute("reportDate", assessment.getFinalReport().getGentime());
			} else {
				Calendar dummy = new GregorianCalendar(1980, 1, 1); // Dummy Data
				session.setAttribute("reportDate", dummy.getTime());
			}

			ReportGenThread reportThread = new ReportGenThread(host, assessment, assessment.getAssessor());
			TaskQueueExecutor.getInstance().execute(reportThread);

			return this.SUCCESSJSON;

		} else if (this.action != null && this.action.equals("finalize")) {
			if (!this.testToken(false))
				return this.ERRORJSON;

			if (assessment != null && assessment.getCompleted() != null) {
				this._message = "Assessment has been finalized.";
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
			if (assessment.isInPr()) {
				this._message = "Assessment cannot be changed while in PeerReview.";
				return this.ERRORJSON;
			}
			if (assessment.isPrComplete()) {
				this._message = "Assessment cannot be changed user acknowledges the PeerReview.";
				return this.ERRORJSON;
			}

			if (assessment.isFinalized()) {
				this._message = "Assessment has been finalized.";
				return this.ERRORJSON;
			}
			if (this.riskAnalysis != null)
				assessment.setRiskAnalysis(this.riskAnalysis);
			if (this.notes != null)
				assessment.setNotes(this.notes);
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

		if (assessment == null) {
			this._message = "Assessment Not Found or is Finalized";
			return this.ERRORJSON;
		}

		if (assessment != null && assessment.getCompleted() != null) {
			this._message = "Assessment has been finalized.";
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

		if (asmt == null) {
			this._message = "Assessment Not Found or is Finalized";
			return this.ERRORJSON;
		}

		if (asmt.isInPr()) {
			this._message = "Assessment Already in PeerReview";
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

	@Action(value = "CheckStatus", results = {
			@Result(name = "success202", type = "httpheader", params = { "status", "202" }),
			@Result(name = "success200", location = "/WEB-INF/jsp/assessment/SuccessMessageJSON.jsp")

	})
	public String checkStatus() {
		if (!(this.isAcengagement() || this.isAcmanager())) {
			return LOGIN;
		}
		HttpSession session = ServletActionContext.getRequest().getSession();
		Date lastDate = (Date) session.getAttribute("reportDate");
		if (lastDate == null)
			return ERROR;

		Long asmtId = (Long) this.getSession("asmtid");
		Assessment assessment = em.find(Assessment.class, asmtId);
		if (assessment.getFinalReport() == null || assessment.getFinalReport().getGentime().equals(lastDate)) {
			return "success202";
		}
		this._message = "" + assessment.getFinalReport().getGentime();

		return "success200";

	}

	private String updatedText = "";

	@Action(value = "CheckLocks", results = {
			@Result(name = "lockSuccess", location = "/WEB-INF/jsp/assessment/lockSuccess.jsp"),
			@Result(name = "lockError", location = "/WEB-INF/jsp/assessment/lockError.jsp"),
			@Result(name = "lockJSON", location = "/WEB-INF/jsp/assessment/lockJSON.jsp"), })
	public String checkLocks() throws UnsupportedEncodingException {
		if (!(this.isAcengagement() || this.isAcmanager())) {
			return LOGIN;
		}
		this.user = this.getSessionUser();
		Long asmtId = (Long) this.getSession("asmtid");
		this.assessment = em.find(Assessment.class, asmtId);
		if (isNotesLockedbyAnotherUser() || isSummaryLockedbyAnotherUser() || isRiskLockedbyAnotherUser()) {
			this.clearLockType("", this.user);
			this.assessment.setNotes(URLEncoder
					.encode(Base64.getEncoder().encodeToString(this.assessment.getNotes().getBytes()), "UTF-8"));
			this.assessment.setSummary(URLEncoder
					.encode(Base64.getEncoder().encodeToString(this.assessment.getSummary().getBytes()), "UTF-8"));
			this.assessment.setRiskAnalysis(URLEncoder
					.encode(Base64.getEncoder().encodeToString(this.assessment.getRiskAnalysis().getBytes()), "UTF-8"));
			return "lockJSON";
		} else {
			return "lockSuccess";
		}
	}

	@Action(value = "SetLock", results = {
			@Result(name = "lockSuccess", location = "/WEB-INF/jsp/assessment/lockSuccess.jsp"),
			@Result(name = "lockError", location = "/WEB-INF/jsp/assessment/lockError.jsp"),
			@Result(name = "lockJSON", location = "/WEB-INF/jsp/assessment/lockJSON.jsp"), })
	public String setLock() throws UnsupportedEncodingException {
		if (!(this.isAcengagement() || this.isAcmanager())) {
			return LOGIN;
		}
		User user = this.getSessionUser();
		Long asmtId = (Long) this.getSession("asmtid");
		this.assessment = em.find(Assessment.class, asmtId);
		if (this.action.equals("notes") && !assessment.isNotesLock()) {
			assessment.setNotesLock(true);
			assessment.setNotesLockAt(new Date());
			assessment.setNotesLockBy(user);
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			em.persist(assessment);
			HibHelper.getInstance().commit();
			return "lockSuccess";
		} else if (this.action.equals("summary") && !assessment.isSummaryLock()) {
			assessment.setSummaryLock(true);
			assessment.setSummaryLockAt(new Date());
			assessment.setSummaryLockBy(user);
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			em.persist(assessment);
			HibHelper.getInstance().commit();
			return "lockSuccess";
		} else if (this.action.equals("risk") && !assessment.isRiskLock()) {
			assessment.setRiskLock(true);
			assessment.setRiskLockAt(new Date());
			assessment.setRiskLockBy(user);
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			em.persist(assessment);
			HibHelper.getInstance().commit();
			return "lockSuccess";
		} else {
			return "lockSuccess";
		}
	}

	@Action(value = "ClearLock", results = {
			@Result(name = "lockSuccess", location = "/WEB-INF/jsp/assessment/lockSuccess.jsp"),
			@Result(name = "lockError", location = "/WEB-INF/jsp/assessment/lockError.jsp"),
			@Result(name = "lockJSON", location = "/WEB-INF/jsp/assessment/lockJSON.jsp"), })
	public String clearLock() {
		if (!(this.isAcengagement() || this.isAcmanager())) {
			return LOGIN;
		}
		User user = this.getSessionUser();
		Long asmtId = (Long) this.getSession("asmtid");
		this.assessment = em.find(Assessment.class, asmtId);
		if (this.clearLockType(action, user)) {
			return "lockSuccess";
		} else {
			return "lockError";
		}
	}

	public boolean isNotesLockedbyAnotherUser() {
		return assessment.isNotesLock() && assessment.getNotesLockBy() != null
				&& assessment.getNotesLockBy().getId() != user.getId();
	}

	public boolean isSummaryLockedbyAnotherUser() {
		return assessment.isSummaryLock() && assessment.getSummaryLockBy() != null
				&& assessment.getSummaryLockBy().getId() != user.getId();
	}

	public boolean isRiskLockedbyAnotherUser() {
		return assessment.isRiskLock() && assessment.getRiskLockBy() != null
				&& assessment.getRiskLockBy().getId() != user.getId();
	}

	private boolean clearLockType(String type, User user) {
		boolean isUpdated = false;
		switch (type) {
		case "notes":
			if (assessment.isNotesLock() && assessment.getNotesLockBy().getId() == user.getId()) {
				isUpdated = true;
				assessment.setNotesLock(false);
				assessment.setNotesLockAt(null);
				assessment.setNotesLockBy(null);
			}
			break;
		case "summary":
			if (assessment.isSummaryLock() && assessment.getSummaryLockBy().getId() == user.getId()) {
				isUpdated = true;
				assessment.setSummaryLock(false);
				assessment.setSummaryLockAt(null);
				assessment.setSummaryLockBy(null);
			}
			break;
		case "risk":
			if (assessment.isRiskLock() && assessment.getRiskLockBy().getId() == user.getId()) {
				isUpdated = true;
				assessment.setRiskLock(false);
				assessment.setRiskLockAt(null);
				assessment.setRiskLockBy(null);
			}
			break;
		default:
			// Clear outdated locks;
			Calendar now = Calendar.getInstance();
			now.add(Calendar.MINUTE, -5);
			Date FiveMin = now.getTime();
			if (assessment.getNotesLockAt() != null && assessment.getNotesLockAt().before(FiveMin)) {
				this.clearLockType("notes", assessment.getNotesLockBy());
				isUpdated = true;
			}
			if (assessment.getSummaryLockAt() != null && assessment.getSummaryLockAt().before(FiveMin)) {
				this.clearLockType("summary", assessment.getSummaryLockBy());
				isUpdated = true;
			}
			if (assessment.getRiskLockAt() != null && assessment.getRiskLockAt().before(FiveMin)) {
				this.clearLockType("risk", assessment.getRiskLockBy());
				isUpdated = true;
			}
			break;
		}
		if (isUpdated) {
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			em.persist(assessment);
			HibHelper.getInstance().commit();
		}
		return isUpdated;
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
			/*
			 * if(!prComplete){ JSONObject msg = new JSONObject(); msg.put("errors",
			 * "PeerReview has not been completed."); this.jsonResponse =
			 * msg.toJSONString(); return "finerrorJson"; }
			 */

			String result = this.IsCheckListComplete(assessment);
			if (result != null)
				return result;

		}
		// HibHelper.getInstance().preJoin();
		// em.joinTransaction();
		assessment.setCompleted(new Date());
		assessment.setFinalized();
		List<Vulnerability> vulns = assessment.getVulns();
		// em.getTransaction().begin();
		for (Vulnerability v : vulns) {
			v.setOpened(new Date());
			// em.persist(v);
		}
		// em.persist(assessment);
		List<Notification> notifiers = new ArrayList();
		for (User a : assessment.getAssessor()) {
			Notification n = new Notification();
			n.setAssessorId(a.getId());
			n.setCreated(new Date());
			n.setMessage(
					"Assessment <b>" + assessment.getName() + "</b> was finalized: <a href='../service/Report.pdf?guid="
							+ assessment.getFinalReport().getFilename() + "'>Report</a>");
			notifiers.add(n);
			// em.persist(n);
		}
		Extensions amgr = new Extensions(Extensions.EventType.ASMT_MANAGER);
		if (amgr.checkIfExtended()) {
			try {
				amgr.execute(em, assessment, AssessmentManager.Operation.Finalize);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		// AuditLog.audit(this, "Assessment Finalized", AuditLog.UserAction,
		// AuditLog.CompAssessment, assessment.getId(), false);
		// HibHelper.getInstance().commit();
		AssessmentQueries.saveAll(this, assessment, em, "Assessment Finalized", assessment.getVulns(), assessment,
				notifiers);

		String email = "<b>Assessment Completed for " + assessment.getName() + " [ " + assessment.getAppId()
				+ " ] </b><br>";
		email += "<p>The assessment was completed by " + user.getFname() + " " + user.getLname() + " on " + new Date()
				+ "</p>";
		EmailThread emailThread = new EmailThread(assessment,
				"Assessment Completed for " + assessment.getName() + " [ " + assessment.getAppId() + " ]", email);
		TaskQueueExecutor.getInstance().execute(emailThread);
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

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
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

	private class History {
		private Date opened;
		private Date closed;
		private String vuln;
		private String report;
		private String severity;
		private String assessor;

		public History(Date opened, Date closed, String vuln, String Report, String severity, String assessor) {
			this.opened = opened;
			this.closed = closed;
			this.vuln = vuln;
			this.report = Report != null ? Report.replace("/tmp/", "") : null;
			this.severity = severity;
			this.assessor = assessor;
		}

		public Date getOpened() {
			return opened;
		}

		public void setOpened(Date opened) {
			this.opened = opened;
		}

		public Date getClosed() {
			return closed;
		}

		public void setClosed(Date closed) {
			this.closed = closed;
		}

		public String getVuln() {
			return vuln;
		}

		public void setVuln(String vuln) {
			this.vuln = vuln;
		}

		public String getReport() {
			return report;
		}

		public void setReport(String report) {
			this.report = report;
		}

		public String getSeverity() {
			return severity;
		}

		public void setSeverity(String severity) {
			this.severity = severity;
		}

		public String getAssessor() {
			return assessor;
		}

		public void setAssessor(String assessor) {
			this.assessor = assessor;
		}
		

	}

	private List<History> createHistory(Assessment a, List<RiskLevel> levels) {
		List<Vulnerability> vulns = VulnerabilityQueries.getVulnerabilitiesByAppId(em, a.getAppId(), levels, true);
		List<History> h = new ArrayList();
		for (Vulnerability v : vulns) {
			String assessors = "";
			for (User u : a.getAssessor()) {
				assessors += u.getFname() + " " + u.getLname() + "; ";
			}

			History obj = new History(v.getOpened(), v.getClosed(), v.getName(),
					a.getFinalReport() == null ? null : a.getFinalReport().getFilename(), v.getOverallStr(), assessors);
			h.add(obj);
		}
		return h;

	}

	public Boolean getNotowner() {
		return notowner;
	}

	public String getUpdatedText() {
		return this.updatedText;
	}
	
	   
	

}
