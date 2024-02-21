package com.fuse.actions.assessment;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.fuse.actions.FSActionSupport;
import com.fuse.dao.Assessment;
import com.fuse.dao.AuditLog;
import com.fuse.dao.CheckListAnswers;
import com.fuse.dao.Comment;
import com.fuse.dao.ExploitStep;
import com.fuse.dao.Files;
import com.fuse.dao.HibHelper;
import com.fuse.dao.Notification;
import com.fuse.dao.PeerReview;
import com.fuse.dao.PeerReviewLock;
import com.fuse.dao.RiskLevel;
import com.fuse.dao.User;
import com.fuse.dao.Vulnerability;
import com.fuse.dao.query.AssessmentQueries;
import com.fuse.tasks.ReportGenThread;
import com.fuse.tasks.TaskQueueExecutor;

@Namespace("/portal")
@Result(name = "success", location = "/WEB-INF/jsp/peerreviews/TrackChanges.jsp")
public class TrackChanges extends FSActionSupport {

	private Assessment asmt;
	private User user;
	private String action = "";
	private String risk;
	private String summary;
	private String risk_notes;
	private String sum_notes;
	private HashMap<String, String> vuln_desc;
	private HashMap<String, String> vuln_rec;
	private HashMap<String, String> vuln_details;
	private HashMap<String, String> vuln_desc_notes;
	private HashMap<String, String> vuln_rec_notes;
	private HashMap<String, String> vuln_detail_notes;
	private Long prid;
	private String jsonResponse;
	private Boolean prqueue = false;
	private List<RiskLevel> levels;
	private Map<Long, List<CheckListAnswers>> checklists = new HashMap();
	private List<Files> files;
	private boolean showSave = true;
	private boolean showComplete = true;
	private String field;
	private PeerReviewLock lock;
	private List<PeerReviewLock> allLocks;

	private Assessment cloneAsssessmentFromPeerReview(PeerReview pr) throws ParseException {
		Assessment assessment = new Assessment();
		Comment com = pr.getComments().get(pr.getComments().size() - 1);
		assessment = com.exportAssessment(em);
		assessment.setAnswers(pr.getAssessment().getAnswers());
		assessment.setId(pr.getAssessment().getId());
		assessment.setAppId(pr.getAssessment().getAppId());
		assessment.setName(pr.getAssessment().getName());
		assessment.setType(pr.getAssessment().getType());
		return assessment;
	}

	private Comment createUpdatedCommentFromPeerReview(PeerReview pr) {
		Comment review = pr.getComments().get(pr.getComments().size() - 1);
		Assessment asmt;
		try {
			asmt = review.exportAssessment(em);
			//TODO: Need to fix this so it happens in the export command
			asmt.setType(pr.getAssessment().getType());
			if (this.summary != null)
				review.setSummary1(this.summary);
			if (this.risk != null)
				review.setSummary2(this.risk);
			if (this.sum_notes != null)
				review.setSummary1_notes(this.sum_notes);
			if (this.risk_notes != null)
				review.setSummary2_notes(this.risk_notes);

			for (Vulnerability v : asmt.getVulns()) {
				long id = v.getId();
				if (vuln_desc != null && vuln_desc.get("" + id) != null)
					v.setDescription(vuln_desc.get("" + id));
				if (vuln_desc_notes != null && vuln_desc_notes.get("" + id) != null)
					v.setDesc_notes(vuln_desc_notes.get("" + id));
				if (vuln_rec != null && vuln_rec.get("" + id) != null)
					v.setRecommendation(vuln_rec.get("" + id));
				if (vuln_rec_notes != null && vuln_rec_notes.get("" + id) != null)
					v.setRec_notes(vuln_rec_notes.get("" + id));
				if (vuln_details != null && vuln_details.get("" + id) != null)
					v.setDetails(vuln_details.get("" + id));
				if (vuln_detail_notes != null && vuln_detail_notes.get("" + id) != null)
					v.setDetail_notes(vuln_detail_notes.get("" + id));
			}
			// Remove the vulns currently there and add our Updated ones.
			review.deleteAllVulns();
			review.addVulns(asmt.getVulns(), false);
			return review;
		} catch (ParseException e) {
			e.printStackTrace();
			return review;
		}

	}

	@Action(value = "SaveChanges", results = {
			@Result(name = "completeErrors", location = "/WEB-INF/jsp/peerreviews/completeErrors.jsp"),
			@Result(name = "completeJSON", location = "/WEB-INF/jsp/peerreviews/completeJson.jsp") })
	public String saveAsOwner() throws ParseException {
		if (!(this.isAcassessor() || this.isAcmanager())) {
			return AuditLog.notAuthorized(this, "User was not an Assessor or Manager", true);
		}

		PeerReview pr = (PeerReview) this.em.find(PeerReview.class, prid);
		if (pr == null)
			return this.SUCCESSJSON;

		if (pr.getAssessment().isAcceptedEdits()) {
			this._message = "Not allowed to edit the PR after you have accepted the changes.";
			return this.ERRORJSON;
		}
		if (pr.getAssessment().isFinalized()) {
			this._message = "Not allowed to edit the PR after the assessment is finalized";
			return this.ERRORJSON;
		}

		if (!pr.getAssessment().isPrComplete()) {
			this._message = "Not allowed to complete when the PR untill it has been reviewed by another assessor";
			return this.ERRORJSON;
		}

		Comment review = this.createUpdatedCommentFromPeerReview(pr);

		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		em.persist(review);
		em.persist(pr);
		HibHelper.getInstance().commit();
		return this.SUCCESSJSON;
	}
	/*
	 * This completeAdOwner Method allows the assessor to accept Peer Review changes
	 */
	@Action(value = "CompleteChanges", results = {
			@Result(name = "completeErrors", location = "/WEB-INF/jsp/peerreviews/completeErrors.jsp"),
			@Result(name = "completeJSON", location = "/WEB-INF/jsp/peerreviews/completeJson.jsp") })
	public String completeAsOwner() throws ParseException {
		if (!(this.isAcassessor() || this.isAcmanager())) {
			return AuditLog.notAuthorized(this, "User was not an Assessor or Manager", true);
		}

		System.out.println(this.prid);
		PeerReview pr = em.find(PeerReview.class, this.prid);
		if (pr == null)
			return this.SUCCESSJSON;

		if (pr.getAssessment().isAcceptedEdits()) {
			this._message = "Not allowed to edit the PR after you have accepted the changes.";
			return this.ERRORJSON;
		}
		if (pr.getAssessment().isFinalized()) {
			this._message = "Not allowed to edit the PR after the assessment is finalized";
			return this.ERRORJSON;
		}

		if (!pr.getAssessment().isPrComplete()) {
			this._message = "Not allowed to complete when the PR until it has been reviewed by another assessor";
			return this.ERRORJSON;
		}

		Assessment assessment = pr.getAssessment();
		Comment comment = pr.getComments().get(pr.getComments().size() - 1);
		Assessment updatedAssessment = comment.exportAssessment(em);
		updatedAssessment.setType(assessment.getType());
		comment.setAcceptedEdits(true);

		// Save Everything...
		assessment.setRiskAnalysis(updatedAssessment.getRiskAnalysis());
		assessment.setSummary(updatedAssessment.getSummary());
		assessment.setAcceptedEdits();
		for (Vulnerability v : assessment.getVulns()) {
			long id = v.getId();
			Vulnerability updated = updatedAssessment.getVulns().stream().filter( a -> a.getId() == id).findFirst().orElse(null);
			v.setDescription(updated.getDescription());
			v.setRecommendation(updated.getRecommendation());
			v.setDetails(updated.getDetails());
		}

		// Check if there are any un-accepted edits. These will html from track changes
		// highlights in the text
		JSONArray errors = new JSONArray();
		String trackChangesTest = "(.*)<span class=\\\"(ins|del) cts-(.*)";

		if (assessment.getRiskAnalysis().matches(trackChangesTest)) {
			errors.add("Risk Analysis Section contains UnAccepted Edits");
		}
		if (assessment.getSummary().matches(trackChangesTest)) {
			errors.add("Application Summary Section contains UnAccepted Edits");
		}
		for (Vulnerability v : assessment.getVulns()) {
			if (v.getDescription().matches(trackChangesTest)) {
				errors.add("Vulnerability " + v.getName() + " Description contains UnAccepted Edits");
			}
			if (v.getRecommendation().matches(trackChangesTest)) {
				errors.add("Vulnerability " + v.getName() + " Recommendation contains UnAccepted Edits");
			}
			if (v.getDetails().matches(trackChangesTest)) {
				errors.add("Vulnerability " + v.getName() + " Details contains UnAccepted Edits");
			}
		}
		JSONObject result = new JSONObject();
		if (errors.size() > 0) {
			result.put("errors", errors);
			this.jsonResponse = result.toJSONString();
			return "completeErrors";
		} else {

			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			for (Vulnerability v : assessment.getVulns()) {
				em.persist(v);
			}
			em.persist(comment);
			em.persist(assessment);

			ReportGenThread reportThread = new ReportGenThread("", assessment, assessment.getAssessor());
			TaskQueueExecutor.getInstance().execute(reportThread);

			// update accepted Edits
			pr.setAcceptedEdits(new Date());
			em.persist(pr);
			AuditLog.audit(this, "Peer Review Accepted/Reviewed Edits", AuditLog.UserAction, AuditLog.CompAssessment,
					assessment.getId(), false);
			HibHelper.getInstance().commit();
			return "completeJSON";
		}

	}

	
	/*
	 * This completeAsPeerReviewer Method is used the the peer reviewer to complete the PR process and kick the 
	 * report back to the assessor
	 */
	@Action(value = "CompletePR", results = {
			@Result(name = "completeErrors", location = "/WEB-INF/jsp/peerreviews/completeErrors.jsp"),
			@Result(name = "completeJSON", location = "/WEB-INF/jsp/peerreviews/completeJson.jsp") })
	public String completeAsPeerReviewer() throws ParseException {

		if (!(this.isAcassessor() || this.isAcmanager())) {
			return AuditLog.notAuthorized(this, "User was not an Assessor or Manager", true);
		}

		User user = this.getSessionUser();

		PeerReview pr = (PeerReview) this.em.createQuery("from PeerReview where id = :id").setParameter("id", prid)
				.getResultList().stream().findFirst().orElse("null");
		if (pr == null) {
			this._message = "This Peer Review Does not Exist";
			return this.ERRORJSON;
		}

		if (!pr.getAssessment().isInPr()) {
			this._message = "Peer Review has already been completed";
			return this.ERRORJSON;
		}

		Comment comment = pr.getComments().get(pr.getComments().size() - 1);
		List<PeerReviewLock> allLocks = em.createQuery("from PeerReviewLock where lastComment = :comment")
				.setParameter("comment", comment).getResultList();

		if (!(allLocks == null || allLocks.size() == 0)) {
			this._message = "Not allowed to complete a Peer Review when someone is still making edits";
			return this.ERRORJSON;
		}

		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		
		//Create a copy of the comment and save it. This new copy is what the original assessors
		// will update in their dashboard. This allows the original review from the peer reviewers 
		// to be kept for later review.
		Comment newComment = new Comment();
		newComment.copyComment(comment);
		newComment.setDateOfComment(new Date());
		pr.getComments().add(newComment);
		em.persist(newComment);
		// Close the PR object
		pr.setCompleted(new Date());
		comment.setDateOfComment(new Date());
		comment.setCommenter(user);
		em.persist(comment);
		em.persist(pr);
		pr.getAssessment().setPrComplete();
		em.persist(pr.getAssessment());

		String reviewers = comment.getCommenters().stream().reduce("",
				(acc, u) -> String.format("%s %s %s,", acc, u.getFname(), u.getLname()), String::concat);

		reviewers = reviewers.substring(0, reviewers.length() - 1); // remove last comma

		for (User a : pr.getAssessment().getAssessor()) {
			Notification notif = new Notification();
			notif.setAssessorId(a.getId());
			notif.setCreated(new Date());
			notif.setMessage("<a href='Assessment?id=app" + pr.getAssessment().getId()
					+ "#tab_3'>Peer Review</a> Completed By: " + reviewers);
			em.persist(notif);
		}

		AuditLog.audit(this, "Peer Review Completed", AuditLog.UserAction, AuditLog.CompAssessment,
				pr.getAssessment().getId(), false);
		HibHelper.getInstance().commit();
		return "completeJSON";

	}

	@Action(value = "SaveTrackChanges", results = {
			@Result(name = "completeErrors", location = "/WEB-INF/jsp/peerreviews/completeErrors.jsp"),
			@Result(name = "completeJSON", location = "/WEB-INF/jsp/peerreviews/completeJson.jsp") })
	public String saveAsPeerReviewer() throws ParseException {
		if (!(this.isAcassessor() || this.isAcmanager())) {
			return AuditLog.notAuthorized(this, "User was not an Assessor or Manager", true);
		}

		User user = this.getSessionUser();

		PeerReview pr = (PeerReview) this.em.find(PeerReview.class, prid);
		if (pr == null)
			return this.SUCCESSJSON;

		if (pr.getAssessment().isAcceptedEdits()) {
			this._message = "Not allowed to edit the PR after you have accepted the changes.";
			return this.ERRORJSON;
		}
		if (pr.getAssessment().isFinalized()) {
			this._message = "Not allowed to edit the PR after the assessment is finalized";
			return this.ERRORJSON;
		}

		Comment review = this.createUpdatedCommentFromPeerReview(pr);
		review.setCommenter(user);

		HibHelper.getInstance().preJoin();
		em.joinTransaction();
		em.persist(review);
		em.persist(pr);
		HibHelper.getInstance().commit();
		return this.SUCCESSJSON;

	}

	@Action(value = "TrackChanges", results = {
			@Result(name = "redirect", type = "redirectAction", location = "PeerReview"),
			@Result(name = "completeErrors", location = "/WEB-INF/jsp/peerreviews/completeErrors.jsp"),
			@Result(name = "completeJSON", location = "/WEB-INF/jsp/peerreviews/completeJson.jsp") })
	public String execute() throws ParseException {

		if (!(this.isAcassessor() || this.isAcmanager())) {
			return AuditLog.notAuthorized(this, "User was not an Assessor or Manager", true);
		}
		User user = this.getSessionUser();

		PeerReview pr = (PeerReview) this.em.find(PeerReview.class, prid);
		levels = em.createQuery("from RiskLevel order by riskId").getResultList();
		if (pr == null)
			return "redirect";

		boolean isAssessor = false;
		for (User u : pr.getAssessment().getAssessor()) {
			if (u.getId() == user.getId()) {
				isAssessor = true;
				break;

			}

		}
		if (pr.getAssessment().getWorkflow() >= 3) {
			this.showComplete = false;
			this.showSave = false;
		} else if (pr.getAssessment().getWorkflow() < 1) {
			this.showComplete = false;
			this.showSave = false;
		}

		if (pr.getCompleted().getTime() != 0 && !isAssessor)
			return "redirect";

		asmt = new Assessment();
		Comment com = pr.getComments().get(pr.getComments().size() - 1);
		asmt = com.exportAssessment(em);
		asmt.setWorkflow(pr.getAssessment().getWorkflow());
		asmt.setAnswers(pr.getAssessment().getAnswers());
		asmt.setId(pr.getAssessment().getId());
		asmt.setAppId(pr.getAssessment().getAppId());
		asmt.setName(pr.getAssessment().getName());
		asmt.setType(pr.getAssessment().getType());

		files = (List<Files>) em.createQuery("from Files where type = :type and entityId = :id")
				.setParameter("type", Files.ASSESSMENT).setParameter("id", pr.getAssessment().getId()).getResultList();

		checklists = new HashMap();
		for (CheckListAnswers a : asmt.getAnswers()) {
			if (checklists.containsKey(a.getCheckId())) {
				checklists.get(a.getCheckId()).add(a);
			} else {
				checklists.put(a.getCheckId(), new ArrayList());
				checklists.get(a.getCheckId()).add(a);
			}
		}

		return SUCCESS;
	}

	@Action(value = "getPRHistory")
	public String getPRHistory() throws ParseException {
		if (!(this.isAcassessor() || this.isAcmanager())) {
			return AuditLog.notAuthorized(this, "User was not an Assessor or Manager", true);
		}

		User user = this.getSessionUser();
		asmt = new Assessment();
		Comment com = em.find(Comment.class, prid);
		if (com == null) {
			return ERROR;
		}
		PeerReview pr = AssessmentQueries.getPeerReviewFromPRComment(em, prid);
		if (pr == null)
			return ERROR;

		if (!this.isAcmanager()
				&& !pr.getAssessment().getAssessor().stream().anyMatch(usr -> usr.getId() == user.getId())) {
			return ERROR;
		}

		asmt = com.exportAssessment(em);
		asmt.setAnswers(pr.getAssessment().getAnswers());
		asmt.setId(pr.getAssessment().getId());
		asmt.setAppId(pr.getAssessment().getAppId());
		asmt.setName(pr.getAssessment().getName());
		asmt.setType(pr.getAssessment().getType());
		this.showComplete = false;
		this.showSave = false;
		levels = em.createQuery("from RiskLevel order by riskId").getResultList();
		return SUCCESS;
	}

	private HashMap<String, String> FieldMap = new HashMap<String, String>() {
		{
			put("summary", "summary1");
			put("risk", "summary2");
			put("summary_notes", "summary1_notes");
			put("risk_notes", "summary2_notes");

		}
	};

	@Action(value = "PRCheckLocks", results = {
			@Result(name = "lockSuccess", location = "/WEB-INF/jsp/peerreviews/lockSuccess.jsp"),
			@Result(name = "lockError", location = "/WEB-INF/jsp/peerreviews/lockError.jsp"),
			@Result(name = "lockJSON", location = "/WEB-INF/jsp/peerreviews/lockJSON.jsp"), })
	public String checkLocks() throws UnsupportedEncodingException, IllegalAccessException, InvocationTargetException,
			NoSuchMethodException, ParseException {
		if (!(this.isAcassessor() || this.isAcmanager())) {
			return LOGIN;
		}
		user = this.getSessionUser();
		PeerReview pr = (PeerReview) this.em.find(PeerReview.class, prid);
		if(pr == null) {
			this._message ="Peer Review has been completed.";
			return this.ERRORJSON;
		}
		Boolean currentAssessor = pr.getAssessment().getAssessor().stream().anyMatch( u -> u.getId() == user.getId());
		if(pr.getCompleted().getTime() != 0l && !currentAssessor) {
			this._message ="Peer Review has been completed.";
			return this.ERRORJSON;
		}
		Comment com = pr.getComments().get(pr.getComments().size() - 1);
		this.clearOldLocks(com);
		if (com == null) {
			return ERROR;
		}
		this.allLocks = em.createQuery("from PeerReviewLock where lastComment = :comment").setParameter("comment", com)
				.getResultList();
		if (allLocks == null || allLocks.size() == 0) {
			return "lockSuccess";
		} else {
			for (PeerReviewLock lock : allLocks) {

				String property = FieldMap.get(lock.getLockedField());
				if (property != null) {
					String updatedText = (String) PropertyUtils.getProperty(com, property);
					updatedText = this.getBase64Encoded(updatedText);
					lock.setUpdatedText(updatedText);
				} else {
					Assessment asmt = com.exportAssessment(em);
					String index = lock.getLockedField().split("'")[1];
					if (lock.getLockedField().startsWith("vuln_")) {
						Vulnerability vulnerability = asmt.getVulns().stream()
								.filter(vuln -> vuln.getId() == Long.parseLong(index)).findFirst().orElse(null);
						if (lock.getLockedField().contains("rec_notes")) {
							lock.setUpdatedText(this.getBase64Encoded(vulnerability.getRec_notes()));
						} else if (lock.getLockedField().contains("desc_notes")) {
							lock.setUpdatedText(this.getBase64Encoded(vulnerability.getDesc_notes()));
						} else if (lock.getLockedField().contains("detail_notes")) {
							lock.setUpdatedText(this.getBase64Encoded(vulnerability.getDetail_notes()));
						} else if (lock.getLockedField().contains("desc")) {
							lock.setUpdatedText(this.getBase64Encoded(vulnerability.getDescription()));
						} else if (lock.getLockedField().contains("rec")) {
							lock.setUpdatedText(this.getBase64Encoded(vulnerability.getRecommendation()));
						} else if (lock.getLockedField().contains("details")) {
							lock.setUpdatedText(this.getBase64Encoded(vulnerability.getDetails()));
						}
					}

				}

			}
			allLocks.removeIf(lock -> (lock.getLockedBy().getId() == user.getId()));
			return "lockJSON";
		}
	}

	@Action(value = "PRSetLock", results = {
			@Result(name = "lockSuccess", location = "/WEB-INF/jsp/peerreviews/lockSuccess.jsp"),
			@Result(name = "lockError", location = "/WEB-INF/jsp/peerreviews/lockError.jsp"),
			@Result(name = "lockJSON", location = "/WEB-INF/jsp/peerreviews/lockJSON.jsp"), })
	public String setLock() throws UnsupportedEncodingException, ParseException {
		if (!(this.isAcassessor() || this.isAcmanager())) {
			return LOGIN;
		}
		user = this.getSessionUser();
		asmt = new Assessment();
		PeerReview pr = (PeerReview) this.em.find(PeerReview.class, prid);
		Comment com = pr.getComments().get(pr.getComments().size() - 1);
		if (com == null) {
			return ERROR;
		}
		this.lock = (PeerReviewLock) em
				.createQuery("from PeerReviewLock where lockedField = :field and lastComment = :comment")
				.setParameter("field", field).setParameter("comment", com).getResultList().stream().findFirst()
				.orElse(null);

		if (this.lock == null) {
			this.lock = new PeerReviewLock();
			this.lock.setLastComment(com);
			this.lock.setLockedBy(user);
			this.lock.setLockedField(this.field);
			this.lock.setLockedAt(new Date());
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			em.persist(this.lock);
			HibHelper.getInstance().commit();
			return "lockSuccess";

		} else if (this.isLockedByAnotherUser(this.lock, this.field)) {
			return "lockError";
		} else {
			return "lockSuccess";
		}

	}

	@Action(value = "PRClearLock", results = {
			@Result(name = "lockSuccess", location = "/WEB-INF/jsp/peerreviews/lockSuccess.jsp"),
			@Result(name = "lockError", location = "/WEB-INF/jsp/peerreviews/lockError.jsp"),
			@Result(name = "lockJSON", location = "/WEB-INF/jsp/peerreviews/lockJSON.jsp"), })
	public String clearLock() throws ParseException {
		if (!(this.isAcassessor() || this.isAcmanager())) {
			return LOGIN;
		}
		user = this.getSessionUser();
		PeerReview pr = (PeerReview) this.em.find(PeerReview.class, prid);
		Comment com = pr.getComments().get(pr.getComments().size() - 1);
		if (com == null) {
			return ERROR;
		}
		this.lock = (PeerReviewLock) em.createQuery(
				"from PeerReviewLock where lockedField = :field and lastComment = :comment and lockedBy = :user")
				.setParameter("field", field).setParameter("comment", com).setParameter("user", user).getResultList()
				.stream().findFirst().orElse("null");

		if (this.lock == null) {
			return "lockError";
		} else {
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			em.remove(this.lock);
			HibHelper.getInstance().commit();
			return "lockSuccess";
		}
	}

	private String getBase64Encoded(String data) throws UnsupportedEncodingException {
		return URLEncoder.encode(Base64.getEncoder().encodeToString(data.getBytes()), "UTF-8");
	}

	public boolean isLockedByAnotherUser(PeerReviewLock lock, String field) {
		return lock.getLockedBy().getId() != user.getId() && lock.getLockedField().equals(field);
	}

	private void clearOldLocks(Comment comment) {
		Calendar now = Calendar.getInstance();
		now.add(Calendar.MINUTE, -5);
		Date fiveMin = now.getTime();
		List<PeerReviewLock> allOldLocks = (List<PeerReviewLock>) em
				.createQuery("from PeerReviewLock where lastComment = :comment and lockedAt < :fiveMin")
				.setParameter("fiveMin", fiveMin).setParameter("comment", comment).getResultList();
		if(allOldLocks.size() != 0) {
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			allOldLocks.stream().forEach( lock -> em.remove(lock));
			HibHelper.getInstance().commit();
		}
	}

	public String getActivePR() {
		if (prqueue)
			return "active";
		else
			return "";
	}

	public String getActiveAQ() {
		if (!prqueue)
			return "active";
		else
			return "";
	}

	public Assessment getAsmt() {
		return asmt;
	}

	public User getUser() {
		return user;
	}

	public String getRisk() {
		return risk;
	}

	public void setRisk(String risk) {
		this.risk = risk;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getRisk_notes() {
		return risk_notes;
	}

	public void setRisk_notes(String risk_notes) {
		this.risk_notes = risk_notes;
	}

	public String getSum_notes() {
		return sum_notes;
	}

	public void setSum_notes(String sum_notes) {
		this.sum_notes = sum_notes;
	}

	public void setAsmt(Assessment asmt) {
		this.asmt = asmt;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public HashMap<String, String> getVuln_desc() {
		return vuln_desc;
	}

	public void setVuln_desc(HashMap<String, String> vuln_desc) {
		this.vuln_desc = vuln_desc;
	}

	public HashMap<String, String> getVuln_rec() {
		return vuln_rec;
	}

	public void setVuln_rec(HashMap<String, String> vuln_rec) {
		this.vuln_rec = vuln_rec;
	}
	public HashMap<String, String> getVuln_details() {
		return vuln_details;
	}
	public void setVuln_details(HashMap<String, String> vuln_details) {
		this.vuln_details = vuln_details;
	}


	public HashMap<String, String> getVuln_desc_notes() {
		return vuln_desc_notes;
	}

	public void setVuln_desc_notes(HashMap<String, String> vuln_desc_notes) {
		this.vuln_desc_notes = vuln_desc_notes;
	}

	public HashMap<String, String> getVuln_rec_notes() {
		return vuln_rec_notes;
	}

	public void setVuln_rec_notes(HashMap<String, String> vuln_rec_notes) {
		this.vuln_rec_notes = vuln_rec_notes;
	}
	public HashMap<String, String> getVuln_detail_notes() {
		return vuln_detail_notes;
	}

	public void setVuln_detail_notes(HashMap<String, String> vuln_detail_notes) {
		this.vuln_detail_notes = vuln_detail_notes;
	}

	public Long getPrid() {
		return prid;
	}

	public void setPrid(Long prid) {
		this.prid = prid;
	}

	public String getJsonResponse() {
		return jsonResponse;
	}

	public Boolean getPrqueue() {
		return prqueue;
	}

	public void setPrqueue(Boolean prqueue) {
		this.prqueue = prqueue;
	}

	public List<RiskLevel> getLevels() {
		return levels;
	}

	public Map<Long, List<CheckListAnswers>> getChecklists() {
		return checklists;
	}

	public void setChecklists(Map<Long, List<CheckListAnswers>> checklists) {
		this.checklists = checklists;
	}

	public List<Files> getFiles() {
		return files;
	}

	public boolean isShowSave() {
		return showSave;
	}

	public boolean isShowComplete() {
		return showComplete;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getFiled() {
		return this.field;
	}

	public void setLock(PeerReviewLock lock) {
		this.lock = lock;
	}

	public PeerReviewLock getLock() {
		return this.lock;
	}

	public List<PeerReviewLock> getAllLocks() {
		return this.allLocks;
	}

}
