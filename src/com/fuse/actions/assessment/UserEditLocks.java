package com.fuse.actions.assessment;

import org.apache.struts2.convention.annotation.Result;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;

import com.fuse.actions.FSActionSupport;
import com.fuse.dao.Assessment;
import com.fuse.dao.HibHelper;
import com.fuse.dao.Note;
import com.fuse.dao.User;
import com.fuse.dao.Vulnerability;

@Result(name = "success", location = "/WEB-INF/jsp/assessment/Assessment.jsp", params = { "contentType", "text/html" })
public class UserEditLocks extends FSActionSupport {
	
	private Long id;
	private Assessment assessment;
	private String updatedText = "";
	private User user;
	private String action;
	private List<Vulnerability> lockedVulns;
	private List<Vulnerability> currentVulns;
	private List<Note> lockedNotes;
	
	/*
	 * Summary page user Locks
	 */


	@Action(value = "/portal/summary/check/locks", results = {
			@Result(name = "lockSuccess", location = "/WEB-INF/jsp/assessment/lockSuccess.jsp"),
			@Result(name = "lockError", location = "/WEB-INF/jsp/assessment/lockError.jsp"),
			@Result(name = "lockJSON", location = "/WEB-INF/jsp/assessment/lockJSON.jsp"), })
	public String checkLocks() throws UnsupportedEncodingException {
		if (!(this.isAcassessor() || this.isAcmanager())) {
			_message="Session Expired";
			return "lockError";
		}
		this.user = this.getSessionUser();
		Long asmtId = (Long) this.getSession("asmtid");
		this.assessment = em.find(Assessment.class, asmtId);
		clearOutdatedLocks(assessment);
		if (isNotesLockedbyAnotherUser() || isSummaryLockedbyAnotherUser() || isRiskLockedbyAnotherUser()) {
			this.clearLockType("", this.user);
			if(this.assessment.getSummary() == null) {
				this.assessment.setSummary("");
			}
			if(this.assessment.getRiskAnalysis() == null) {
				this.assessment.setRiskAnalysis("");
			}
			/*this.assessment.setNotes(URLEncoder
					.encode(Base64.getEncoder().encodeToString(this.assessment.getNotes().getBytes()), "UTF-8"));*/
			this.assessment.setSummary(URLEncoder
					.encode(Base64.getEncoder().encodeToString(this.assessment.getSummary().getBytes()), "UTF-8"));
			this.assessment.setRiskAnalysis(URLEncoder
					.encode(Base64.getEncoder().encodeToString(this.assessment.getRiskAnalysis().getBytes()), "UTF-8"));
			return "lockJSON";
		} else {
			return "lockSuccess";
		}
	}

	@Action(value = "/portal/summary/set/lock", results = {
			@Result(name = "lockSuccess", location = "/WEB-INF/jsp/assessment/lockSuccess.jsp"),
			@Result(name = "lockError", location = "/WEB-INF/jsp/assessment/lockError.jsp"),
			@Result(name = "lockJSON", location = "/WEB-INF/jsp/assessment/lockJSON.jsp"), })
	public String setLock() throws UnsupportedEncodingException {
		if (!(this.isAcassessor() || this.isAcmanager())) {
			return LOGIN;
		}
		User user = this.getSessionUser();
		Long asmtId = (Long) this.getSession("asmtid");
		this.assessment = em.find(Assessment.class, asmtId);
		if (this.action.equals("summary") && !assessment.isSummaryLock()) {
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

	@Action(value = "/portal/summary/clear/lock", results = {
			@Result(name = "lockSuccess", location = "/WEB-INF/jsp/assessment/lockSuccess.jsp"),
			@Result(name = "lockError", location = "/WEB-INF/jsp/assessment/lockError.jsp"),
			@Result(name = "lockJSON", location = "/WEB-INF/jsp/assessment/lockJSON.jsp"), })
	public String clearLock() {
		if (!(this.isAcassessor() || this.isAcmanager())) {
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
	
	/*
	 * Vulnerability Page User Locks
	 */
	
	@Action(value = "/portal/vulnerability/set/lock", results = {
			@Result(name = "lockSuccess", location = "/WEB-INF/jsp/assessment/lockSuccess.jsp"),
			@Result(name = "lockError", location = "/WEB-INF/jsp/assessment/lockError.jsp"),
			@Result(name = "lockJSON", location = "/WEB-INF/jsp/assessment/lockJSON.jsp"), })
	public String Setock() {
		if (!(this.isAcassessor() || this.isAcmanager()))
			return LOGIN;

		if (!this.testToken(false))
			return this.ERRORJSON;
		
		if (this.id == null) {
			this._message = "Must Select a Vulnerability";
			return this.ERRORJSON;
		}
		User user = this.getSessionUser();
		
		Vulnerability vuln = em.find(Vulnerability.class, id);
		if (vuln == null) {
			this._message = "Not a valid Vulnerability";
			return this.ERRORJSON;
		}
		
		if(vuln.getDetail_lock() && vuln.getDesc_locked_by().getId() != user.getId()) {
			this._message = "Edits are locked by another user.";
			return this.ERRORJSON;
			
		}else {
			vuln.setDesc_lock(true);
			vuln.setDesc_locked_by(user);
			vuln.setDesc_lock_time(new Date());
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			em.persist(vuln);
			HibHelper.getInstance().commit();
		}
		
		
		return "lockSuccess";
	}
	
	@Action(value = "/portal/vulnerability/clear/lock", results = {
			@Result(name = "lockSuccess", location = "/WEB-INF/jsp/assessment/lockSuccess.jsp"),
			@Result(name = "lockError", location = "/WEB-INF/jsp/assessment/lockError.jsp"),
			@Result(name = "lockJSON", location = "/WEB-INF/jsp/assessment/lockJSON.jsp"), })
	public String ClearVulnLock() {
		if (!(this.isAcassessor() || this.isAcmanager()))
			return LOGIN;

		if (!this.testToken(false))
			return this.ERRORJSON;
		
		if (this.id == null) {
			this._message = "Must Select a Vulnerability";
			return this.ERRORJSON;
		}
		User user = this.getSessionUser();
		
		Vulnerability vuln = em.find(Vulnerability.class, id);
		if (vuln == null) {
			this._message = "Not a valid Vulnerability";
			return this.ERRORJSON;
		}
		//TODO: for now block out the entire vuln but later make it so that each section can be locked individually
		if(vuln.getDetail_lock() && vuln.getDesc_locked_by().getId() != user.getId()) {
			this._message = "Edits are locked by another user.";
			return this.ERRORJSON;
			
		}else {
			vuln.setDesc_lock(false);
			vuln.setDesc_locked_by(null);
			vuln.setDesc_lock_time(null);
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			em.persist(vuln);
			HibHelper.getInstance().commit();
		}
		return "lockSuccess";
	}
	
	/// The method checks for both note and vulnerability locks
	@Action(value = "/portal/vulnerability/check/locks", results = {
			@Result(name = "lockSuccess", location = "/WEB-INF/jsp/assessment/lockSuccess.jsp"),
			@Result(name = "lockError", location = "/WEB-INF/jsp/assessment/lockError.jsp"),
			@Result(name = "lockJSON", location = "/WEB-INF/jsp/assessment/lockJSON.jsp"), })
	public String CheckVulnLocks() {
		if (!(this.isAcassessor() || this.isAcmanager())) {
			_message="Session Expired";
			return "lockError";
		}

		if (this.id == null) {
			this._message = "Invalid Assessment";
			return this.ERRORJSON;
		}
		User user = this.getSessionUser();
		
		
		assessment = em.find(Assessment.class, id);
		clearOutdatedLocks(assessment);
		currentVulns = assessment.getVulns();
		currentVulns.stream().forEach( v -> v.updateRiskLevels(em));
		lockedVulns= currentVulns
				.stream()
				.filter( v -> v.getDesc_lock() && v.getDesc_locked_by().getId() != user.getId())
				.collect(Collectors.toList());
		lockedNotes = assessment
				.getNotebook()
				.stream()
				.filter( v -> v.getNoteLocked() && v.getNoteLockedBy().getId() != user.getId())
				.collect(Collectors.toList());
		
		return "lockJSON";
	}
	
	/*
	 * Note Page User Locks
	 */
	
	@Action(value = "/portal/note/set/lock", results = {
			@Result(name = "lockSuccess", location = "/WEB-INF/jsp/assessment/lockSuccess.jsp"),
			@Result(name = "lockError", location = "/WEB-INF/jsp/assessment/lockError.jsp"),
			@Result(name = "lockJSON", location = "/WEB-INF/jsp/assessment/lockJSON.jsp"), })
	public String SetNoteLock() {
		if (!(this.isAcassessor() || this.isAcmanager()))
			return LOGIN;

		if (!this.testToken(false))
			return this.ERRORJSON;
		
		if (this.id == null) {
			this._message = "Must Select a Vulnerability";
			return this.ERRORJSON;
		}
		User user = this.getSessionUser();
		
		//TODO: Security Check that the user is editing a note associated to their assessment
		
		Note note = em.find(Note.class, id);
		if (note == null) {
			this._message = "Not a valid Note";
			return this.ERRORJSON;
		}
		
		if(note.getNoteLocked() && note.getNoteLockedBy().getId() != user.getId()) {
			this._message = "Edits are locked by another user.";
			return this.ERRORJSON;
			
		}else {
			note.setNoteLocked(true);
			note.setNoteLockedBy(user);
			note.setNoteLockedAt(new Date());
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			em.persist(note);
			HibHelper.getInstance().commit();
		}
		
		
		return "lockSuccess";
	}
	
	@Action(value = "/portal/note/clear/lock", results = {
			@Result(name = "lockSuccess", location = "/WEB-INF/jsp/assessment/lockSuccess.jsp"),
			@Result(name = "lockError", location = "/WEB-INF/jsp/assessment/lockError.jsp"),
			@Result(name = "lockJSON", location = "/WEB-INF/jsp/assessment/lockJSON.jsp"), })
	public String ClearNoteLock() {
		if (!(this.isAcassessor() || this.isAcmanager()))
			return LOGIN;

		if (!this.testToken(false))
			return this.ERRORJSON;
		
		if (this.id == null) {
			this._message = "Note a valid Note";
			return this.ERRORJSON;
		}
		User user = this.getSessionUser();
		
		//TODO: Check that the user is editing a note associated to their assessment
		Note note = em.find(Note.class, id);
		if (note == null) {
			this._message = "Not a valid Note";
			return this.ERRORJSON;
		}
		if(note.getNoteLocked() && note.getNoteLockedBy().getId() != user.getId()) {
			this._message = "Edits are locked by another user.";
			return this.ERRORJSON;
			
		}else {
			note.setNoteLocked(false);
			note.setNoteLockedBy(null);
			note.setNoteLockedAt(null);
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			em.persist(note);
			HibHelper.getInstance().commit();
		}
		return "lockSuccess";
	}
	
	
	/*
	 * Supporting Functions
	 */
	
	private void clearOutdatedLocks(Assessment assessment) {
		
		boolean isUpdated = false;
		Calendar now = Calendar.getInstance();
		now.add(Calendar.MINUTE, -5);
		Date FiveMin = now.getTime();
		if (assessment.getSummaryLockAt() != null && assessment.getSummaryLockAt().before(FiveMin)) {
			this.clearLockType("summary", assessment.getSummaryLockBy());
			isUpdated = true;
		}
		if (assessment.getRiskLockAt() != null && assessment.getRiskLockAt().before(FiveMin)) {
			this.clearLockType("risk", assessment.getRiskLockBy());
			isUpdated = true;
		}
		if (assessment.getRiskLockAt() != null && assessment.getRiskLockAt().before(FiveMin)) {
			this.clearLockType("risk", assessment.getRiskLockBy());
			isUpdated = true;
		}
		for(Vulnerability vuln : assessment.getVulns()) {
			if(vuln.getDesc_lock_time() !=null && vuln.getDesc_lock_time().before(FiveMin) ) {
				vuln.setDesc_lock(false);
				vuln.setDesc_lock_time(null);
				vuln.setDesc_locked_by(null);
			}
		}
		for(Note note : assessment.getNotebook()) {
			if(note.getNoteLockedAt() !=  null && note.getNoteLockedAt().before(FiveMin)) {
				note.setNoteLocked(false);
				note.setNoteLockedAt(null);
				note.setNoteLockedBy(null);
			}
		}
		if(isUpdated) {
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			em.persist(assessment);
			HibHelper.getInstance().commit();
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
	
	/*
	 * Getters and Setters
	 */
	public void setId(Long id) {
		this.id = id;
	}
	public String getUpdatedText() {
		return this.updatedText;
	}
	public void setAction(String action) {
		this.action	= action;
	}
	public Assessment getAssessment() {
		return this.assessment;
	}
	public List<Vulnerability> getLockedVulns(){
		return this.lockedVulns;
	}
	public List<Vulnerability> getCurrentVulns(){
		return this.currentVulns;
	}
	public List<Note> getLockedNotes() {
		return this.lockedNotes;
	}
	
	public String getSafeJSON(String vuln) {
		return Base64.getEncoder().encodeToString(vuln.getBytes());
	}
}