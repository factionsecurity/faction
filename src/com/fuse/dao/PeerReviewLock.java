package com.fuse.dao;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

@Entity
public class PeerReviewLock {

	@Id
	private String id = UUID.randomUUID().toString();

	private Date lockedAt;
	@ManyToOne
	private User lockedBy;
	@ManyToOne
	private Comment lastComment;
	private String lockedField;
	private String updatedText;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getLockedAt() {
		return lockedAt;
	}

	public void setLockedAt(Date lockedAt) {
		this.lockedAt = lockedAt;
	}

	public User getLockedBy() {
		return lockedBy;
	}

	public void setLockedBy(User lockedBy) {
		this.lockedBy = lockedBy;
	}

	public Comment getLastComment() {
		return lastComment;
	}

	public void setLastComment(Comment lastComment) {
		this.lastComment = lastComment;
	}

	public String getLockedField() {
		return lockedField;
	}

	public void setLockedField(String lockedField) {
		this.lockedField = lockedField;
	}

	@Transient
	public String getUpdatedText() {
		return this.updatedText;
	}

	@Transient
	public void setUpdatedText(String updatedText) {
		this.updatedText = updatedText;
	}

}
