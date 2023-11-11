package com.fuse.dao;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Notification {
	
	@Id
	@GeneratedValue
	private Long id;
	private String message;
	private Long assessorId;
	private Date created;
	public Long getId() {
		return id;
	}
	public String getMessage() {
		return message;
	}
	public Long getAssessorId() {
		return assessorId;
	}
	public Date getCreated() {
		return created;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public void setAssessorId(Long assessorId) {
		this.assessorId = assessorId;
	}
	public void setCreated(Date date) {
		this.created = date;
	}
	
	
	

}
