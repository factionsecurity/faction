package com.fuse.dao;

import java.util.Base64;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;

import com.fuse.utils.FSUtils;

@Entity
public class Note {
	
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "noteGen")
	@TableGenerator(
			name = "noteGen", 
			table = "noteGenseq", 
			pkColumnValue = "note", 
			valueColumnName = "nextNote", 
			initialValue = 1, 
			allocationSize = 1)
	private Long id;
	private String name;
	private String note;
	@ManyToOne
	private User createdBy;
	@ManyToOne
	private User updatedBy;
	private Date created;
	private Date updated;
	private Boolean noteLocked = false;
	@ManyToOne
	private User noteLockedBy;
	private Date noteLockedAt;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = FSUtils.sanitizeHTML(name);
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = FSUtils.sanitizeHTML(note);
	}
	public User getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}
	public User getUpdatedBy() {
		return updatedBy;
	}
	public void setUpdatedBy(User updatedBy) {
		this.updatedBy = updatedBy;
	}
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	public Date getUpdated() {
		return updated;
	}
	public void setUpdated(Date updated) {
		this.updated = updated;
	}
	public Boolean getNoteLocked() {
		return noteLocked == null ? false : noteLocked;
	}
	public void setNoteLocked(Boolean noteLocked) {
		this.noteLocked = noteLocked;
	}
	public User getNoteLockedBy() {
		return noteLockedBy;
	}
	public void setNoteLockedBy(User noteLockedBy) {
		this.noteLockedBy = noteLockedBy;
	}
	public Date getNoteLockedAt() {
		return noteLockedAt;
	}
	public void setNoteLockedAt(Date noteLockedAt) {
		this.noteLockedAt = noteLockedAt;
	}
	@Transient
	public String getEncodedNote() {
		String note = this.note == null? "" : this.note;
		return Base64.getEncoder().encodeToString(this.note.getBytes());
	}
	
	
	

}