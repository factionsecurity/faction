package com.fuse.dao;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.TableGenerator;

@Entity
public class VulnNotes implements Serializable{
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "vnGen")
    @TableGenerator(
        name = "vnGen",
        table = "vnGenseq",
        pkColumnValue = "vn",
        valueColumnName = "nextVn",
        initialValue = 1,
        allocationSize = 1
    )
	private Long id;
	private String uuid;
	private String note;
	private Long vulnId;
	private Date created;
	private Long creator;
	@ManyToOne
	private User creatorObj;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public Long getVulnId() {
		return vulnId;
	}
	public void setVulnId(Long vulnId) {
		this.vulnId = vulnId;
	}
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	public Long getCreator() {
		return creator;
	}
	public void setCreator(Long creator) {
		this.creator = creator;
	}
	public User getCreatorObj() {
		return creatorObj;
	}
	public void setCreatorObj(User creatorObj) {
		this.creatorObj = creatorObj;
	}
	
	
	

}
