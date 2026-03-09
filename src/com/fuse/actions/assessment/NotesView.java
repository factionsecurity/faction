package com.fuse.actions.assessment;


import java.util.Date;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;

import com.fuse.actions.FSActionSupport;
import com.fuse.dao.Assessment;
import com.fuse.dao.AuditLog;
import com.fuse.dao.Note;
import com.fuse.dao.User;
import com.fuse.dao.query.AssessmentQueries;

@Namespace("/portal")
@Result(name = "success", location = "/WEB-INF/jsp/assessment/Assessment.jsp", params = { "contentType", "text/html" })
public class NotesView extends FSActionSupport {
	
	private String id;
	private Note note;
	private Assessment assessment;
	private Boolean notowner;
	private String noteName;
	private Long noteid;
	private String noteText;

	
	
	
	@Action(value = "createNote", results = {
			@Result(name = "noteJSON", location = "/WEB-INF/jsp/assessment/noteJSON.jsp") 
	})
	public String createNote(){
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
		
		List<Note> notebook = assessment.getNotebook();
		this.note = new Note();
		note.setName(noteName);
		note.setNote("");
		note.setCreated(new Date());
		note.setUpdated(new Date());
		note.setCreatedBy(user);
		note.setUpdatedBy(user);
		notebook.add(note);
		AssessmentQueries.saveAssessment(this, em, assessment, "User has created a new note");
		
		return "noteJSON";
	}
	
	@Action(value = "getNote", results = {
			@Result(name = "noteJSON", location = "/WEB-INF/jsp/assessment/noteJSON.jsp") 
	})
	public String getNotebookNote(){
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
		
		this.note = assessment.getNoteById(this.noteid);
		
		return "noteJSON";
	}
	
	@Action(value = "updateNote", results = {
			@Result(name = "noteJSON", location = "/WEB-INF/jsp/assessment/noteJSON.jsp") 
	})
	public String updateNote(){
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
		if (assessment == null) {
			this._message = "Invalid Assessment";
			return this.ERRORJSON;
		}
		if (noteid == null) {
			this._message = "Invalid Note";
			return this.ERRORJSON;
		}
		
		note = assessment.getNoteById(noteid);
		
		if(note == null) {
			this._message = "Invalid Note";
			return this.ERRORJSON;
		}
		
		if( !(noteName != null && noteText != null)) {
			if(noteName != null)
				note.setName(noteName);
			if(noteText != null)
				note.setNote(noteText);
			note.setUpdated(new Date());
			note.setUpdatedBy(user);
			AssessmentQueries.saveAssessment(this, em, assessment, "User has created a new note");
		}
		
		return this.SUCCESSJSON;
	}
	@Action(value = "deleteNote")
	public String deleteNote() {
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
		
		Note note = assessment.getNoteById(this.noteid);
		assessment.getNotebook().remove(note);
		
		AssessmentQueries.saveAssessment(this, em, assessment, "User has created a new note");
		
		return this.SUCCESSJSON;
	}
	
	public void setNoteid(Long noteid) {
		this.noteid = noteid;
	}
	public void setNoteName(String noteName) {
		this.noteName = noteName;
	}
	public Note getNote() {
		return this.note;
	}
	public void setNoteText(String noteText) {
		this.noteText = noteText;
	}

}
